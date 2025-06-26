package assembler;

import java.util.HashMap;

/**
 * The SymbolTable class manages symbols and their memory addresses.
 * It provides methods to add, check, and retrieve addresses for variables and labels.
 */
public class SymbolTable {
    private HashMap<String, Integer> table;

    /**
     * Constructor: Creates a new empty symbol table and initializes it
     * with the predefined symbols.
     */
    public SymbolTable() {
        table = new HashMap<>();

        // Initialize predefined symbols
        // Virtual machine registers
        table.put("SP", 0);
        table.put("LCL", 1);
        table.put("ARG", 2);
        table.put("THIS", 3);
        table.put("THAT", 4);

        // Predefined pointers
        for (int i = 0; i <= 15; i++) {
            table.put("R" + i, i);
        }

        // I/O pointers
        table.put("SCREEN", 16384);
        table.put("KBD", 24576);
    }

    /**
     * Adds the pair (symbol, address) to the table.
     */
    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
    }

    /**
     * Returns true if the symbol table contains the given symbol.
     */
    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    /**
     * Returns the address associated with the symbol.
     */
    public int getAddress(String symbol) {
        Integer address = table.get(symbol);
        if (address == null) {
            throw new IllegalArgumentException("Symbol '" + symbol + "' not found in symbol table");
        }
        return address;
    }
}