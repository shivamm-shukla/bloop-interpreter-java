# Architecture

> This document explains how the BLOOP interpreter is structured internally — how data flows from raw source code to program output, and why the system is designed the way it is.

---

## The Three-Step Pipeline

Every time you run a `.bloop` file, the interpreter performs exactly three steps in sequence.

```
Source Code  →  [Tokenizer]  →  [Parser]  →  [Interpreter + Environment]  →  Output
```

Each step has one job. Each step hands its output to the next. Nothing skips a step.

---

## Step 1 — Tokenizer (`token/`)

**Input:** Raw source code as a single `String`  
**Output:** `List<Token>`

The Tokenizer reads the source character by character. When it recognises a complete pattern — a number, a keyword, an operator — it wraps it in a `Token` object and moves on.

**Example:**

Source: `put 10 into x`

Produces:
```
Token(PUT,        "put",   line 1)
Token(NUMBER,     "10",    line 1)
Token(INTO,       "into",  line 1)
Token(IDENTIFIER, "x",     line 1)
Token(NEWLINE,    "\n",    line 1)
```

**Key classes:**

| Class | Role |
|-------|------|
| `TokenType` | Enum listing every kind of token BLOOP can produce |
| `Token` | Immutable object holding type, raw text, and line number |
| `Tokenizer` | Walks the source string and emits tokens one at a time |

---

## Step 2 — Parser (`parser/` + `ast/`)

**Input:** `List<Token>`  
**Output:** `List<Instruction>`

The Parser reads tokens one at a time and decides what kind of instruction it is looking at. For each instruction it builds an object. For each expression inside that instruction it builds a tree.

### Why a Tree?

Arithmetic has structure. In `x + y * 2`, multiplication must happen before addition. A flat list of tokens cannot capture this — but a tree can.

```
    Add
   /   \
  x   Multiply
       /   \
      y     2
```

The node sitting deeper in the tree gets evaluated first. The shape of the tree encodes precedence automatically — no special logic needed at evaluation time.

### How Precedence is Handled

The Parser uses three chained methods:

```
parseExpression()  →  handles + and -  (lowest precedence)
     calls
parseTerm()        →  handles * and /  (higher precedence)
     calls
parsePrimary()     →  handles a single number, string, or variable  (highest)
```

Because `parseTerm` is called first for each operand of `+` or `-`, multiplication and division naturally bind tighter. This is called **recursive descent parsing**.

**Example:**

Source: `put x + y * 2 into result`

Parser builds:
```
AssignInstruction(
  name = "result",
  expression = BinaryOpNode(
    left  = VariableNode("x"),
    op    = "+",
    right = BinaryOpNode(
      left  = VariableNode("y"),
      op    = "*",
      right = NumberNode(2)
    )
  )
)
```

**Key classes:**

| Class | Role |
|-------|------|
| `Expression` | Interface with `evaluate(Environment)` — all expression nodes implement this |
| `NumberNode` | Leaf node — holds a numeric literal |
| `StringNode` | Leaf node — holds a string literal |
| `VariableNode` | Leaf node — looks up a variable in the Environment |
| `BinaryOpNode` | Composite node — holds left expression, operator, right expression |
| `Parser` | Reads tokens and builds the instruction + expression tree |

---

## Step 3 — Interpreter + Environment (`interpreter/` + `runtime/`)

**Input:** `List<Instruction>` + shared `Environment`  
**Output:** Printed output to stdout

The Interpreter loops through every instruction and calls `execute(env)` on it. Each instruction evaluates its expressions and reads or writes variables through the shared `Environment`.

```java
Environment env = new Environment();
for (Instruction instr : instructions) {
    instr.execute(env);
}
```

### Environment

The `Environment` is a simple `Map<String, Object>`. It is created once and shared across every instruction. Every assignment writes to it. Every variable reference reads from it.

```
put 10 into x     →  env.set("x", 10.0)
print x           →  env.get("x")  →  prints 10
```

**Key classes:**

| Class | Role |
|-------|------|
| `Instruction` | Interface with `execute(Environment)` — all instruction classes implement this |
| `AssignInstruction` | Evaluates expression, stores result in Environment |
| `PrintInstruction` | Evaluates expression, prints result to stdout |
| `IfInstruction` | Evaluates condition, executes body if true |
| `RepeatInstruction` | Executes body a fixed number of times |
| `Environment` | Shared variable store — `Map<String, Object>` |
| `Interpreter` | Creates the pipeline — runs Tokenizer, Parser, then execution loop |

---

## Complete Data Flow — Worked Example

**Source:** `program3.bloop`
```
put 85 into score
if score > 50 then:
    print "Pass"
```

```
Tokenizer produces:
  [PUT, NUMBER(85), INTO, IDENTIFIER(score), NEWLINE,
   IF, IDENTIFIER(score), GREATER, NUMBER(50), THEN, COLON, NEWLINE,
   PRINT, STRING("Pass"), NEWLINE, EOF]

Parser builds:
  [
    AssignInstruction("score", NumberNode(85)),
    IfInstruction(
      condition = BinaryOpNode(VariableNode("score"), ">", NumberNode(50)),
      body = [ PrintInstruction(StringNode("Pass")) ]
    )
  ]

Interpreter executes:
  AssignInstruction → env.set("score", 85.0)
  IfInstruction     → evaluates BinaryOpNode → 85.0 > 50.0 → true
                    → executes body
  PrintInstruction  → prints "Pass"

Output:
  Pass
```

---

## Package Summary

```
bloop/
├── token/        Step 1 — Lexical analysis
├── ast/          Step 2 — Expression tree nodes
├── parser/       Step 2 — Token list to instruction list
├── instruction/  Step 3 — Instruction execution
├── runtime/      Step 3 — Variable storage
├── interpreter/  Connects all three steps
└── Main.java     CLI entry point
```
