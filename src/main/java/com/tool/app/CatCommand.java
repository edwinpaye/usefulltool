package com.tool.app;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Command(
    name = "cat",
    description = "Print file content or clipboard content."
)
public class CatCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Path to the target file.")
    private String filePathStr;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    boolean helpRequested;

    @Override
    public void run() {
        try {
            if (filePathStr == null) {
                // Retrieve from clipboard
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    String content = (String) clipboard.getData(DataFlavor.stringFlavor);
                    System.out.println("\n" + content);
                } else {
                    throw new UnsupportedFlavorException(DataFlavor.stringFlavor);
                }
            } else {
                Path filePath = Paths.get(filePathStr);
                String content = Files.readString(filePath);
                System.out.println("\n" + content);
            }

            // System.out.println("Content copied to " + filePath.toAbsolutePath());
        } catch (HeadlessException e) {
            System.err.println("Error: Clipboard access not available in headless environment.");
            System.exit(1);
        } catch (UnsupportedFlavorException | IOException e) {
            System.err.println("Error retrieving clipboard content: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
