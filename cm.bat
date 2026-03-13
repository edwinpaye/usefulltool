@echo off
setlocal EnableDelayedExpansion

:: ====================== CONFIG ======================
set "SCRIPT_DIR=%~dp0"
set "CONFIG_FILE=%SCRIPT_DIR%commands.cfg"
set "LOG_FILE=%SCRIPT_DIR%ecm.log"
set "VERSION=1.1.0"
set "TEMP_CFG=%TEMP%\ecm_temp_!RANDOM!.cfg"

:: ====================== CREATE DEFAULT CONFIG ======================
if not exist "%CONFIG_FILE%" (
    echo [INFO] Creating default configuration...
    (
        echo # ====================================================
        echo # Enterprise Command Manager - commands.cfg
        echo # Format:    alias=full command with spaces ^&^& etc
        echo # Comments:  lines starting with # or ;
        echo # ====================================================
        echo.
        echo start=echo [START] Launching... ^&^& docker-compose up -d
        echo stop=docker-compose down --remove-orphans
        echo logs=docker-compose logs -f --tail=200
        echo kget=kubectl get pods -A -o wide
        echo build=mvn clean package -DskipTests
    ) > "%CONFIG_FILE%"
)

:: ====================== ARGUMENT PARSING ======================
if "%~1"=="" goto :show_help

set "ACTION=%~1"
set "ARG1=%~2"
set "ARG2=%~3"

:: Normalize common aliases
if /i "%ACTION%"=="--help"     set "ACTION=help"
if /i "%ACTION%"=="-h"         set "ACTION=help"
if /i "%ACTION%"=="list"       set "ACTION=list"
if /i "%ACTION%"=="show"       set "ACTION=show"
if /i "%ACTION%"=="cat"        set "ACTION=show"
if /i "%ACTION%"=="add"        set "ACTION=add"
if /i "%ACTION%"=="edit"       set "ACTION=edit"
if /i "%ACTION%"=="remove"     set "ACTION=remove"
if /i "%ACTION%"=="rm"         set "ACTION=remove"
if /i "%ACTION%"=="del"        set "ACTION=remove"

:: Built-in meta commands
if /i "%ACTION%"=="help"       goto :show_help
if /i "%ACTION%"=="version"    goto :show_version
if /i "%ACTION%"=="--version"  goto :show_version
if /i "%ACTION%"=="-v"         goto :show_version

:: ====================== CRUD OPERATIONS ======================

if /i "%ACTION%"=="list" goto :do_list
if /i "%ACTION%"=="show" goto :do_show
if /i "%ACTION%"=="add"  goto :do_add
if /i "%ACTION%"=="edit" goto :do_edit
if /i "%ACTION%"=="remove" goto :do_remove


:: ────────────── Normal alias execution ──────────────
shift

set "args="

:: Rebuild %* without the first (original) argument
:rebuild_args
set "test=%~1"
@REM if not defined test echo blank
if "%~1"=="" goto :execute_command
if defined args (set "args=%args% ") else set "args="
set "args=%args%%~1"
shift
goto :rebuild_args

:: Otherwise → treat as regular command execution
goto :execute_command

:: ────────────────────────────────────────────────
::  LIST all aliases
:: ────────────────────────────────────────────────
:do_list
echo.
echo Available commands:
echo.
for /f "usebackq delims=" %%L in (`findstr /v /b "^[#;]" "%CONFIG_FILE%" ^| findstr "="`) do (
    for /f "tokens=1,* delims==" %%a in ("%%L") do (
        set "key=%%a"
        set "val=%%b"
        for /f "tokens=* delims= " %%k in ("!key!") do set "key=%%k"
        for /f "tokens=* delims= " %%v in ("!val!") do set "val=%%v"
        if defined key (
            echo   !key! ^( !val! ^)
        )
    )
)
echo.
echo Use:   ecm add ^<alias^> "^<command^>"
echo        ecm edit ^<alias^> "^<new command^>"
echo        ecm remove ^<alias^>
echo.
exit /b 0

:: ────────────────────────────────────────────────
::  SHOW one command or whole file
:: ────────────────────────────────────────────────
:do_show
if "%ARG1%"=="" (
    echo [INFO] Showing full config:
    echo.
    type "%CONFIG_FILE%"
    exit /b 0
)

set "FOUND="
for /f "usebackq delims=" %%L in (`findstr /v /b "^[#;]" "%CONFIG_FILE%" ^| findstr "="`) do (
    for /f "tokens=1,* delims==" %%a in ("%%L") do (
        set "key=%%a"
        for /f "tokens=* delims= " %%k in ("!key!") do set "key=%%k"
        if /i "!key!"=="%ARG1%" (
            set "FOUND=1"
            set "val=%%b"
            for /f "tokens=* delims= " %%v in ("!val!") do set "val=%%v"
            echo !key! = !val!
        )
    )
)

if not defined FOUND (
    echo [ERROR] Alias not found: %ARG1%
    exit /b 1
)
exit /b 0

:: ────────────────────────────────────────────────
::  ADD new entry
:: ────────────────────────────────────────────────
:do_add
if "%ARG1%"=="" goto :add_usage
if "%ARG2%"=="" goto :add_usage

:: Check if already exists
set "EXISTS="
for /f "usebackq delims=" %%L in (`findstr /v /b "^[#;]" "%CONFIG_FILE%" ^| findstr "="`) do (
    for /f "tokens=1,* delims==" %%a in ("%%L") do (
        set "key=%%a"
        for /f "tokens=* delims= " %%k in ("!key!") do set "key=%%k"
        if /i "!key!"=="%ARG1%" set "EXISTS=1"
    )
)

if defined EXISTS (
    echo [ERROR] Alias already exists: %ARG1%
    echo Use 'ecm edit %ARG1% ...' to change it.
    exit /b 1
)

:: Append new line
echo %ARG1%=%ARG2%>> "%CONFIG_FILE%"

echo [OK] Added:
echo   %ARG1% = %ARG2%
echo.
exit /b 0

:add_usage
echo Usage:
echo   ecm add ^<alias^> "^<full command with spaces^>"
echo Example:
echo   ecm add deploy "kubectl apply -k ./overlays/prod"
exit /b 1

:: ────────────────────────────────────────────────
::  EDIT existing entry
:: ────────────────────────────────────────────────
:do_edit
if "%ARG1%"=="" goto :edit_usage
if "%ARG2%"=="" goto :edit_usage

set "FOUND="
>"%TEMP_CFG%" (
    for /f "usebackq delims=" %%L in ("%CONFIG_FILE%") do (
        set "line=%%L"
        if "!line:~0,1!"=="#" (
            echo !line!
        ) else if "!line:~0,1!"==";" (
            echo !line!
        ) else if "!line!"=="" (
            echo.
        ) else (
            for /f "tokens=1,* delims==" %%a in ("!line!") do (
                set "key=%%a"
                for /f "tokens=* delims= " %%k in ("!key!") do set "key=%%k"
                if /i "!key!"=="%ARG1%" (
                    set "FOUND=1"
                    echo %ARG1%=%ARG2%
                ) else (
                    echo !line!
                )
            )
        )
    )
)

if not defined FOUND (
    del "%TEMP_CFG%" 2>nul
    echo [ERROR] Alias not found: %ARG1%
    exit /b 1
)

move /y "%TEMP_CFG%" "%CONFIG_FILE%" >nul
echo [OK] Updated:
echo   %ARG1% = %ARG2%
exit /b 0

:edit_usage
echo Usage:
echo   ecm edit ^<alias^> "^<new full command^>"
echo Example:
echo   ecm edit logs "docker-compose logs -f --tail=500"
exit /b 1

:: ────────────────────────────────────────────────
::  REMOVE entry
:: ────────────────────────────────────────────────
:do_remove
if "%ARG1%"=="" goto :remove_usage

set "FOUND="
>"%TEMP_CFG%" (
    for /f "usebackq delims=" %%L in ("%CONFIG_FILE%") do (
        set "line=%%L"
        if "!line:~0,1!"=="#" (
            echo !line!
        ) else if "!line:~0,1!"==";" (
            echo !line!
        ) else if "!line!"=="" (
            echo.
        ) else (
            for /f "tokens=1,* delims==" %%a in ("!line!") do (
                set "key=%%a"
                for /f "tokens=* delims= " %%k in ("!key!") do set "key=%%k"
                if /i "!key!"=="%ARG1%" (
                    set "FOUND=1"
                    REM skip this line → effectively delete
                ) else (
                    echo !line!
                )
            )
        )
    )
)

if not defined FOUND (
    del "%TEMP_CFG%" 2>nul
    echo [ERROR] Alias not found: %ARG1%
    exit /b 1
)

move /y "%TEMP_CFG%" "%CONFIG_FILE%" >nul
echo [OK] Removed alias: %ARG1%
exit /b 0

:remove_usage
echo Usage:
echo   ecm remove ^<alias^>
echo   ecm rm ^<alias^>
echo   ecm del ^<alias^>
exit /b 1

:: ────────────────────────────────────────────────
::  EXECUTE normal command (fallback)
:: ────────────────────────────────────────────────
:execute_command
set "SUBCMD=%ACTION%"
set "FULL_CMD="

for /f "usebackq delims=" %%L in (`findstr /v /b "^[#;]" "%CONFIG_FILE%" ^| findstr "="`) do (
    for /f "tokens=1,* delims==" %%a in ("%%L") do (
        set "key=%%a"
        set "val=%%b"
        for /f "tokens=* delims= " %%k in ("!key!") do set "key=%%k"
        for /f "tokens=* delims= " %%v in ("!val!") do set "val=%%v"
        if /i "!key!"=="%SUBCMD%" (
            set "FULL_CMD=!val!"
            goto :command_found
        )
    )
)

:command_not_found
echo [ERROR] Unknown command or alias: %SUBCMD%
echo.
goto :show_help

:command_found
if not defined FULL_CMD goto :command_not_found

echo [%DATE% %TIME%] [EXEC] %SUBCMD%  Args: !args! >> "%LOG_FILE%"
echo Running: !FULL_CMD! !args!

@REM !FULL_CMD! %*
!FULL_CMD! !args!
set "EXIT_CODE=%ERRORLEVEL%"

echo [%DATE% %TIME%] [DONE] Exit: %EXIT_CODE% >> "%LOG_FILE%"
exit /b %EXIT_CODE%

:: ────────────────────────────────────────────────
::  HELP & VERSION
:: ────────────────────────────────────────────────
:show_version
echo Enterprise Command Manager ^(ECM^) v%VERSION%
exit /b 0

:show_help
echo.
echo Enterprise Command Manager ^(ECM^) v%VERSION%
echo Manage prefixed/shortcut commands + CRUD on config
echo.
echo Usage:
echo   %~nx0 ^<alias^> [arguments...]
echo   %~nx0 ^<action^> [parameters]
echo.
echo Actions:
echo   list              List all available aliases
echo   show ^<alias^>      Show command for one alias (or whole file if no arg)
echo   add ^<alias^> "^<command^>"    Add new shortcut
echo   edit ^<alias^> "^<new command^>"  Change existing command
echo   remove ^<alias^>    Delete alias   (also: rm, del)
echo   help / --help / -h  This help
echo   version / --version / -v   Show version
echo.
echo Examples:
echo   ecm start
echo   ecm logs --tail 300
echo   ecm add clean "mvn clean -U"
echo   ecm edit clean "mvn clean install -U -DskipTests"
echo   ecm remove clean
echo   ecm show logs
echo.
echo Config file: %CONFIG_FILE%
echo Log file:    %LOG_FILE%
echo.
exit /b 0