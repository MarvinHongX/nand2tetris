package jackanalyzer;

import java.io.*;
import java.util.*;

public class JackTokenizer {

    public enum TokenType {
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
    }

    public enum KeyWord {
        CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC,
        FIELD, LET, DO, IF, ELSE, WHILE, RETURN, TRUE, FALSE, NULL, THIS
    }

    private String input;
    private String[] tokens;
    private boolean[] isStringConstantArray;
    private int currentToken;
    private String currentTokenValue;
    private boolean isStringConstant;

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "class", "constructor", "function", "method", "field", "static", "var",
            "int", "char", "boolean", "void", "true", "false", "null", "this",
            "let", "do", "if", "else", "while", "return"
    ));

    private static final Set<Character> SYMBOLS = new HashSet<>(Arrays.asList(
            '{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&',
            '|', '<', '>', '=', '~'
    ));

    public JackTokenizer(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String line;

        // Read file and remove comments
        while ((line = reader.readLine()) != null) {
            sb.append(removeComments(line)).append(" ");
        }
        reader.close();

        input = removeBlockComments(sb.toString());
        tokenize();
        currentToken = -1;
    }

    private String removeComments(String line) {
        // Remove // comments
        int doubleSlash = line.indexOf("//");
        if (doubleSlash != -1) {
            line = line.substring(0, doubleSlash);
        }
        return line.trim();
    }

    private String removeBlockComments(String text) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < text.length()) {
            // Check for start of block comment
            if (i < text.length() - 1 && text.charAt(i) == '/' && text.charAt(i + 1) == '*') {
                // Skip until end of block comment
                i += 2;
                while (i < text.length() - 1) {
                    if (text.charAt(i) == '*' && text.charAt(i + 1) == '/') {
                        i += 2;
                        break;
                    }
                    i++;
                }
            } else {
                result.append(text.charAt(i));
                i++;
            }
        }

        return result.toString();
    }

    private void tokenize() {
        List<String> tokenList = new ArrayList<>();
        List<Boolean> stringConstList = new ArrayList<>();
        int i = 0;

        while (i < input.length()) {
            char c = input.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // String constant
            if (c == '"') {
                int start = i + 1;
                i++;
                while (i < input.length() && input.charAt(i) != '"') {
                    i++;
                }
                tokenList.add(input.substring(start, i));
                stringConstList.add(true); // Mark as string constant
                i++; // Skip closing quote
            }
            // Symbol
            else if (SYMBOLS.contains(c)) {
                tokenList.add(String.valueOf(c));
                stringConstList.add(false);
                i++;
            }
            // Number or identifier/keyword
            else {
                int start = i;
                while (i < input.length() && !Character.isWhitespace(input.charAt(i))
                        && !SYMBOLS.contains(input.charAt(i))) {
                    i++;
                }
                String token = input.substring(start, i);
                if (!token.isEmpty()) {
                    tokenList.add(token);
                    stringConstList.add(false);
                }
            }
        }

        tokens = tokenList.toArray(new String[0]);
        isStringConstantArray = new boolean[stringConstList.size()];
        for (int j = 0; j < stringConstList.size(); j++) {
            isStringConstantArray[j] = stringConstList.get(j);
        }
    }

    public boolean hasMoreTokens() {
        return currentToken + 1 < tokens.length;
    }

    public void advance() {
        if (hasMoreTokens()) {
            currentToken++;
            currentTokenValue = tokens[currentToken];
            isStringConstant = isStringConstantArray[currentToken];
        }
    }

    public TokenType tokenType() {
        if (isStringConstant) {
            return TokenType.STRING_CONST;
        } else if (KEYWORDS.contains(currentTokenValue)) {
            return TokenType.KEYWORD;
        } else if (currentTokenValue.length() == 1 && SYMBOLS.contains(currentTokenValue.charAt(0))) {
            return TokenType.SYMBOL;
        } else if (currentTokenValue.matches("\\d+")) {
            return TokenType.INT_CONST;
        } else {
            return TokenType.IDENTIFIER;
        }
    }

    public KeyWord keyWord() {
        return KeyWord.valueOf(currentTokenValue.toUpperCase());
    }

    public char symbol() {
        return currentTokenValue.charAt(0);
    }

    public String identifier() {
        return currentTokenValue;
    }

    public int intVal() {
        return Integer.parseInt(currentTokenValue);
    }

    public String stringVal() {
        return currentTokenValue;
    }

    public String getCurrentToken() {
        return currentTokenValue;
    }
}