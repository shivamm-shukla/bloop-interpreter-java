# Design Patterns and OOP Principles

> This document explains which design patterns and object-oriented principles appear in the BLOOP interpreter, exactly where they are applied, and why each decision was made.

---

## Design Patterns

### 1. Composite Pattern

**Location:** `src/bloop/ast/`  
**Classes involved:** `Expression`, `NumberNode`, `StringNode`, `VariableNode`, `BinaryOpNode`

#### What it is

The Composite pattern lets you treat individual objects and compositions of objects the same way. A single number and a complex nested expression both respond to the same `evaluate()` call.

#### How it appears in BLOOP

The `Expression` interface has one method:
```java
Object evaluate(Environment env);
```

Leaf nodes (`NumberNode`, `StringNode`, `VariableNode`) hold a single value and return it directly.

`BinaryOpNode` is the composite — it holds two `Expression` children and an operator. When evaluated, it asks each child to evaluate itself, then applies the operator.

```
BinaryOpNode("+")
    ├── VariableNode("x")          ← leaf
    └── BinaryOpNode("*")          ← composite
            ├── VariableNode("y")  ← leaf
            └── NumberNode(2)      ← leaf
```

Because every node implements `Expression`, `BinaryOpNode` does not need to know or care whether its children are simple values or further nested expressions. It just calls `evaluate()` on each one.

#### Why this was the right choice

Without Composite, the evaluator would need `if (left instanceof NumberNode)` checks everywhere. With Composite, every node is treated identically — the tree evaluates itself recursively with no special-case logic.

---

### 2. Strategy Pattern

**Location:** `src/bloop/instruction/`  
**Classes involved:** `Instruction`, `AssignInstruction`, `PrintInstruction`, `IfInstruction`, `RepeatInstruction`

#### What it is

The Strategy pattern defines a family of algorithms, puts each in its own class, and makes them interchangeable through a common interface.

#### How it appears in BLOOP

The `Instruction` interface defines one method:
```java
void execute(Environment env);
```

Each instruction type is its own class with its own `execute()` implementation:

| Class | What execute() does |
|-------|---------------------|
| `AssignInstruction` | Evaluates expression, stores result in Environment |
| `PrintInstruction` | Evaluates expression, prints result to stdout |
| `IfInstruction` | Evaluates condition, runs body if true |
| `RepeatInstruction` | Runs body a fixed number of times |

The Interpreter's execution loop does not need to know which instruction it is running:
```java
for (Instruction instr : instructions) {
    instr.execute(env);   // same call every time
}
```

#### Why this was the right choice

If we used a single class with a big `if-else` or `switch` block, adding a new instruction type would require editing existing code. With Strategy, adding a new instruction means adding one new class. Nothing else changes.

---

### 3. Pipeline Pattern

**Location:** `src/bloop/interpreter/Interpreter.java`

#### What it is

The Pipeline pattern structures a process as a sequence of independent stages. Each stage receives the output of the previous stage as its input.

#### How it appears in BLOOP

```java
public void run(String sourceCode) {
    // Stage 1
    Tokenizer tokenizer = new Tokenizer(sourceCode);
    List<Token> tokens = tokenizer.tokenize();

    // Stage 2
    Parser parser = new Parser(tokens);
    List<Instruction> instructions = parser.parse();

    // Stage 3
    Environment env = new Environment();
    for (Instruction instr : instructions) {
        instr.execute(env);
    }
}
```

The three stages — Tokenizer, Parser, Evaluator — are completely independent. The Tokenizer knows nothing about instructions. The Parser knows nothing about how values get printed. Each stage has exactly one responsibility.

#### Why this was the right choice

Independence between stages means each part can be developed, tested, and debugged in isolation. A bug in the Parser does not affect the Tokenizer. A bug in the Evaluator does not affect the Parser.

---

## OOP Principles (SOLID)

### S — Single Responsibility Principle

> A class should have only one reason to change.

| Class | Its one responsibility |
|-------|------------------------|
| `Tokenizer` | Break source text into tokens |
| `Token` | Hold one piece of source code immutably |
| `Parser` | Turn a token list into an instruction list |
| `Environment` | Store and retrieve variable values |
| `BinaryOpNode` | Represent and evaluate a binary expression |
| `AssignInstruction` | Execute one assignment statement |

None of these classes reach into each other's territory. If the way variables are stored needs to change, only `Environment` changes. If the BLOOP syntax changes, only `Tokenizer` and `Parser` change.

---

### O — Open / Closed Principle

> Classes should be open for extension, closed for modification.

Both `Expression` and `Instruction` are interfaces. To add a new feature:

- New expression type (e.g. unary minus) → add `UnaryOpNode implements Expression`. Nothing else changes.
- New instruction type (e.g. while loop) → add `WhileInstruction implements Instruction`. Nothing else changes.

The `Interpreter` execution loop, the `Environment`, and all existing classes remain untouched.

---

### L — Liskov Substitution Principle

> Subtypes must be substitutable for their base type without breaking the program.

Wherever the code expects an `Expression`, any of these works identically:
- `NumberNode`
- `StringNode`
- `VariableNode`
- `BinaryOpNode`

The `Parser` stores expression results as `Expression` references. The `Interpreter` calls `evaluate()` without knowing or caring which subclass it has. Any subclass can stand in for any other without breaking anything.

---

### D — Dependency Inversion Principle

> High-level modules should depend on abstractions, not on concrete implementations.

The `Interpreter` class (high-level) depends on:
- The `Instruction` **interface** — not on `AssignInstruction`, `PrintInstruction`, etc. directly
- The `Environment` **class** as a single shared abstraction

The `Parser` produces `List<Instruction>` — it does not know or care which concrete instruction subclasses it creates at the call site. The execution loop treats them all identically through the interface.

---

## Summary Table

| Concept | Where | One-line reason |
|---------|-------|-----------------|
| Composite Pattern | `ast/` | Lets simple and complex expressions be treated identically |
| Strategy Pattern | `instruction/` | Each instruction type encapsulates its own behaviour |
| Pipeline Pattern | `Interpreter.java` | Keeps the three stages independent and testable |
| Single Responsibility | Every class | Each class has exactly one job |
| Open / Closed | `Expression`, `Instruction` interfaces | New features = new classes, no edits to existing code |
| Liskov Substitution | All `Expression` subclasses | Any node works wherever `Expression` is expected |
| Dependency Inversion | `Interpreter`, `Parser` | Both depend on interfaces, not concrete classes |
