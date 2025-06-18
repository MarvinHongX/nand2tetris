# Nand2Tetris Part 10 - Jack Analyzer
This project implements the Jack Analyzer (Syntax Analyzer) for Part 10 of the [Nand2Tetris](https://www.nand2tetris.org/) course.  
It translates `.jack` files—written in the Jack programming language—into XML files representing the program's syntax structure.

## Project Structure
```
10/
├── src/
│   └── jackanalyzer/
│       ├── JackAnalyzer.java      // Main class
│       ├── JackTokenizer.java     // Tokenizes Jack source code
│       └── CompilationEngine.java // Parses tokens into XML syntax tree
├── jack/                          // Test .jack files
├── out/                           // Output .xml files
└── README.md
```

## Build and Run Instructions
### 1. Compile
From the project root directory, run:
```bash
javac -d out src/jackanalyzer/*.java
```
The compiled `.class` files will be placed under the `out/jackanalyzer/` directory.

### 2. Execute
```bash
java -cp out jackanalyzer.JackAnalyzer [input_file.jack | directory]
```
Example:
```bash
java -cp out jackanalyzer.JackAnalyzer jack/Square.jack
java -cp out jackanalyzer.JackAnalyzer jack/SquareGame
```
The generated `.xml` file will be saved alongside the input `.jack` file.

## Features
- Tokenizes Jack source code into keywords, symbols, identifiers, constants
- Parses complete Jack syntax including classes, methods, statements, expressions
- Generates properly formatted XML syntax trees
- Handles comments and string literals correctly
- Supports both single file and directory processing

## Notes
- The output `.xml` file contains the complete syntax tree structure with proper indentation
- XML special characters (`<`, `>`, `&`, `"`) are properly escaped in the output