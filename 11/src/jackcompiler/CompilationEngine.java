package jackcompiler;

import java.io.*;

public class CompilationEngine {

    private JackTokenizer tokenizer;
    private VMWriter vmWriter;
    private SymbolTable symbolTable;
    private String className;
    private String currentSubroutineName;
    private String currentSubroutineType; // "constructor", "function", or "method"
    private int labelIndex;
    private String labelPrefix; // For generating unique labels
    private boolean isFirstFunction; // Track if this is the first function

    public CompilationEngine(JackTokenizer tokenizer, String outputFile) throws IOException {
        this.tokenizer = tokenizer;
        this.vmWriter = new VMWriter(outputFile);
        this.symbolTable = new SymbolTable();
        this.labelIndex = 0;
        this.isFirstFunction = true;

        // Extract label prefix from output file name
        String fileName = outputFile.substring(outputFile.lastIndexOf('/') + 1);
        if (fileName.lastIndexOf('\\') > 0) {
            fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        }
        this.labelPrefix = fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private void eat(String expected) {
        if (!tokenizer.getCurrentToken().equals(expected)) {
            throw new RuntimeException("Expected '" + expected + "' but got '" + tokenizer.getCurrentToken() + "'");
        }

        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }

    private void eatCurrentToken() {
        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }

    private String getUniqueLabel() {
        return labelPrefix + "_" + (labelIndex++);
    }

    public void compileClass() {
        eat("class");
        className = tokenizer.getCurrentToken(); // className
        eatCurrentToken(); // className
        eat("{");

        // classVarDec*
        while (tokenizer.getCurrentToken().equals("static") || tokenizer.getCurrentToken().equals("field")) {
            compileClassVarDec();
        }

        // subroutineDec*
        while (tokenizer.getCurrentToken().equals("constructor") ||
                tokenizer.getCurrentToken().equals("function") ||
                tokenizer.getCurrentToken().equals("method")) {
            compileSubroutineDec();
        }

        eat("}");
        vmWriter.close();
    }

    public void compileClassVarDec() {
        String kind = tokenizer.getCurrentToken(); // static or field
        eatCurrentToken(); // static or field
        String type = tokenizer.getCurrentToken(); // type
        eatCurrentToken(); // type
        String name = tokenizer.getCurrentToken(); // varName
        eatCurrentToken(); // varName

        SymbolTable.Kind symbolKind = kind.equals("static") ? SymbolTable.Kind.STATIC : SymbolTable.Kind.FIELD;
        symbolTable.define(name, type, symbolKind);

        while (tokenizer.getCurrentToken().equals(",")) {
            eat(",");
            name = tokenizer.getCurrentToken(); // varName
            eatCurrentToken(); // varName
            symbolTable.define(name, type, symbolKind);
        }

        eat(";");
    }

    public void compileSubroutineDec() {
        symbolTable.startSubroutine();

        // Add newline before function if it's not the first one
        if (!isFirstFunction) {
            vmWriter.addNewline();
        }
        isFirstFunction = false;

        currentSubroutineType = tokenizer.getCurrentToken(); // constructor, function, or method
        eatCurrentToken(); // constructor, function, or method
        eatCurrentToken(); // void or type
        currentSubroutineName = tokenizer.getCurrentToken(); // subroutineName
        eatCurrentToken(); // subroutineName

        // For methods, the first argument is always 'this'
        if (currentSubroutineType.equals("method")) {
            symbolTable.define("this", className, SymbolTable.Kind.ARG);
        }

        eat("(");
        compileParameterList();
        eat(")");
        compileSubroutineBody();
    }

    public void compileParameterList() {
        if (!tokenizer.getCurrentToken().equals(")")) {
            String type = tokenizer.getCurrentToken(); // type
            eatCurrentToken(); // type
            String name = tokenizer.getCurrentToken(); // varName
            eatCurrentToken(); // varName

            symbolTable.define(name, type, SymbolTable.Kind.ARG);

            while (tokenizer.getCurrentToken().equals(",")) {
                eat(",");
                type = tokenizer.getCurrentToken(); // type
                eatCurrentToken(); // type
                name = tokenizer.getCurrentToken(); // varName
                eatCurrentToken(); // varName

                symbolTable.define(name, type, SymbolTable.Kind.ARG);
            }
        }
    }

    public void compileSubroutineBody() {
        eat("{");

        // Count local variables first
        int varCount = 0;

        while (tokenizer.getCurrentToken().equals("var")) {
            compileVarDec();
            varCount = symbolTable.varCount(SymbolTable.Kind.VAR);
        }

        // Write function declaration
        vmWriter.writeFunction(className + "." + currentSubroutineName, varCount);

        // Handle constructor/method setup
        if (currentSubroutineType.equals("constructor")) {
            // Allocate memory for the new object
            vmWriter.writePush(VMWriter.Segment.CONST, symbolTable.varCount(SymbolTable.Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0); // anchor this
        } else if (currentSubroutineType.equals("method")) {
            // Set up 'this' pointer
            vmWriter.writePush(VMWriter.Segment.ARG, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 0);
        }

        compileStatements();
        eat("}");
    }

    public void compileVarDec() {
        eat("var");
        String type = tokenizer.getCurrentToken(); // type
        eatCurrentToken(); // type
        String name = tokenizer.getCurrentToken(); // varName
        eatCurrentToken(); // varName

        symbolTable.define(name, type, SymbolTable.Kind.VAR);

        while (tokenizer.getCurrentToken().equals(",")) {
            eat(",");
            name = tokenizer.getCurrentToken(); // varName
            eatCurrentToken(); // varName
            symbolTable.define(name, type, SymbolTable.Kind.VAR);
        }

        eat(";");
    }

    public void compileStatements() {
        while (tokenizer.getCurrentToken().equals("let") ||
                tokenizer.getCurrentToken().equals("if") ||
                tokenizer.getCurrentToken().equals("while") ||
                tokenizer.getCurrentToken().equals("do") ||
                tokenizer.getCurrentToken().equals("return")) {

            String statement = tokenizer.getCurrentToken();

            if (statement.equals("let")) {
                compileLet();
            } else if (statement.equals("if")) {
                compileIf();
            } else if (statement.equals("while")) {
                compileWhile();
            } else if (statement.equals("do")) {
                compileDo();
            } else if (statement.equals("return")) {
                compileReturn();
            }
        }
    }

    public void compileLet() {
        eat("let");
        String varName = tokenizer.getCurrentToken(); // varName
        eatCurrentToken(); // varName

        boolean isArray = false;
        if (tokenizer.getCurrentToken().equals("[")) {
            isArray = true;

            eat("[");
            compileExpression(); // Push index
            eat("]");

            // Push array base address
            pushVariable(varName);

            // Calculate array[index] address
            vmWriter.writeArithmetic(VMWriter.Command.ADD);
        }

        eat("=");
        compileExpression(); // Push value to assign
        eat(";");

        if (isArray) {
            // Pop value to temp, set THAT to address, pop value to THAT 0
            vmWriter.writePop(VMWriter.Segment.TEMP, 0);
            vmWriter.writePop(VMWriter.Segment.POINTER, 1);
            vmWriter.writePush(VMWriter.Segment.TEMP, 0);
            vmWriter.writePop(VMWriter.Segment.THAT, 0);
        } else {
            // Simple variable assignment
            popVariable(varName);
        }
    }

    public void compileIf() {
        eat("if");
        eat("(");
        compileExpression();
        eat(")");

        vmWriter.writeArithmetic(VMWriter.Command.NOT);

        String ifFalseLabel = getUniqueLabel();
        String endLabel = getUniqueLabel();

        vmWriter.writeIf(ifFalseLabel);

        eat("{");
        compileStatements();
        eat("}");

        vmWriter.writeGoto(endLabel);
        vmWriter.writeLabel(ifFalseLabel);

        // Check for else
        if (tokenizer.getCurrentToken().equals("else")) {
            eat("else");
            eat("{");
            compileStatements();
            eat("}");
        }

        vmWriter.writeLabel(endLabel);
    }

    public void compileWhile() {
        String loopLabel = getUniqueLabel();
        String endLabel = getUniqueLabel();

        vmWriter.writeLabel(loopLabel);

        eat("while");
        eat("(");
        compileExpression();
        eat(")");

        vmWriter.writeArithmetic(VMWriter.Command.NOT);
        vmWriter.writeIf(endLabel);

        eat("{");
        compileStatements();
        eat("}");

        vmWriter.writeGoto(loopLabel);
        vmWriter.writeLabel(endLabel);
    }

    public void compileDo() {
        eat("do");
        compileSubroutineCall();
        eat(";");

        // Do statements don't use return value
        vmWriter.writePop(VMWriter.Segment.TEMP, 0);
    }

    public void compileReturn() {
        eat("return");

        if (!tokenizer.getCurrentToken().equals(";")) {
            compileExpression();
        } else {
            // Void function returns 0
            vmWriter.writePush(VMWriter.Segment.CONST, 0);
        }

        eat(";");
        vmWriter.writeReturn();
    }

    public void compileExpression() {
        compileTerm();

        while (isOperator(tokenizer.getCurrentToken())) {
            String op = tokenizer.getCurrentToken();
            eatCurrentToken(); // operator
            compileTerm();

            // Write the operator command
            writeOperator(op);
        }
    }

    public void compileTerm() {
        JackTokenizer.TokenType type = tokenizer.tokenType();

        if (type == JackTokenizer.TokenType.INT_CONST) {
            int value = Integer.parseInt(tokenizer.getCurrentToken());
            vmWriter.writePush(VMWriter.Segment.CONST, value);
            eatCurrentToken();
        } else if (type == JackTokenizer.TokenType.STRING_CONST) {
            String str = tokenizer.getCurrentToken();
            // Create string
            vmWriter.writePush(VMWriter.Segment.CONST, str.length());
            vmWriter.writeCall("String.new", 1);

            for (char c : str.toCharArray()) {
                vmWriter.writePush(VMWriter.Segment.CONST, (int) c);
                vmWriter.writeCall("String.appendChar", 2);
            }
            eatCurrentToken();
        } else if (isKeywordConstant(tokenizer.getCurrentToken())) {
            String keyword = tokenizer.getCurrentToken();
            if (keyword.equals("true")) {
                vmWriter.writePush(VMWriter.Segment.CONST, 1);
                vmWriter.writeArithmetic(VMWriter.Command.NEG);
            } else if (keyword.equals("false") || keyword.equals("null")) {
                vmWriter.writePush(VMWriter.Segment.CONST, 0);
            } else if (keyword.equals("this")) {
                vmWriter.writePush(VMWriter.Segment.POINTER, 0);
            }
            eatCurrentToken();
        } else if (tokenizer.getCurrentToken().equals("(")) {
            eat("(");
            compileExpression();
            eat(")");
        } else if (isUnaryOperator(tokenizer.getCurrentToken())) {
            String op = tokenizer.getCurrentToken();
            eatCurrentToken(); // unary operator
            compileTerm();

            if (op.equals("-")) {
                vmWriter.writeArithmetic(VMWriter.Command.NEG);
            } else if (op.equals("~")) {
                vmWriter.writeArithmetic(VMWriter.Command.NOT);
            }
        } else if (type == JackTokenizer.TokenType.IDENTIFIER) {
            String identifier = tokenizer.getCurrentToken();
            eatCurrentToken(); // identifier

            if (tokenizer.getCurrentToken().equals("[")) {
                // Array access
                eat("[");
                compileExpression(); // Push index
                eat("]");
                pushVariable(identifier); // Push array base address
                vmWriter.writeArithmetic(VMWriter.Command.ADD);
                vmWriter.writePop(VMWriter.Segment.POINTER, 1);
                vmWriter.writePush(VMWriter.Segment.THAT, 0);
            } else if (tokenizer.getCurrentToken().equals("(") || tokenizer.getCurrentToken().equals(".")) {
                // Subroutine call - backtrack
                tokenizer.goBack();
                compileSubroutineCall();
            } else {
                // Simple variable
                pushVariable(identifier);
            }
        }
    }

    private void compileSubroutineCall() {
        String name = tokenizer.getCurrentToken();
        eatCurrentToken();

        int nArgs = 0;
        String functionName;

        if (tokenizer.getCurrentToken().equals(".")) {
            // obj.method() or Class.function()
            eat(".");
            String methodName = tokenizer.getCurrentToken();
            eatCurrentToken();

            // Check if name is a variable (object) or class name
            if (symbolTable.kindOf(name) != null) {
                // It's an object, so it's a method call
                pushVariable(name); // Push object reference
                nArgs = 1;
                functionName = symbolTable.typeOf(name) + "." + methodName;
            } else {
                // It's a class name, so it's a function call
                functionName = name + "." + methodName;
            }
        } else {
            // method() - method of current class
            vmWriter.writePush(VMWriter.Segment.POINTER, 0); // Push this
            nArgs = 1;
            functionName = className + "." + name;
        }

        eat("(");
        nArgs += compileExpressionList();
        eat(")");

        vmWriter.writeCall(functionName, nArgs);
    }

    public int compileExpressionList() {
        int nArgs = 0;

        if (!tokenizer.getCurrentToken().equals(")")) {
            compileExpression();
            nArgs++;

            while (tokenizer.getCurrentToken().equals(",")) {
                eat(",");
                compileExpression();
                nArgs++;
            }
        }

        return nArgs;
    }

    private void pushVariable(String name) {
        SymbolTable.Kind kind = symbolTable.kindOf(name);

        if (kind == null) {
            // Variable not found in symbol table - might be a class name or undeclared variable
            System.out.println("Warning: Variable '" + name + "' not found in symbol table");
            // For now, just push 0 as a placeholder
            vmWriter.writePush(VMWriter.Segment.CONST, 0);
            return;
        }

        int index = symbolTable.indexOf(name);

        switch (kind) {
            case STATIC:
                vmWriter.writePush(VMWriter.Segment.STATIC, index);
                break;
            case FIELD:
                vmWriter.writePush(VMWriter.Segment.THIS, index);
                break;
            case ARG:
                vmWriter.writePush(VMWriter.Segment.ARG, index);
                break;
            case VAR:
                vmWriter.writePush(VMWriter.Segment.LOCAL, index);
                break;
        }
    }

    private void popVariable(String name) {
        SymbolTable.Kind kind = symbolTable.kindOf(name);

        if (kind == null) {
            // Variable not found in symbol table
            System.out.println("Warning: Variable '" + name + "' not found in symbol table for pop operation");
            // For now, just pop to temp
            vmWriter.writePop(VMWriter.Segment.TEMP, 0);
            return;
        }

        int index = symbolTable.indexOf(name);

        switch (kind) {
            case STATIC:
                vmWriter.writePop(VMWriter.Segment.STATIC, index);
                break;
            case FIELD:
                vmWriter.writePop(VMWriter.Segment.THIS, index);
                break;
            case ARG:
                vmWriter.writePop(VMWriter.Segment.ARG, index);
                break;
            case VAR:
                vmWriter.writePop(VMWriter.Segment.LOCAL, index);
                break;
        }
    }

    private void writeOperator(String op) {
        switch (op) {
            case "+":
                vmWriter.writeArithmetic(VMWriter.Command.ADD);
                break;
            case "-":
                vmWriter.writeArithmetic(VMWriter.Command.SUB);
                break;
            case "*":
                vmWriter.writeCall("Math.multiply", 2);
                break;
            case "/":
                vmWriter.writeCall("Math.divide", 2);
                break;
            case "&":
                vmWriter.writeArithmetic(VMWriter.Command.AND);
                break;
            case "|":
                vmWriter.writeArithmetic(VMWriter.Command.OR);
                break;
            case "<":
                vmWriter.writeArithmetic(VMWriter.Command.LT);
                break;
            case ">":
                vmWriter.writeArithmetic(VMWriter.Command.GT);
                break;
            case "=":
                vmWriter.writeArithmetic(VMWriter.Command.EQ);
                break;
        }
    }

    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") ||
                token.equals("/") || token.equals("&") || token.equals("|") ||
                token.equals("<") || token.equals(">") || token.equals("=");
    }

    private boolean isUnaryOperator(String token) {
        return token.equals("-") || token.equals("~");
    }

    private boolean isKeywordConstant(String token) {
        return token.equals("true") || token.equals("false") ||
                token.equals("null") || token.equals("this");
    }
}