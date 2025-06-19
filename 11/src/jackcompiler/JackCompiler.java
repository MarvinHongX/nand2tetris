package jackcompiler;

import java.io.*;
import java.util.*;

public class JackCompiler {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java JackCompiler <input file or directory>");
            System.exit(1);
        }

        String input = args[0];
        File inputFile = new File(input);

        if (!inputFile.exists()) {
            System.out.println("Error: Input file or directory does not exist.");
            System.exit(1);
        }

        try {
            if (inputFile.isFile()) {
                // Single file
                if (input.endsWith(".jack")) {
                    compileFile(input);
                } else {
                    System.out.println("Error: Input file must have .jack extension.");
                }
            } else if (inputFile.isDirectory()) {
                // Directory
                compileDirectory(input);
            }
        } catch (IOException e) {
            System.out.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void compileFile(String jackFileName) throws IOException {
        System.out.println("Compiling: " + jackFileName);

        String baseName = jackFileName.substring(0, jackFileName.lastIndexOf('.'));
        String vmFileName = baseName + ".vm";

        // Create tokenizer and compilation engine
        JackTokenizer tokenizer = new JackTokenizer(jackFileName);
        CompilationEngine engine = new CompilationEngine(tokenizer, vmFileName);

        // Start compilation
        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }

        engine.compileClass();

        System.out.println("Generated: " + vmFileName);
    }

    private static void compileDirectory(String directoryName) throws IOException {
        File directory = new File(directoryName);
        File[] files = directory.listFiles();

        if (files == null) {
            System.out.println("Error: Cannot read directory contents.");
            return;
        }

        List<String> jackFiles = new ArrayList<>();

        // Find all .jack files
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".jack")) {
                jackFiles.add(file.getAbsolutePath());
            }
        }

        if (jackFiles.isEmpty()) {
            System.out.println("No .jack files found in directory: " + directoryName);
            return;
        }

        // Compile each .jack file
        for (String jackFile : jackFiles) {
            compileFile(jackFile);
        }

        System.out.println("Compiled " + jackFiles.size() + " files.");
    }
}