@echo off
setlocal enabledelayedexpansion

:: --- CONFIGURATION ---
:: The name you want your JAR to have in this folder
set "JAR_NAME=app.jar"
set "JAR_PATH=%~dp0%JAR_NAME%"

:: --- CHECK FOR UPDATE FLAG ---
:: If the first argument is --update or -u
if /i "%~1"=="--update" goto :update_logic
if /i "%~1"=="-u" goto :update_logic

:: --- EXECUTION LOGIC ---
@REM echo [INFO] Script Location: %~dp0
if not exist "%JAR_PATH%" (
    echo [ERROR] JAR not found: %JAR_PATH%
    echo Use "--update [path]" to install the JAR first.
    exit /b 1
)

@REM echo [INFO] Launching app with params: %*
@REM java -Dfile.encoding=UTF-8 -jar "%JAR_PATH%" %*
java -jar "%JAR_PATH%" %*
exit /b %ERRORLEVEL%

:: --- UPDATE LOGIC ---
:update_logic
set "SOURCE_FILE=%~2"

if "%SOURCE_FILE%"=="" (
    echo [ERROR] No source file provided.
    echo Usage: %~nx0 --update ".\target\new_version.jar"
    exit /b 1
)

if not exist "%SOURCE_FILE%" (
    echo [ERROR] Source file does not exist: "%SOURCE_FILE%"
    exit /b 1
)

@REM echo [INFO] Replacing local JAR...
copy /y "%SOURCE_FILE%" "%JAR_PATH%"

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] %JAR_NAME% has been updated in %~dp0
) else (
    echo [ERROR] Failed to replace file. Ensure the app is not currently running.
)
exit /b %ERRORLEVEL%