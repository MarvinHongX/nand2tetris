package assembler;

/**
 * Enumeration representing the different types of assembly commands.
 */
public enum CommandType {
    /**
     * A-instruction: @Xxx where Xxx is either a symbol or a decimal number
     */
    A_COMMAND,

    /**
     * C-instruction: dest=comp;jump
     */
    C_COMMAND,

    /**
     * Label declaration: (Xxx) where Xxx is a symbol
     */
    L_COMMAND
}