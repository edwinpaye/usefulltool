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
import java.util.ArrayList;
import java.util.List;

@Command(
    name = "sca",
    description = "Make scafold from clipboard content. Creates the files and directories if they don't exist."
)
public class ScafoldCommand implements Runnable {

    @Parameters(index = "1", arity = "0..1", description = "Optional content to copy. If not provided, uses clipboard content.")
    private String content;

    @Parameters(index = "0", arity = "0..1", description = "Path for the scafolding.")
    private String filePathStr;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    boolean helpRequested;

    @Override
    public void run() {
        try {
        // if (args.length == 0) {
        //     System.out.println("Usage: java -jar tree-creator-0.0.1-SNAPSHOT.jar <path_to_txt_file>");
        //     return;
        // }

        // String filePath = args[0];
        // List<String> lines = Files.readAllLines(Paths.get(filePath));

        // Path filePath = Paths.get(filePathStr);

        if (content == null) {
            // Retrieve from clipboard
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                content = (String) clipboard.getData(DataFlavor.stringFlavor);
            } else {
                throw new UnsupportedFlavorException(DataFlavor.stringFlavor);
            }
        }

        // // Create directories if needed
        // if (filePath.getParent() != null) {
        //     Files.createDirectories(filePath.getParent());
        // }

        List<String> lines = List.of(content.split("\n"));

        if (lines.isEmpty()) {
            System.out.println("Empty file");
            return;
        }

        List<String> pathStack = new ArrayList<>();

        String root = lines.get(0).split(" ")[0].trim();
        if (root.endsWith("/")) {
            root = root.substring(0, root.length() - 1);
        }
        Files.createDirectories(Paths.get(root));
        pathStack.add(root);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;

            // Find connector pos ('├' or '└')
            int connectorPos = -1;
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                if (c == '\u251C' || c == '\u2514') { // ├ or └
                    connectorPos = j;
                    break;
                }
            }
            if (connectorPos == -1) {
                System.out.println("Skipping invalid line: " + line);
                continue;
            }

            // Validate connector sequence: '─' '─' ' '
            if (connectorPos + 3 >= line.length() ||
                line.charAt(connectorPos + 1) != '\u2500' || // ─
                line.charAt(connectorPos + 2) != '\u2500' || // ─
                line.charAt(connectorPos + 3) != ' ') {
                System.out.println("Skipping invalid connector in line: " + line);
                continue;
            }

            String name = line.substring(connectorPos + 4).split(" ")[0].trim();

            int depth = (connectorPos / 4) + 1;

            // Pop stack to correct depth
            while (pathStack.size() > depth) {
                pathStack.remove(pathStack.size() - 1);
            }

            // Build full path
            StringBuilder fullPath = new StringBuilder();
            for (String p : pathStack) {
                fullPath.append(p).append("/");
            }
            fullPath.append(name);
            String strFullPath = fullPath.toString();

            if (name.endsWith("/")) {
                String dirName = name.substring(0, name.length() - 1);
                Files.createDirectories(Paths.get(strFullPath));
                pathStack.add(dirName);
            } else {
                Files.createFile(Paths.get(strFullPath));
            }
        }

        System.out.println("Structure created successfully.");

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
