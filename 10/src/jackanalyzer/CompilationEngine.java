package jackanalyzer;

import java.io.*;

public class CompilationEngine {

    private JackTokenizer tokenizer;
    private PrintWriter writer;
    private int indentLevel;

    public CompilationEngine(JackTokenizer tokenizer, String outputFile) throws IOException {
        this.tokenizer = tokenizer;
        this.writer = new PrintWriter(new FileWriter(outputFile));
        this.indentLevel = 0;
    }

    private void writeOpenTag(String tag) {
        writeIndent();
        writer.println("<" + tag + ">");
        indentLevel++;
    }

    private void writeCloseTag(String tag) {
        indentLevel--;
        writeIndent();
        writer.println("</" + tag + ">");
    }

    private void writeTerminal(String tag, String value) {
        writeIndent();
        // Handle XML special characters
        value = value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
        writer.println("<" + tag + "> " + value + " </" + tag + ">");
    }

    private void writeIndent() {
        for (int i = 0; i < indentLevel; i++) {
            writer.print("  ");
        }
    }

    private void eat(String expected) {
        if (!tokenizer.getCurrentToken().equals(expected)) {
            throw new RuntimeException("Expected '" + expected + "' but got '" + tokenizer.getCurrentToken() + "'");
        }

        JackTokenizer.TokenType type = tokenizer.tokenType();
        String value = tokenizer.getCurrentToken();

        switch (type) {
            case KEYWORD:
                writeTerminal("keyword", value);
                break;
            case SYMBOL:
                writeTerminal("symbol", value);
                break;
            case IDENTIFIER:
                writeTerminal("identifier", value);
                break;
            case INT_CONST:
                writeTerminal("integerConstant", value);
                break;
            case STRING_CONST:
                writeTerminal("stringConstant", value);
                break;
        }

        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }

    private void eatCurrentToken() {
        JackTokenizer.TokenType type = tokenizer.tokenType();
        String value = tokenizer.getCurrentToken();

        switch (type) {
            case KEYWORD:
                writeTerminal("keyword", value);
                break;
            case SYMBOL:
                writeTerminal("symbol", value);
                break;
            case IDENTIFIER:
                writeTerminal("identifier", value);
                break;
            case INT_CONST:
                writeTerminal("integerConstant", value);
                break;
            case STRING_CONST:
                writeTerminal("stringConstant", value);
                break;
        }

        if (tokenizer.hasMoreTokens()) {
            tokenizer.advance();
        }
    }

    public void compileClass() {
        writeOpenTag("class");

        eat("class");
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

        writeCloseTag("class");
        writer.close();
    }

    public void compileClassVarDec() {
        writeOpenTag("classVarDec");

        eatCurrentToken(); // static or field
        eatCurrentToken(); // type
        eatCurrentToken(); // varName

        while (tokenizer.getCurrentToken().equals(",")) {
            eat(",");
            eatCurrentToken(); // varName
        }

        eat(";");

        writeCloseTag("classVarDec");
    }

    public void compileSubroutineDec() {
        writeOpenTag("subroutineDec");

        eatCurrentToken(); // constructor, function, or method
        eatCurrentToken(); // void or type
        eatCurrentToken(); // subroutineName
        eat("(");
        compileParameterList();
        eat(")");
        compileSubroutineBody();

        writeCloseTag("subroutineDec");
    }

    public void compileParameterList() {
        writeOpenTag("parameterList");

        if (!tokenizer.getCurrentToken().equals(")")) {
            eatCurrentToken(); // type
            eatCurrentToken(); // varName

            while (tokenizer.getCurrentToken().equals(",")) {
                eat(",");
                eatCurrentToken(); // type
                eatCurrentToken(); // varName
            }
        }

        writeCloseTag("parameterList");
    }

    public void compileSubroutineBody() {
        writeOpenTag("subroutineBody");

        eat("{");

        while (tokenizer.getCurrentToken().equals("var")) {
            compileVarDec();
        }

        compileStatements();
        eat("}");

        writeCloseTag("subroutineBody");
    }

    public void compileVarDec() {
        writeOpenTag("varDec");

        eat("var");
        eatCurrentToken(); // type
        eatCurrentToken(); // varName

        while (tokenizer.getCurrentToken().equals(",")) {
            eat(",");
            eatCurrentToken(); // varName
        }

        eat(";");

        writeCloseTag("varDec");
    }

    public void compileStatements() {
        writeOpenTag("statements");

        while (tokenizer.getCurrentToken().equals("let") ||
                tokenizer.getCurrentToken().equals("if") ||
                tokenizer.getCurrentToken().equals("while") ||
                tokenizer.getCurrentToken().equals("do") ||
                tokenizer.getCurrentToken().equals("return")) {

            if (tokenizer.getCurrentToken().equals("let")) {
                compileLet();
            } else if (tokenizer.getCurrentToken().equals("if")) {
                compileIf();
            } else if (tokenizer.getCurrentToken().equals("while")) {
                compileWhile();
            } else if (tokenizer.getCurrentToken().equals("do")) {
                compileDo();
            } else if (tokenizer.getCurrentToken().equals("return")) {
                compileReturn();
            }
        }

        writeCloseTag("statements");
    }

    public void compileLet() {
        writeOpenTag("letStatement");

        eat("let");
        eatCurrentToken(); // varName

        if (tokenizer.getCurrentToken().equals("[")) {
            eat("[");
            compileExpression();
            eat("]");
        }

        eat("=");
        compileExpression();
        eat(";");

        writeCloseTag("letStatement");
    }

    public void compileIf() {
        writeOpenTag("ifStatement");

        eat("if");
        eat("(");
        compileExpression();
        eat(")");
        eat("{");
        compileStatements();
        eat("}");

        if (tokenizer.getCurrentToken().equals("else")) {
            eat("else");
            eat("{");
            compileStatements();
            eat("}");
        }

        writeCloseTag("ifStatement");
    }

    public void compileWhile() {
        writeOpenTag("whileStatement");

        eat("while");
        eat("(");
        compileExpression();
        eat(")");
        eat("{");
        compileStatements();
        eat("}");

        writeCloseTag("whileStatement");
    }

    public void compileDo() {
        writeOpenTag("doStatement");

        eat("do");
        eatCurrentToken(); // subroutineName or className or varName

        if (tokenizer.getCurrentToken().equals(".")) {
            eat(".");
            eatCurrentToken(); // subroutineName
        }

        eat("(");
        compileExpressionList();
        eat(")");
        eat(";");

        writeCloseTag("doStatement");
    }

    public void compileReturn() {
        writeOpenTag("returnStatement");

        eat("return");

        if (!tokenizer.getCurrentToken().equals(";")) {
            compileExpression();
        }

        eat(";");

        writeCloseTag("returnStatement");
    }

    public void compileExpression() {
        writeOpenTag("expression");

        compileTerm();

        while (isOperator(tokenizer.getCurrentToken())) {
            eatCurrentToken(); // operator
            compileTerm();
        }

        writeCloseTag("expression");
    }

    public void compileTerm() {
        writeOpenTag("term");

        JackTokenizer.TokenType type = tokenizer.tokenType();

        if (type == JackTokenizer.TokenType.INT_CONST ||
                type == JackTokenizer.TokenType.STRING_CONST ||
                isKeywordConstant(tokenizer.getCurrentToken())) {
            eatCurrentToken();
        } else if (tokenizer.getCurrentToken().equals("(")) {
            eat("(");
            compileExpression();
            eat(")");
        } else if (isUnaryOperator(tokenizer.getCurrentToken())) {
            eatCurrentToken(); // unary operator
            compileTerm();
        } else if (type == JackTokenizer.TokenType.IDENTIFIER) {
            eatCurrentToken(); // identifier

            if (tokenizer.getCurrentToken().equals("[")) {
                eat("[");
                compileExpression();
                eat("]");
            } else if (tokenizer.getCurrentToken().equals("(")) {
                eat("(");
                compileExpressionList();
                eat(")");
            } else if (tokenizer.getCurrentToken().equals(".")) {
                eat(".");
                eatCurrentToken(); // subroutineName
                eat("(");
                compileExpressionList();
                eat(")");
            }
        }

        writeCloseTag("term");
    }

    public void compileExpressionList() {
        writeOpenTag("expressionList");

        if (!tokenizer.getCurrentToken().equals(")")) {
            compileExpression();

            while (tokenizer.getCurrentToken().equals(",")) {
                eat(",");
                compileExpression();
            }
        }

        writeCloseTag("expressionList");
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