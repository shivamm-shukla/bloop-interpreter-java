# Bloop Interpreter — Java

> A tree-walking interpreter for **Bloop**, a simple, indentation-based programming language — built in Java.

---

## What is Bloop?

Bloop is a beginner-friendly programming language designed to be readable and minimal. It uses plain English-style syntax and indentation to define code blocks — no curly braces, no semicolons.

```
put 5 into count
repeat count times:
    print "Hello, Bloop!"
```

---

## Features

- **Variables** — `put <value> into <name>`
- **Arithmetic** — `+`, `-`, `*`, `/` with correct operator precedence
- **Comparisons** — `==`, `!=`, `>`, `>=`, `<`, `<=`
- **Print** — `print <expression>`
- **If / Else** — indentation-based conditional blocks
- **Repeat loop** — fixed-count iteration with expression support
- **String literals** — with escape sequences (`\n`, `\t`, `\\`, `\"`)
- **Meaningful error messages** — Lexer, Parse, and Runtime errors with line numbers

---

## Project Structure

```
bloop-interpreter-java/
├── src/
│   └── bloop/
│       ├── ast/                  # Expression nodes (NumberNode, StringNode, etc.)
│       ├── exceptions/           # BloopException hierarchy
│       ├── instruction/          # Instruction nodes (Assign, Print, If, Repeat)
│       ├── interpreter/          # Interpreter entry point
│       ├── lexer/                # Tokenizer helpers (cursor, registries, indentation)
│       ├── parser/               # Parser, statement parsers, expression parser
│       ├── runtime/              # Environment (variable store)
│       ├── token/                # Token, TokenType, Tokenizer
│       └── Main.java             # Program entry point
├── tests/
│   └── bloop/
│       ├── ast/                  # Unit tests for AST nodes
│       ├── instruction/          # Unit tests for instructions
│       ├── interpreter/          # End-to-end tests
│       ├── parser/               # Parser and expression parser tests
│       ├── runtime/              # Environment tests
│       └── token/                # Tokenizer tests
├── docs/
│   ├── Architecture.md           # System design and component breakdown
│   ├── DesignPatterns.md         # Design patterns used in the codebase
│   ├── LanguageSpec.md           # Formal language specification
│   └── UserGuide.md              # How to write Bloop programs
├── lib/                          # jUnit libraries for running tests
│   
└── README.md
│   
└── .gitignore                    
```

---

## Getting Started

### Prerequisites

- Java 17 or higher
- IntelliJ IDEA (recommended) or any Java IDE

### Running a Bloop Program

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-org>/bloop-interpreter-java.git
   cd bloop-interpreter-java
   ```

2. Open the project in your IDE and build it.

### Option 1: Using CLI (Recommended)

1. Compile the project:

```bash
javac -d out $(find src -name "*.java")
```

2. Run a Bloop program:

```bash
java -cp out bloop.Main examples/program1.bloop
```

3. Run multiple programs:

```bash
java -cp out bloop.Main examples/program1.bloop examples/program2.bloop
```

### Option 2: Using Interpreter (Inside Java Code)
You can directly execute Bloop source code using the Interpreter class:

```bash
java -cp out bloop.Main examples/program1.bloop examples/program2.bloop
```
#### Note: This must be written inside a Java class (e.g., Main.java) and NOT in the terminal.

---

## Running Tests

All tests are in the `tests/` directory and follow the same package structure as `src/`.

### Using IntelliJ IDEA (Recommended)
* Right-click on the tests/ folder
* Click Run 'All Tests'

### Using Command Line (with JUnit JARs)
This project uses JUnit 5 libraries located in the lib/ directory.

**1. Compile source + test files:**

```bash
javac -d out -cp "lib/*" $(find src -name "*.java") $(find tests -name "*.java")
```
**2. Run tests:**

```bash
javac -d out -cp "lib/*" $(find src -name "*.java") $(find tests -name "*.java")
```
###### On Windows (CMD), replace : with ;
```bash
javac -d out -cp "lib/*" $(find src -name "*.java") $(find tests -name "*.java")
```


The test suite includes unit tests for every component and end-to-end tests in `E2ETest.java`.

---

## Git Workflow

This project follows a structured branching strategy:

```
main
└── dev
    ├── feature/tokenizer
    ├── feature/parser
    ├── feature/evaluator
    └── integration
```

| Branch | Purpose |
|---|---|
| `main` | Stable, production-ready code. Requires **2 approvals** to merge. |
| `dev` | Active development. Requires **1 approval** to merge. |
| `feature/tokenizer` | Lexer and tokenization work |
| `feature/parser` | Parser and AST construction |
| `feature/evaluator` | Instruction execution and runtime |
| `integration` | Cross-component integration and integration testing |

**Merge flow:** `feature/*` → `dev` → `main` (via Pull Requests only)

---

## Documentation

| Document | Description |
|---|---|
| [UserGuide.md](docs/UserGuide.md) | How to write programs in Bloop |
| [LanguageSpec.md](docs/LanguageSpec.md) | Formal grammar and language rules |
| [Architecture.md](docs/Architecture.md) | System design, pipeline, and component responsibilities |
| [DesignPatterns.md](docs/DesignPatterns.md) | Design patterns applied in this codebase |

---

## License

This project is for educational purposes.