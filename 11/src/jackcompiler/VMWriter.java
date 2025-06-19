package jackcompiler;

import java.io.*;

public class VMWriter {

    public enum Segment {
        CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP
    }

    public enum Command {
        ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT
    }

    private PrintWriter writer;
    private boolean inFunction;

    // Constructor: creates a new file and prepares it for writing
    public VMWriter(String outputFile) throws IOException {
        writer = new PrintWriter(new FileWriter(outputFile));
        inFunction = false;
    }

    // Writes a VM push command
    public void writePush(Segment segment, int index) {
        writeWithIndent("push " + segmentToString(segment) + " " + index);
    }

    // Writes a VM pop command
    public void writePop(Segment segment, int index) {
        writeWithIndent("pop " + segmentToString(segment) + " " + index);
    }

    // Writes a VM arithmetic command
    public void writeArithmetic(Command command) {
        writeWithIndent(commandToString(command));
    }

    // Writes a VM label command
    public void writeLabel(String label) {
        writer.print("\nlabel " + label);  // Labels don't get indented
    }

    // Writes a VM goto command
    public void writeGoto(String label) {
        writeWithIndent("goto " + label);
    }

    // Writes a VM if-goto command
    public void writeIf(String label) {
        writeWithIndent("if-goto " + label);
    }

    // Writes a VM call command
    public void writeCall(String name, int nArgs) {
        writeWithIndent("call " + name + " " + nArgs);
    }

    // Writes a VM function command
    public void writeFunction(String name, int nLocals) {
        writer.print("function " + name + " " + nLocals);
        inFunction = true;
    }

    // Writes a VM return command
    public void writeReturn() {
        writeWithIndent("return");
    }

    // Adds a newline (for separating functions)
    public void addNewline() {
        writer.print("\n");
    }

    // Helper method to write with proper indentation
    private void writeWithIndent(String command) {
        if (inFunction) {
            writer.print("\n    " + command);
        } else {
            writer.print("\n" + command);
        }
    }

    // Closes the output file
    public void close() {
        writer.close();
    }

    private String segmentToString(Segment segment) {
        switch (segment) {
            case CONST: return "constant";
            case ARG: return "argument";
            case LOCAL: return "local";
            case STATIC: return "static";
            case THIS: return "this";
            case THAT: return "that";
            case POINTER: return "pointer";
            case TEMP: return "temp";
            default: return "";
        }
    }

    private String commandToString(Command command) {
        switch (command) {
            case ADD: return "add";
            case SUB: return "sub";
            case NEG: return "neg";
            case EQ: return "eq";
            case GT: return "gt";
            case LT: return "lt";
            case AND: return "and";
            case OR: return "or";
            case NOT: return "not";
            default: return "";
        }
    }
}