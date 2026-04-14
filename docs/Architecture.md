# Architecture

> Internal design of the Bloop Interpreter — how source code becomes output.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Three-Stage Pipeline](#2-three-stage-pipeline)
3. [Package Breakdown](#3-package-breakdown)
4. [Stage 1 — Tokenizer](#4-stage-1--tokenizer)
5. [Stage 2 — Parser](#5-stage-2--parser)
6. [Stage 3 — Execution](#6-stage-3--execution)
7. [Error Handling](#7-error-handling)
8. [Extension Points](#8-extension-points)

---

## 1. Overview

The Bloop interpreter is a **tree-walking interpreter** implemented in Java. It processes source code in three sequential stages — tokenization, parsing, and execution — each completely separated from the others. No stage knows about the internals of the stage before or after it.

The entry point is `Interpreter.run(String sourceCode)`, which orchestrates all three stages and handles top-level error reporting.

---

## 2. Three-Stage Pipeline

```
Source Code (String)
       │
       ▼
┌─────────────────────────────────────┐
│  Stage 1 — Tokenizer                │
│  Tokenizer + LexerCursor +          │
│  IndentationHandler + Registries    │
└─────────────────────────────────────┘
       │
       ▼  List<Token>
┌─────────────────────────────────────┐
│  Stage 2 — Parser                   │
│  Parser + TokenCursor +             │
│  ExpressionParser + StatementParsers│
└─────────────────────────────────────┘
       │
       ▼  List<Instruction>
┌─────────────────────────────────────┐
│  Stage 3 — Execute                  │
│  Instruction.execute(Environment)   │
└─────────────────────────────────────┘
       │
       ▼  Output / RuntimeException
```

Each stage produces a clean data structure that the next stage consumes. The stages share no mutable state.

---

## 3. Package Breakdown

| Package | Responsibility |
|---|---|
| `bloop.token` | `Token` record, `TokenType` enum, `Tokenizer` |
| `bloop.lexer` | Tokenizer helpers — `LexerCursor`, `IndentationHandler`, `KeywordRegistry`, `OperatorRegistry` |
| `bloop.ast` | Expression nodes — `Expression` interface, `NumberNode`, `StringNode`, `VariableNode`, `BinaryOpNode` |
| `bloop.parser` | Top-level `Parser`, orchestrates statement and expression parsing |
| `bloop.parser.cursor` | `TokenCursor` — stateful token navigation |
| `bloop.parser.expression` | `ExpressionParser` — recursive-descent expression parsing |
| `bloop.parser.statement` | `StatementParser` interface, `BlockParser`, `StatementParserRegistry`, and all statement parser implementations |
| `bloop.instruction` | `Instruction` interface, `AssignInstruction`, `PrintInstruction`, `IfInstruction`, `RepeatInstruction` |
| `bloop.runtime` | `Environment` — variable storage (name → value map) |
| `bloop.interpreter` | `Interpreter` — entry point, owns the pipeline |
| `bloop.exceptions` | `BloopException` hierarchy — Lexer, Parse, and Runtime exceptions |

---

## 4. Stage 1 — Tokenizer

**Entry:** `Tokenizer.tokenize()`  
**Output:** `List<Token>` (immutable)

The tokenizer converts raw source text into a flat, ordered list of tokens. Each `Token` is a Java record holding a `TokenType`, a string `value`, and a `line` number.

**Key responsibilities:**

`LexerCursor` manages the current position in the source string and provides character-level read operations (`currentChar()`, `peekNextChar()`, `advance()`).

`IndentationHandler` maintains a stack of indentation levels. On every new non-blank line it compares the current indentation against the stack and emits `INDENT` or `DEDENT` tokens as needed. This makes the grammar aware of block structure without the parser needing to count spaces.

`KeywordRegistry` maps reserved words (`put`, `into`, `if`, `then`, `else`, `repeat`, `times`, `print`) to their `TokenType`. Any word not in the map becomes an `IDENTIFIER`.

`OperatorRegistry` maps single characters and two-character compound operators to their token types. It distinguishes between `>` and `>=`, `=` and `==`, and so on.

**Token types produced:**

- Literals: `NUMBER`, `STRING`, `IDENTIFIER`
- Keywords: `PUT`, `INTO`, `PRINT`, `IF`, `THEN`, `ELSE`, `REPEAT`, `TIMES`
- Operators: `PLUS`, `MINUS`, `STAR`, `SLASH`, `EQUAL_EQUAL`, `NOT_EQUAL`, `GREATER`, `GREATER_EQUAL`, `LESS`, `LESS_EQUAL`
- Symbols: `LEFT_PAREN`, `RIGHT_PAREN`, `COLON`, `COMMA`
- Layout: `NEWLINE`, `INDENT`, `DEDENT`
- Sentinel: `EOF`

---

## 5. Stage 2 — Parser

**Entry:** `Parser.parse(List<Token>)`  
**Output:** `List<Instruction>` (immutable)

The parser consumes the token list and builds an Abstract Syntax Tree (AST) represented as a flat list of top-level `Instruction` objects. Each instruction may recursively contain `Expression` nodes and nested instruction lists (for blocks).

**Key components:**

`TokenCursor` wraps the token list with a current-index pointer. It provides `consume()`, `expect()`, `tryConsume()`, `check()`, and `skipNewlines()` — the entire parser communicates with the token stream only through this cursor.

`ExpressionParser` implements a classic **recursive-descent parser** with four precedence layers: comparison (lowest) → addition/subtraction → multiplication/division → primary (highest). Unary minus and parenthesised expressions are handled at the primary layer.

`StatementParserRegistry` is a map from `TokenType` to `StatementParser`. When the parser encounters a token, it looks up the registered handler. This makes adding new statement types a matter of implementing `StatementParser` and registering it — the `Parser` itself never changes.

`BlockParser` is a `@FunctionalInterface` passed to every `StatementParser.parse()` call. It encapsulates the logic for parsing an `INDENT`-delimited block, so statement parsers do not need to know how blocks work.

**Statement parsers:**

| Class | Trigger token | Produces |
|---|---|---|
| `PutStatementParser` | `PUT` | `AssignInstruction` |
| `PrintStatementParser` | `PRINT` | `PrintInstruction` |
| `IfStatementParser` | `IF` | `IfInstruction` (with optional else) |
| `RepeatStatementParser` | `REPEAT` | `RepeatInstruction` |

---

## 6. Stage 3 — Execution

**Entry:** `Instruction.execute(Environment)`  
**No return value** — instructions produce side effects (output, variable mutation)

Each `Instruction` node calls `execute(env)` on itself. Instructions that contain nested blocks call `execute(env)` recursively on each nested instruction. `Expression` nodes are evaluated by `Expression.evaluate(env)`, which returns an `Object` (`Double` or `String`).

`Environment` is a `HashMap<String, Object>` wrapped in a class. It provides `set(name, value)` and `get(name)`. Accessing an undefined variable immediately throws a `BloopRuntimeException`.

**Execution behaviour by instruction type:**

`AssignInstruction` evaluates its expression and calls `env.set()`.

`PrintInstruction` evaluates its expression, formats the result (whole-number `Double` values are printed without a decimal point), and writes to `System.out`.

`IfInstruction` evaluates its condition expression, asserts the result is a `Boolean`, and executes either the then-body or the else-body.

`RepeatInstruction` evaluates its count expression, validates it is a non-negative whole number within `Integer.MAX_VALUE`, and iterates the body that many times.

---

## 7. Error Handling

All interpreter exceptions extend `BloopException`, which stores an optional source line number and formats it into the message automatically.

| Exception class | When thrown |
|---|---|
| `BloopLexerException` | Unrecognized character, unterminated string, malformed number, bad indentation |
| `BloopParseException` | Unexpected token, missing keyword, empty block |
| `BloopRuntimeException` | Undefined variable, wrong type in expression, division by zero, repeat count out of range |

`Interpreter.run()` catches any `BloopException` and prints its message to `System.err`, so the program degrades gracefully rather than producing a stack trace.

---

## 8. Extension Points

The architecture is intentionally open for extension:

**Adding a new statement type** — implement `StatementParser`, define a trigger `TokenType`, and register the parser in `Parser.createDefault()`. No existing class changes.

**Adding a new expression node** — implement `Expression`, add handling to `ExpressionParser` at the appropriate precedence level.

**Adding a new instruction** — implement `Instruction`. The `Environment` and execution loop require no changes.

**Adding a new built-in value type** — extend `BinaryOpNode` evaluation logic and update `PrintInstruction.formatForOutput()`.