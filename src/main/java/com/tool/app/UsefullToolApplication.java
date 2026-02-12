package com.tool.app;

import picocli.CommandLine;

public class UsefullToolApplication {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new UsefullToolCommand()).execute(args);
        System.exit(exitCode);
    }
}