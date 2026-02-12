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
    name = "copy",
    description = "Copy provided content or clipboard content to a file. Creates the file and directories if they don't exist."
)
public class CopyCommand implements Runnable {

    @Parameters(index = "1", arity = "0..1", description = "Optional content to copy. If not provided, uses clipboard content.")
    private String content;

    @Parameters(index = "0", description = "Path to the target file.")
    private String filePathStr;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    boolean helpRequested;

    @Override
    public void run() {
        try {
            Path filePath = Paths.get(filePathStr);

            if (content == null) {
                // Retrieve from clipboard
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    content = (String) clipboard.getData(DataFlavor.stringFlavor);
                } else {
                    throw new UnsupportedFlavorException(DataFlavor.stringFlavor);
                }
            }

            // Create directories if needed
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            // Write content to file
            Files.writeString(filePath, content);
            System.out.println("Content copied to " + filePath.toAbsolutePath());
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

// import picocli.CommandLine.Command;
// import picocli.CommandLine.Parameters;
// import picocli.CommandLine.Option;

// import java.awt.*;
// import java.awt.datatransfer.Clipboard;
// import java.awt.datatransfer.DataFlavor;
// import java.awt.datatransfer.UnsupportedFlavorException;
// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;

// @Command(
//     name = "copy",
//     description = "Writes content to a file. " +
//                   "Behavior depends on provided arguments:\n" +
//                   "  usefulltool copy [CONTENT] [PATH]     → writes CONTENT to PATH\n" +
//                   "  usefulltool copy [PATH]               → writes current clipboard to PATH\n" +
//                   "  usefulltool copy                      → reads clipboard as PATH, " +
//                   "writes previous clipboard content to that file"
// )
// public class CopyCommand implements Runnable {

//     // Positional parameters (0 or 1 or 2)
//     @Parameters(index = "0", arity = "0..1", description = "Optional: content to write (if omitted → use clipboard). " +
//             "If only one parameter is given and no content provided → treated as PATH and previous clipboard is written.")
//     private String arg0;

//     @Parameters(index = "1", arity = "0..1", description = "Path to the target file (required unless using clipboard-as-path mode).")
//     private String arg1;

//     @Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help.")
//     boolean helpRequested;

//     @Override
//     public void run() {
//         try {
//             String contentToWrite;
//             String targetPathStr;

//             Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//             if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
//                 System.err.println("Error: Clipboard does not contain text.");
//                 System.exit(1);
//             }

//             // Case 1 & 2: two arguments → arg0 = content, arg1 = path
//             if (arg1 != null) {
//                 contentToWrite = (arg0 != null) ? arg0 : getClipboardText(clipboard);
//                 targetPathStr = arg1;

//             // Case 3: only one argument → arg0 = path, content = current clipboard
//             } else if (arg0 != null) {
//                 contentToWrite = getClipboardText(clipboard);
//                 targetPathStr = arg0;

//             // Case 4: no arguments → clipboard = path, previous clipboard = content
//             } else {
//                 // Read clipboard → this will be used as PATH
//                 String clipboardAsPath = getClipboardText(clipboard);

//                 // Now read clipboard AGAIN → this should be the "previous" content
//                 // (User needs to have copied something before copying the path)
//                 String previousContent = getClipboardText(clipboard);

//                 // In practice the two reads are almost the same unless user copies very fast between commands
//                 // But this matches your request literally
//                 contentToWrite = previousContent;
//                 targetPathStr = clipboardAsPath;
//             }

//             Path targetPath = Paths.get(targetPathStr);

//             // Create parent directories if needed
//             if (targetPath.getParent() != null) {
//                 Files.createDirectories(targetPath.getParent());
//             }

//             // Write
//             Files.writeString(targetPath, contentToWrite);

//             System.out.printf("Successfully wrote %d characters to: %s%n",
//                     contentToWrite.length(), targetPath.toAbsolutePath());

//         } catch (HeadlessException e) {
//             System.err.println("Error: Clipboard access not available (headless environment?).");
//             System.exit(1);
//         } catch (UnsupportedFlavorException | IOException e) {
//             System.err.println("Error accessing clipboard or file: " + e.getMessage());
//             System.exit(1);
//         } catch (Exception e) {
//             System.err.println("Unexpected error: " + e.getMessage());
//             System.exit(1);
//         }
//     }

//     private String getClipboardText(Clipboard clipboard)
//             throws UnsupportedFlavorException, IOException {
//         return (String) clipboard.getData(DataFlavor.stringFlavor);
//     }
// }