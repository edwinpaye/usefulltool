package com.tool.app;

import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

@Command(
    name = "usefulltool",
    description = "A collection of useful CLI utilities.",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    subcommands = {
        CopyCommand.class,
        PasteCommand.class,
        HashCommand.class,
        Base64Command.class,
        ScafoldCommand.class,
        CatCommand.class,
        HelpCommand.class
    }
)
public class UsefullToolCommand implements Runnable {

    @Option(names = {"-v", "--version"}, versionHelp = true, description = "Display version info")
    boolean versionHelpRequested;

    @Override
    public void run() {
        // If no subcommand, show help
        System.out.println(new picocli.CommandLine(this).getUsageMessage());
    }
}