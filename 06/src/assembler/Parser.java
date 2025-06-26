package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * The Parser class reads and parses the input assembly file.
 * It provides methods to navigate through commands and extract their components.
 */
public class Parser {
    private Scanner scanner;
    private String currentCommand;
    private boolean hasNext;

    /**
     * Constructor to initialize the parser with the input file.
     */
    public Parser(File inputFile) throws FileNotFoundException {
        this.scanner = new Scanner(inputFile);
        this.hasNext = false;
        advance(); // Load the first command
    }

    /**
     * Returns true if there are more commands in the input.
     */
    public boolean hasMoreCommands() {
        return hasNext;
    }

    /**
     * Reads the next command from the input and makes it the current command.
     * Should be called only if hasMoreCommands() is true.
     * Initially there is no current command.
     */
    public void advance() {
        currentCommand = null;
        hasNext = false;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            // Remove inline comments
            int commentIndex = line.indexOf("//");
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex).trim();
            }

            if (!line.isEmpty()) {
                currentCommand = line;
                hasNext = true;
                return;
            }
        }
    }

    /**
     * Returns the type of the current command:
     * A_COMMAND for @Xxx where Xxx is either a symbol or a decimal number
     * C_COMMAND for dest=comp;jump
     * L_COMMAND for (Xxx) where Xxx is a symbol
     */
    public CommandType commandType() {
        if (currentCommand == null) {
            throw new IllegalStateException("No current command");
        }

        if (currentCommand.startsWith("@")) {
            return CommandType.A_COMMAND;
        } else if (currentCommand.startsWith("(") && currentCommand.endsWith(")")) {
            return CommandType.L_COMMAND;
        } else {
            return CommandType.C_COMMAND;
        }
    }

    /**
     * Returns the symbol or decimal Xxx of the current command @Xxx or (Xxx).
     * Should be called only when commandType() is A_COMMAND or L_COMMAND.
     */
    public String symbol() {
        CommandType type = commandType();
        if (type == CommandType.A_COMMAND) {
            return currentCommand.substring(1); // Remove '@'
        } else if (type == CommandType.L_COMMAND) {
            return currentCommand.substring(1, currentCommand.length() - 1); // Remove '(' and ')'
        } else {
            throw new IllegalStateException("symbol() should only be called for A_COMMAND or L_COMMAND");
        }
    }

    /**
     * Returns the dest mnemonic in the current C-command (8 possibilities).
     * Should be called only when commandType() is C_COMMAND.
     */
    public String dest() {
        if (commandType() != CommandType.C_COMMAND) {
            throw new IllegalStateException("dest() should only be called for C_COMMAND");
        }

        if (currentCommand.contains("=")) {
            return currentCommand.split("=")[0];
        } else {
            return null; // No destination
        }
    }

    /**
     * Returns the comp mnemonic in the current C-command (28 possibilities).
     * Should be called only when commandType() is C_COMMAND.
     */
    public String comp() {
        if (commandType() != CommandType.C_COMMAND) {
            throw new IllegalStateException("comp() should only be called for C_COMMAND");
        }

        String compPart = currentCommand;

        // Remove dest part if exists
        if (compPart.contains("=")) {
            compPart = compPart.split("=")[1];
        }

        // Remove jump part if exists
        if (compPart.contains(";")) {
            compPart = compPart.split(";")[0];
        }

        return compPart;
    }

    /**
     * Returns the jump mnemonic in the current C-command (8 possibilities).
     * Should be called only when commandType() is C_COMMAND.
     */
    public String jump() {
        if (commandType() != CommandType.C_COMMAND) {
            throw new IllegalStateException("jump() should only be called for C_COMMAND");
        }

        if (currentCommand.contains(";")) {
            String[] parts = currentCommand.split(";");
            return parts[parts.length - 1]; // Get the last part after ';'
        } else {
            return null; // No jump
        }
    }

    /**
     * Closes the scanner.
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }


}