# Nand2Tetris Part 11 - Jack Compiler

This project implements the Jack Compiler (Code Generator) for Part 11 of the [Nand2Tetris](https://www.nand2tetris.org/) course.  
It translates `.jack` files—written in the Jack programming language—into `.vm` files containing VM code that can be executed on the Hack virtual machine.

## Project Structure
```
11/
├── src/
│   └── jackcompiler/
│       ├── JackCompiler.java       // Main class
│       ├── JackTokenizer.java      // Tokenizes Jack source code
│       ├── CompilationEngine.java  // Parses tokens and generates VM code
│       ├── SymbolTable.java        // Manages variable scope and symbols
│       └── VMWriter.java           // Writes VM commands to output
├── jack/                           // Test .jack files
│   ├── Average/                    // Simple average calculation program
│   ├── ComplexArrays/              // Array manipulation and processing
│   ├── ConvertToBin/               // Binary conversion utility
│   ├── Pong/                       // Classic Pong game implementation
│   ├── Seven/                      // Basic arithmetic operations
│   ├── Square/                     // Square drawing and manipulation
│   └── TetrisFromNand/            // Complete Tetris game implementation
├── out/                           // Output .vm files
└── README.md
```

## Build and Run Instructions

### 1. Compile
From the project root directory, run:
```bash
javac -d out src/jackcompiler/*.java
```
The compiled `.class` files will be placed under the `out/jackcompiler/` directory.

### 2. Execute
```bash
java -cp out jackcompiler.JackCompiler [input_file.jack | directory]
```

Example:
```bash
java -cp out jackcompiler.JackCompiler jack/Main.jack
java -cp out jackcompiler.JackCompiler jack/Square
java -cp out jackcompiler.JackCompiler jack/Pong
java -cp out jackcompiler.JackCompiler jack/TetrisFromNand
```

The generated `.vm` file will be saved alongside the input `.jack` file.

## Features

- **Complete Jack Language Support**: Handles all Jack language constructs including classes, methods, functions, constructors
- **Symbol Table Management**: Tracks variable scope (static, field, argument, local) across class and subroutine levels
- **VM Code Generation**: Translates Jack constructs into efficient VM commands
- **Expression Compilation**: Handles complex expressions with proper operator precedence
- **Control Flow**: Implements if/else statements, while loops with proper label management
- **Object-Oriented Features**: Supports method calls, constructor calls, field access
- **Memory Management**: Proper handling of object allocation and this/that pointers
- **Array Support**: Handles array access and manipulation
- **String Processing**: Manages string constants and operations
- **Error Handling**: Provides meaningful error messages for compilation issues

## VM Code Generation Details

The compiler generates VM code following these conventions:
- **Functions**: `function ClassName.methodName nLocals`
- **Method Calls**: Proper argument pushing and `call` instructions
- **Object Creation**: `Memory.alloc` for constructors with proper `this` pointer setup
- **Array Access**: Address calculation using `add` and `that` segment
- **Control Flow**: Unique label generation for if/else and while constructs

## Example Projects

The `jack/` directory contains various test programs demonstrating different aspects of the Jack language:

### Basic Programs
- **Average/**: Demonstrates input/output and arithmetic operations for calculating averages
- **Seven/**: Simple program showcasing basic Jack syntax and operations
- **ConvertToBin/**: Binary conversion utility demonstrating bit manipulation and string operations

### Array and Data Structure Programs  
- **ComplexArrays/**: Advanced array manipulation including multi-dimensional arrays and complex data processing

### Interactive Games
- **Square/**: Interactive square drawing program with keyboard controls and graphics
- **Pong/**: Classic Pong game implementation with ball physics, paddle movement, and collision detection
- **Tetris Game**: Complete Tetris implementation in `TetrisFromNand/` directory including:
  - **Board.jack**: Game board management and line clearing logic
  - **GameLogic.jack**: Main game loop and piece movement logic  
  - **GameRenderer.jack**: Graphics rendering and ASCII art display
  - **Tetromino.jack**: Tetris piece definitions and rotations

## Testing

To test the compiler with different programs:

```bash
# Simple programs
java -cp out jackcompiler.JackCompiler jack/Seven
java -cp out jackcompiler.JackCompiler jack/Average

# Array processing
java -cp out jackcompiler.JackCompiler jack/ComplexArrays

# Utility programs  
java -cp out jackcompiler.JackCompiler jack/ConvertToBin

# Interactive games
java -cp out jackcompiler.JackCompiler jack/Square
java -cp out jackcompiler.JackCompiler jack/Pong

# Complete game project
java -cp out jackcompiler.JackCompiler jack/TetrisFromNand
```

These will generate VM files for all Jack classes in each project, which can then be run on the VM emulator to test functionality.

## Notes

- The output `.vm` files contain optimized VM code with proper indentation
- Symbol table warnings may appear for undefined variables (handled gracefully)
- Label generation ensures unique labels across compilation units
- String literals are properly escaped and handled in VM code
- The compiler supports both single file and directory processing
- Memory allocation follows the Hack platform conventions