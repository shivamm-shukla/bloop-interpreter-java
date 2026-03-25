# BLOOP Interpreter

> A working interpreter for the **BLOOP** scripting language, built entirely in pure Java.  
> Course Project — Advanced Object-Oriented Programming | Sitare University

---

## Table of Contents

- [What is BLOOP?](#what-is-bloop)
- [Team](#team)
- [Project Structure](#project-structure)
- [How to Compile and Run](#how-to-compile-and-run)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [OOP Principles](#oop-principles)
- [Sample Programs](#sample-programs)
- [Git Workflow](#git-workflow)

---

## What is BLOOP?

**BLOOP** (Beginner-Level Object-Oriented Program) is a small scripting language that reads like plain English sentences. It supports variables, arithmetic, conditionals, and loops.

```
put 10 into x
put 3 into y
put x + y * 2 into result
print result

if result > 10 then:
    print "big number"

repeat 3 times:
    print "hello"
```

---

## Team

| Member | Responsibility |
|--------|----------------|
| Member 1 | `token/` — TokenType, Token, Tokenizer · `Main.java` |
| Member 2 | `ast/` — Expression nodes · `parser/` — Parser |
| Member 3 | `instruction/` — All instructions · `runtime/` — Environment · `interpreter/` — Interpreter |

---

## Project Structure

```
bloop-interpreter/
├── src/
│   └── bloop/
│       ├── token/
│       │   ├── TokenType.java
│       │   ├── Token.java
│       │   └── Tokenizer.java
│       ├── ast/
│       │   ├── Expression.java
│       │   ├── NumberNode.java
│       │   ├── StringNode.java
│       │   ├── VariableNode.java
│       │   └── BinaryOpNode.java
│       ├── instruction/
│       │   ├── Instruction.java
│       │   ├── AssignInstruction.java
│       │   ├── PrintInstruction.java
│       │   ├── IfInstruction.java
│       │   └── RepeatInstruction.java
│       ├── runtime/
│       │   └── Environment.java
│       ├── parser/
│       │   └── Parser.java
│       ├── interpreter/
│       │   └── Interpreter.java
│       └── Main.java
├── examples/
│   ├── program1.bloop
│   ├── program2.bloop
│   ├── program3.bloop
│   └── program4.bloop
├── docs/
│   ├── Architecture.md
│   ├── DesignPatterns.md
│   └── LanguageSpec.md
├── .gitignore
└── README.md
```

---

## How to Compile and Run

### Prerequisites
- Java 11 or higher
- No external libraries required

### Step 1 — Compile

```bash
javac -d out \
  src/bloop/token/*.java \
  src/bloop/ast/*.java \
  src/bloop/instruction/*.java \
  src/bloop/runtime/*.java \
  src/bloop/parser/*.java \
  src/bloop/interpreter/*.java \
  src/bloop/Main.java
```

### Step 2 — Run a `.bloop` file

```bash
java -cp out bloop.Main examples/program1.bloop
```

### Example

```bash
$ java -cp out bloop.Main examples/program1.bloop
16
```

---

## Architecture

The interpreter works as a **three-step pipeline**. Each step takes the output of the previous one.

```
┌─────────────────┐
│  Source Code    │  (.bloop file)
└────────┬────────┘
         ↓
┌─────────────────┐
│   Tokenizer     │  Breaks source into a flat list of Tokens
└────────┬────────┘
         ↓
┌─────────────────┐
│     Parser      │  Builds a List<Instruction> with Expression trees inside
└────────┬────────┘
         ↓
┌─────────────────────────┐
│  Interpreter +          │  Executes each instruction using shared Environment
│  Environment            │
└─────────────────────────┘
         ↓
┌─────────────────┐
│     Output      │  Printed to stdout
└─────────────────┘
```

### Why a Tree for Expressions?

A flat list of tokens cannot capture operator precedence. A tree can.

For `x + y * 2`, multiply must happen before add:

```
    Add
   /   \
  x   Multiply
       /   \
      y     2
```

The deeper a node sits in the tree, the earlier it gets evaluated.  
No special tricks needed — the shape of the tree handles precedence automatically.

---

## Design Patterns

| Pattern | Location | Purpose |
|---------|----------|---------|
| **Composite** | `ast/` | `BinaryOpNode` holds two `Expression` children, enabling arbitrarily nested expressions |
| **Strategy** | `instruction/` | Each instruction class encapsulates its own `execute()` logic behind a common interface |
| **Pipeline** | `Interpreter.java` | Three independent stages — Tokenizer → Parser → Evaluator — each with one job |

See [`docs/DesignPatterns.md`](docs/DesignPatterns.md) for detailed explanation.

---

## OOP Principles

| Principle | How it is applied |
|-----------|-------------------|
| **S** — Single Responsibility | `Tokenizer` only tokenizes. `Parser` only parses. `Environment` only stores variables. |
| **O** — Open / Closed | Adding a new instruction means adding a new class. No existing class needs to change. |
| **L** — Liskov Substitution | Any `Expression` subclass works wherever `Expression` is expected. |
| **D** — Dependency Inversion | `Interpreter` depends on the `Instruction` interface, not on concrete classes. |

---

## Sample Programs

### Program 1 — Arithmetic and Variables
```
put 10 into x
put 3 into y
put x + y * 2 into result
print result
```
**Output:** `16`

### Program 2 — String Output
```
put "Sitare" into name
print name
print "Hello from BLOOP"
```
**Output:**
```
Sitare
Hello from BLOOP
```

### Program 3 — Conditional
```
put 85 into score
if score > 50 then:
    print "Pass"
```
**Output:** `Pass`

### Program 4 — Loop
```
put 1 into i
repeat 4 times:
    print i
    put i + 1 into i
```
**Output:**
```
1
2
3
4
```

---

## Git Workflow

```
main                       ← stable, fully tested code only
dev                        ← integration branch
  ├── feature/tokenizer    ← Member 1
  ├── feature/ast-parser   ← Member 2
  └── feature/runtime      ← Member 3
```

### Branch Rules
- Never commit directly to `main`
- All feature branches merge into `dev` via Pull Request
- `dev` merges into `main` only when all 4 sample programs produce correct output

---

## Dependencies

- Java 11+
- No external libraries