package com.tool.app;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
// import javax.xml.bind.DatatypeConverter;

@Command(
    name = "hash",
    description = "Compute the hash of a string or file content."
)
public class HashCommand implements Runnable {

    @Option(names = {"-t", "--type"}, description = "Hash type: MD5, SHA-1, SHA-256 (default: SHA-256)", defaultValue = "SHA-256")
    private String hashType;

    @Parameters(index = "0", description = "String or path to file to hash.")
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

            MessageDigest md = MessageDigest.getInstance(hashType);
            md.update(bytes);
            byte[] digest = md.digest();
            // String hash = DatatypeConverter.printHexBinary(digest).toLowerCase();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String hash = sb.toString();

            System.out.println(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Invalid hash type: " + hashType);
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
