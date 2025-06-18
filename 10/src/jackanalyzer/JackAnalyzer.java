package jackanalyzer;

import java.io.*;
import java.util.*;

public class JackAnalyzer {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java JackAnalyzer <input file or directory>");
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
                    analyzeFile(input);
                } else {
                    System.out.println("Error: Input file must have .jack extension.");
                }
            } else if (inputFile.isDirectory()) {
                // Directory
                analyzeDirectory(input);
            }
        } catch (IOException e) {
            System.out.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void analyzeFile(String jackFileName) throws IOException {
        System.out.println("Analyzing: " + jackFileName);

        String baseName = jackFileName.substring(0, jackFileName.lastIndexOf('.'));
        String xmlFileName = baseName + ".xml";
        String tokensFileName = baseName + "T.xml";

        // Generate tokens XML file
        generateTokensXML(jackFileName, tokensFileName);

        // Generate syntax analysis XML file
        JackTokenizer tokenizer = new JackTokenizer(jackFileName);
        CompilationEngine engine = new CompilationEngine(tokenizer, xmlFileName);

        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }

        engine.compileClass();

        System.out.println("Generated: " + xmlFileName);
        System.out.println("Generated: " + tokensFileName);
    }

    private static void generateTokensXML(String jackFileName, String tokensFileName) throws IOException {
        JackTokenizer tokenizer = new JackTokenizer(jackFileName);
        PrintWriter writer = new PrintWriter(new FileWriter(tokensFileName));

        writer.println("<tokens>");

        while (tokenizer.hasMoreTokens()) {
            tokenizer.advance();

            JackTokenizer.TokenType type = tokenizer.tokenType();
            String value = tokenizer.getCurrentToken();

            // Handle XML special characters
            value = value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");

            switch (type) {
                case KEYWORD:
                    writer.println("<keyword> " + value + " </keyword>");
                    break;
                case SYMBOL:
                    writer.println("<symbol> " + value + " </symbol>");
                    break;
                case IDENTIFIER:
                    writer.println("<identifier> " + value + " </identifier>");
                    break;
                case INT_CONST:
                    writer.println("<integerConstant> " + value + " </integerConstant>");
                    break;
                case STRING_CONST:
                    writer.println("<stringConstant> " + value + " </stringConstant>");
                    break;
            }
        }

        writer.println("</tokens>");
        writer.close();
    }

    private static void analyzeDirectory(String directoryName) throws IOException {
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

        // Analyze each .jack file
        for (String jackFile : jackFiles) {
            analyzeFile(jackFile);
        }

        System.out.println("Processed " + jackFiles.size() + " files.");
    }
}