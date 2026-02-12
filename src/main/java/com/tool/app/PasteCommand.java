package com.tool.app;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Command(
    name = "paste",
    description = "Read content from a file and copy it to the clipboard."
)
public class PasteCommand implements Runnable {

    @Parameters(index = "0", description = "Path to the source file.")
    private String filePathStr;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    boolean helpRequested;

    @Override
    public void run() {
        try {
            Path filePath = Paths.get(filePathStr);
            if (!Files.exists(filePath)) {
                throw new IOException("File does not exist: " + filePath);
            }

            String content = Files.readString(filePath);

            // Copy to clipboard
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection selection = new StringSelection(content);
            clipboard.setContents(selection, null);

            System.out.println("Content from " + filePath.toAbsolutePath() + " copied to clipboard.");
        } catch (HeadlessException e) {
            System.err.println("Error: Clipboard access not available in headless environment.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
