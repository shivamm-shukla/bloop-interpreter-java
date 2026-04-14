# Design Patterns

> Design patterns applied in the Bloop Interpreter codebase — what they are, where they live, and why they were chosen.

---

## Table of Contents

1. [Interpreter Pattern](#1-interpreter-pattern)
2. [Composite Pattern](#2-composite-pattern)
3. [Strategy Pattern](#3-strategy-pattern)
4. [Registry Pattern](#4-registry-pattern)
5. [Template Method (via Interface Default Behaviour)](#5-template-method-via-interface-default-behaviour)
6. [Null Object Pattern](#6-null-object-pattern)
7. [Factory Method Pattern](#7-factory-method-pattern)
8. [Cursor / Iterator Pattern](#8-cursor--iterator-pattern)

---

## 1. Interpreter Pattern

**What it is:** Each node in an Abstract Syntax Tree implements an `interpret` or `evaluate` method. Traversal is done by calling that method recursively, passing shared context.

**Where it lives:**

- `Expression` interface → `evaluate(Environment env) : Object`
- Implementations: `NumberNode`, `StringNode`, `VariableNode`, `BinaryOpNode`
- `Instruction` interface → `execute(Environment env)`
- Implementations: `AssignInstruction`, `PrintInstruction`, `IfInstruction`, `RepeatInstruction`

**Why it was chosen:** The AST nodes own their own evaluation logic. Adding a new expression type means adding one class — no switch statements, no `instanceof` chains, no changes to existing classes. This is the heart of a tree-walking interpreter.

```java
// Each node knows how to evaluate itself
public Object evaluate(Environment env) {
    Object left  = leftOperand.evaluate(env);
    Object right = rightOperand.evaluate(env);
    return applyArithmetic(left, right, operator);
}
```

---

## 2. Composite Pattern

**What it is:** Individual objects and compositions of objects are treated uniformly through a shared interface. A composite node contains children of the same interface type.

**Where it lives:**

- `BinaryOpNode` contains two `Expression` children, both accessed through the `Expression` interface.
- `IfInstruction` and `RepeatInstruction` contain `List<Instruction>` as their bodies — lists of the same `Instruction` interface they themselves implement.

**Why it was chosen:** Nested expressions like `(a + b) * (c - d)` and nested blocks like a `repeat` inside an `if` are handled without any special cases. The tree can be arbitrarily deep and execution remains a simple recursive call.

```java
// IfInstruction holds a List<Instruction> body — same type as itself
thenBody.forEach(instruction -> instruction.execute(env));
```

---

## 3. Strategy Pattern

**What it is:** A family of algorithms is encapsulated behind a common interface, and the appropriate one is selected at runtime.

**Where it lives:**

- `StatementParser` interface — each implementation (`PutStatementParser`, `PrintStatementParser`, `IfStatementParser`, `RepeatStatementParser`) is a separate parsing strategy.
- `BlockParser` functional interface — the block-parsing behaviour is passed as a strategy into every `StatementParser.parse()` call.

**Why it was chosen:** Each statement has its own syntax rules. Encapsulating them as separate strategy objects means they can be developed, tested, and extended independently. The `Parser` itself stays thin — it just dispatches to the right strategy.

```java
// The correct parsing strategy is selected by the registry at runtime
StatementParser handler = statementRegistry.resolve(currentToken);
return handler.parse(cursor, () -> parseIndentedBlock(cursor));
```

---

## 4. Registry Pattern

**What it is:** A central map from a key to a handler object, looked up at runtime. A variation of Strategy where the selection is data-driven rather than hardcoded.

**Where it lives:**

- `StatementParserRegistry` — maps `TokenType` → `StatementParser`. Built once at startup from a list of registered parsers.
- `KeywordRegistry` — maps `String` word → `TokenType`. A static map used during lexing.
- `OperatorRegistry` — maps `Character` → `TokenType` (single) or `CompoundEntry` (two-character operators).

**Why it was chosen:** Adding a new statement, keyword, or operator requires registering one new entry. The lookup code never changes. This keeps the core pipeline open for extension and closed for modification (Open/Closed Principle).

```java
// StatementParserRegistry built from a list — no hardcoded dispatch
List<StatementParser> builtInParsers = List.of(
                new PutStatementParser(sharedExpressionParser),
                new PrintStatementParser(sharedExpressionParser),
                new IfStatementParser(sharedExpressionParser),
                new RepeatStatementParser(sharedExpressionParser)
        );
return new Parser(new StatementParserRegistry(builtInParsers));
```

---

## 5. Template Method (via Interface Default Behaviour)

**What it is:** A skeleton algorithm is defined in a base type; subclasses or implementations fill in specific steps.

**Where it lives:**

- `StatementParser` interface defines the contract: `triggerToken()` returns the token that activates this parser, and `parse()` performs the actual parsing. Every implementation provides both.
- `BlockParser` functional interface acts as a callback that any `StatementParser` can invoke to parse an indented sub-block without knowing how blocks work internally.

**Why it was chosen:** All statement parsers follow the same skeleton — check the trigger, parse the syntax, return an instruction. Factoring out the trigger token as a separate method allows the registry to build itself automatically. The `BlockParser` callback prevents code duplication across `IfStatementParser` and `RepeatStatementParser`.

---

## 6. Null Object Pattern

**What it is:** Instead of returning `null` to represent "nothing found", a safe default object is returned that behaves harmlessly.

**Where it lives:**

- `TokenCursor.current()` — when the cursor is past the end of the token list, it returns `new Token(TokenType.EOF, "EOF", -1)` instead of `null`. Every caller can safely call `.type()` without a null check.
- `TokenCursor.tryConsume()` — returns `null` only as an explicit "not matched" signal, documented and handled at every call site. This is intentional to support `while (token = cursor.tryConsume(...)) != null` loops.

**Why it was chosen:** `NullPointerException` during parsing would be hard to diagnose and would produce no useful error message. Returning a sentinel `EOF` token means the parser always has a token to inspect, and the error message comes from the parser's own exception rather than a surprise NPE.

```java
public Token current() {
    if (currentIndex >= tokens.size()) {
        return new Token(TokenType.EOF, "EOF", -1); // safe sentinel
    }
    return tokens.get(currentIndex);
}
```

---

## 7. Factory Method Pattern

**What it is:** Object creation is delegated to a static factory method rather than calling constructors directly, allowing the creation logic to be encapsulated and reused.

**Where it lives:**

- `Parser.createDefault()` — a static factory method that wires up all the default statement parsers, creates a shared `ExpressionParser`, builds the `StatementParserRegistry`, and returns a fully configured `Parser`.

**Why it was chosen:** `Interpreter` should not need to know the internal wiring of the parser. `createDefault()` centralises that knowledge. Tests can bypass it and pass a custom `StatementParserRegistry` directly through the constructor.

```java
// Interpreter uses the factory — knows nothing about the wiring
this.parser = Parser.createDefault();

// Tests can inject a custom registry directly
return new Parser(new StatementParserRegistry(customParsers));
```

---

## 8. Cursor / Iterator Pattern

**What it is:** Stateful traversal of a collection is encapsulated in a separate object, providing a clean API for advancing through elements one at a time.

**Where it lives:**

- `LexerCursor` — encapsulates position within the raw source `String`. Provides `currentChar()`, `peekNextChar()`, `advance()`, `advanceIf()`, `sliceFrom()`.
- `TokenCursor` — encapsulates position within the `List<Token>`. Provides `current()`, `consume()`, `expect()`, `tryConsume()`, `check()`, `skipNewlines()`.

**Why it was chosen:** The `Tokenizer` and `Parser` have complex, non-linear traversal logic (lookahead, conditional advancement, backtracking-free single-pass parsing). Encapsulating the traversal state in a cursor object keeps the main logic readable and prevents index-arithmetic bugs from spreading across the codebase.

```java
// All position state is hidden inside the cursor
Token operatorToken = cursor.tryConsume(
                TokenType.PLUS, TokenType.MINUS
        );
if (operatorToken != null) { ... }
```