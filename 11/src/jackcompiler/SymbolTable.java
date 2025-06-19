package jackcompiler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public enum Kind {
        STATIC, FIELD, ARG, VAR
    }

    private static class Symbol {
        String type;
        Kind kind;
        int index;

        Symbol(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
    }

    private Map<String, Symbol> classTable;
    private Map<String, Symbol> subroutineTable;
    private int staticCount;
    private int fieldCount;
    private int argCount;
    private int varCount;

    // Constructor: creates a new empty Symbol Table
    public SymbolTable() {
        classTable = new HashMap<>();
        subroutineTable = new HashMap<>();
        staticCount = 0;
        fieldCount = 0;
        argCount = 0;
        varCount = 0;
    }

    // Starts a new subroutine scope (resets the subroutine's symbol table)
    public void startSubroutine() {
        subroutineTable.clear();
        argCount = 0;
        varCount = 0;
    }

    // Defines a new identifier of a given name, type, and kind and assigns it a running index
    public void define(String name, String type, Kind kind) {
        switch (kind) {
            case STATIC:
                classTable.put(name, new Symbol(type, kind, staticCount++));
                break;
            case FIELD:
                classTable.put(name, new Symbol(type, kind, fieldCount++));
                break;
            case ARG:
                subroutineTable.put(name, new Symbol(type, kind, argCount++));
                break;
            case VAR:
                subroutineTable.put(name, new Symbol(type, kind, varCount++));
                break;
        }
    }

    // Returns the number of variables of the given kind already defined in the current scope
    public int varCount(Kind kind) {
        switch (kind) {
            case STATIC: return staticCount;
            case FIELD: return fieldCount;
            case ARG: return argCount;
            case VAR: return varCount;
            default: return 0;
        }
    }

    // Returns the kind of the named identifier in the current scope
    public Kind kindOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).kind;
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).kind;
        }
        return null; // not found
    }

    // Returns the type of the named identifier in the current scope
    public String typeOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).type;
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).type;
        }
        return null; // not found
    }

    // Returns the index assigned to the named identifier
    public int indexOf(String name) {
        if (subroutineTable.containsKey(name)) {
            return subroutineTable.get(name).index;
        } else if (classTable.containsKey(name)) {
            return classTable.get(name).index;
        }
        return -1; // not found
    }
}