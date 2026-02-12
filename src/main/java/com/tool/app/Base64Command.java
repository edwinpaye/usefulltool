package com.tool.app;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Command(
    name = "base64",
    description = "Encode or decode Base64 for a string or file."
)
public class Base64Command implements Runnable {

    @Parameters(index = "0", description = "Action: encode or decode.")
    private String action;

    @Parameters(index = "1", description = "String or path to file.")
    private String input;

    @Option(names = {"-f", "--file"}, description = "Treat input as a file path (default: auto-detect).")
    boolean forceFile;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    boolean helpRequested;

    @Override
    public void run() {
        try {
            byte[] bytes;
            Path path = Paths.get(input);
            if (forceFile || Files.exists(path)) {
                bytes = Files.readAllBytes(path);
            } else {
                bytes = input.getBytes();
            }

            String result;
            if ("encode".equalsIgnoreCase(action)) {
                result = Base64.getEncoder().encodeToString(bytes);
            } else if ("decode".equalsIgnoreCase(action)) {
                result = new String(Base64.getDecoder().decode(bytes));
            } else {
                throw new IllegalArgumentException("Invalid action: " + action + ". Use 'encode' or 'decode'.");
            }

            System.out.println(result);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
