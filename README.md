# BLOOP Interpreter

A working interpreter for the **BLOOP scripting language**, built entirely in pure Java.  
Course Project — Advanced Object-Oriented Programming | Sitare University



## Table of Contents

- What is BLOOP?
- Team
- Project Structure
- How to Compile and Run
- Architecture
- Design Patterns
- OOP Principles
- Sample Programs
- Git Workflow



## What is BLOOP?

**BLOOP (Beginner-Level Object-Oriented Program)** is a small scripting language that reads like plain English sentences.  
It supports variables, arithmetic, conditionals, and loops.

put 10 into x
put 3 into y
put x + y * 2 into result
print result

if result > 10 then:
print "big number"

repeat 3 times:
print "hello"



## Team

| Member   | Responsibility |
|----------|---------------|
| Member 1 | token/ — TokenType, Token, Tokenizer · Main.java |
| Member 2 | ast/ — Expression nodes · parser/ — Parser |
| Member 3 | instruction/ — Instructions · runtime/ — Environment · interpreter/ |



## Project Structure

bloop-interpreter/
├── src/
│ └── bloop/
│ ├── token/
│ │ ├── TokenType.java
│ │ ├── Token.java
│ │ └── Tokenizer.java
│ ├── ast/
│ │ ├── Expression.java
│ │ ├── NumberNode.java
│ │ ├── StringNode.java
│ │ ├── VariableNode.java
│ │ └── BinaryOpNode.java
│ ├── instruction/
│ │ ├── Instruction.java
│ │ ├── AssignInstruction.java
│ │ ├── PrintInstruction.java
│ │ ├── IfInstruction.java
│ │ └── RepeatInstruction.java
│ ├── runtime/
│ │ └── Environment.java
│ ├── parser/
│ │ └── Parser.java
│ ├── interpreter/
│ │ └── Interpreter.java
│ └── Main.java
├── examples/
│ ├── program1.bloop
│ ├── program2.bloop
│ ├── program3.bloop
│ └── program4.bloop
├── docs/
│ ├── Architecture.md
│ ├── DesignPatterns.md
│ └── LanguageSpec.md
├── .gitignore
└── README.md






## How to Compile and Run

### Prerequisites
- Java 11 or higher
- No external libraries required



### Step 1 — Compile
javac -d out
src/bloop/token/.java
src/bloop/ast/.java
src/bloop/instruction/.java
src/bloop/runtime/.java
src/bloop/parser/.java
src/bloop/interpreter/.java
src/bloop/Main.java





### Step 2 — Run a .bloop file

java -cp out bloop.Main examples/program1.bloop




### Example Output
16



## Architecture

The interpreter works as a three-step pipeline. Each stage processes the output of the previous one.


Source Code (.bloop)
↓
Tokenizer (Lexer)
↓
Parser
↓
Interpreter + Environment
↓
Output




## Why a Tree for Expressions?

A flat list of tokens cannot capture operator precedence. A tree can.

For:
x + y * 2


Tree representation:
    Add
   /   \
  x   Multiply
       /     \
      y       2



The deeper a node is in the tree, the earlier it gets evaluated.



## Design Patterns

| Pattern   | Location       | Purpose |
|-----------|--------------|--------|
| Composite | ast/         | Expression trees |
| Strategy  | instruction/ | Instruction execution |
| Pipeline  | Interpreter  | Lexer → Parser → Execution |



## OOP Principles

| Principle | How it is applied |
|----------|------------------|
| Single Responsibility | Each component has one clear job |
| Open / Closed | New features can be added without modifying existing code |
| Liskov Substitution | Expression subclasses behave consistently |
| Dependency Inversion | Interpreter depends on abstractions |



## Sample Programs

### Program 1 — Arithmetic
put 10 into x
put 3 into y
put x + y * 2 into result
print result


Output:

16


### Program 2 — String Output


put "Sitare" into name
print name
print "Hello from BLOOP"


Output:


Sitare
Hello from BLOOP




### Program 3 — Conditional


put 85 into score
if score > 50 then:
print "Pass"


Output:

Pass



### Program 4 — Loop


put 1 into i
repeat 4 times:
print i
put i + 1 into i


Output:


1
2
3
4



## Git Workflow


main → stable code
dev → integration branch
├── feature/tokenizer
├── feature/ast-parser
└── feature/runtime


### Rules

- Never commit directly to main
- All feature branches merge into dev via Pull Request
- dev merges into main only after testing



## Dependencies

- Java 11+
- No external libraries