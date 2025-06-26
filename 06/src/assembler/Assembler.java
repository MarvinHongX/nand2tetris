package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * The Assembler class is the main entry point for the assembler program.
 * It coordinates the parsing, translation, and output of assembly code to machine code.
 * It can process either a single .asm file or all .asm files in a directory.
 */
public class Assembler {
    public static void main(String[] args) {
        // Check if an argument is provided
        if (args.length != 1) {
            System.out.println("Usage: java Assembler <input_file.asm | directory>");
            return;
        }

        String inputPath = args[0];
        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            System.err.println("Error: File or directory " + inputPath + " not found.");
            return;
        }

        try {
            if (inputFile.isDirectory()) {
                // Process all .asm files in the directory
                processDirectory(inputFile);
            } else if (inputPath.endsWith(".asm")) {
                // Process single .asm file
                processFile(inputFile);
            } else {
                System.err.println("Error: Input must be a .asm file or a directory containing .asm files.");
            }
        } catch (Exception e) {
            System.err.println("Error during assembly: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Processes all .asm files in a directory.
     */
    private static void processDirectory(File directory) {
        File[] asmFiles = directory.listFiles((dir, name) -> name.endsWith(".asm"));

        if (asmFiles == null || asmFiles.length == 0) {
            System.out.println("No .asm files found in directory: " + directory.getPath());
            return;
        }

        System.out.println("Found " + asmFiles.length + " .asm file(s) in directory: " + directory.getPath());

        // Sort files for consistent processing order
        Arrays.sort(asmFiles);

        for (File asmFile : asmFiles) {
            try {
                System.out.println("Processing: " + asmFile.getName());
                processFile(asmFile);
            } catch (Exception e) {
                System.err.println("Error processing file " + asmFile.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Processes a single .asm file.
     */
    private static void processFile(File inputFile) throws FileNotFoundException {
        String inputFileName = inputFile.getPath();
        String outputFileName = inputFileName.replace(".asm", ".hack");

        // Initialize symbol table
        SymbolTable symbolTable = new SymbolTable();

        // First pass: build the symbol table
        firstPass(inputFile, symbolTable);

        // Second pass: generate machine code
        secondPass(inputFile, symbolTable, outputFileName);

        System.out.println("Assembly completed: " + outputFileName);
    }

    /**
     * First pass: Goes through the entire assembly program, line by line,
     * and builds the symbol table without generating any code.
     */
    private static void firstPass(File inputFile, SymbolTable symbolTable) throws FileNotFoundException {
        Parser parser = new Parser(inputFile);
        int romAddress = 0;

        while (parser.hasMoreCommands()) {
            CommandType commandType = parser.commandType();

            if (commandType == CommandType.L_COMMAND) {
                // Add label to symbol table with current ROM address
                String symbol = parser.symbol();
                symbolTable.addEntry(symbol, romAddress);
            } else {
                // A-instruction or C-instruction: increment ROM address
                romAddress++;
            }

            parser.advance();
        }

        parser.close();
    }

    /**
     * Second pass: Goes through the entire assembly program again, and generates code.
     */
    private static void secondPass(File inputFile, SymbolTable symbolTable, String outputFileName) throws FileNotFoundException {
        Parser parser = new Parser(inputFile);
        Code code = new Code();
        PrintWriter outputFile = new PrintWriter(new File(outputFileName));
        int ramAddress = 16; // Start address for variables

        while (parser.hasMoreCommands()) {
            CommandType commandType = parser.commandType();

            if (commandType == CommandType.A_COMMAND) {
                String symbol = parser.symbol();
                int address;

                // Check if symbol is a number
                if (symbol.matches("\\d+")) {
                    address = Integer.parseInt(symbol);
                } else {
                    // It's a variable or label
                    if (!symbolTable.contains(symbol)) {
                        // New variable: add to symbol table
                        symbolTable.addEntry(symbol, ramAddress);
                        address = ramAddress;
                        ramAddress++;
                    } else {
                        // Known symbol: get its address
                        address = symbolTable.getAddress(symbol);
                    }
                }

                // Generate A-instruction
                String binaryInstruction = code.toBinaryA(address);
                outputFile.println(binaryInstruction);

            } else if (commandType == CommandType.C_COMMAND) {
                // Generate C-instruction
                String dest = parser.dest();
                String comp = parser.comp();
                String jump = parser.jump();

                String binaryInstruction = code.toBinaryC(dest, comp, jump);
                outputFile.println(binaryInstruction);
            }
            // L_COMMAND is ignored in second pass

            parser.advance();
        }

        parser.close();
        outputFile.close();
    }
}