# BLOOP Language Specification

> BLOOP (Beginner-Level Object-Oriented Program) is a small scripting language designed to read like plain English sentences. This document defines its complete syntax and behaviour.

---

## Overview

BLOOP programs are plain text files with the `.bloop` extension. Each statement occupies one line. Blocks (the body of `if` and `repeat`) are indented with spaces or a tab.

---

## Data Types

| Type | Examples | Notes |
|------|----------|-------|
| Number | `10`, `3`, `3.14` | Stored internally as `double` |
| String | `"hello"`, `"Sitare"` | Must be wrapped in double quotes |
| Boolean | produced by comparisons | `true` or `false` — not a literal in source |

---

## Statements

### Assignment — `put ... into`

Evaluates an expression and stores the result in a variable.

```
put <expression> into <variable>
```

**Examples:**
```
put 10 into x
put 3 into y
put x + y * 2 into result
put "Sitare" into name
```

---

### Print — `print`

Evaluates an expression and prints the result to standard output, followed by a newline.

```
print <expression>
```

**Examples:**
```
print result
print name
print "Hello from BLOOP"
```

**Number formatting:** If the result is a whole number (e.g. `16.0`), it is printed as an integer (`16`), not as a decimal.

---

### Conditional — `if ... then:`

Evaluates a condition. If the condition is true, the indented body is executed. If false, the body is skipped entirely.

```
if <condition> then:
    <instruction>
    <instruction>
    ...
```

**Example:**
```
put 85 into score
if score > 50 then:
    print "Pass"
```

- The condition must be a comparison expression.
- The body must be indented (at least one space or tab).
- There is no `else` clause in the base language.

---

### Repeat Loop — `repeat ... times:`

Executes the body a fixed number of times.

```
repeat <number> times:
    <instruction>
    <instruction>
    ...
```

**Example:**
```
put 1 into i
repeat 4 times:
    print i
    put i + 1 into i
```

- The repeat count must be a positive integer literal.
- The body must be indented.

---

## Expressions

Expressions appear on the right-hand side of assignments and as arguments to `print` and `if`.

### Literals

```
10          number
3.14        number
"hello"     string
```

### Variable References

```
x
result
score
```

A variable must be assigned a value before it can be used. Using an undefined variable causes a runtime error.

### Arithmetic

```
x + y       addition
x - y       subtraction
x * y       multiplication
x / y       division
```

**Operator precedence:** `*` and `/` bind more tightly than `+` and `-`, following standard mathematical rules.

```
x + y * 2   →  x + (y * 2)    ← NOT (x + y) * 2
```

Use parentheses if a different order is needed *(if supported by the implementation)*.

### Comparisons

```
x > y       greater than
x < y       less than
x >= y      greater than or equal
x <= y      less than or equal
x == y      equal to
```

Comparison expressions return a boolean result. They are used as conditions in `if` statements.

---

## Keywords

The following words are reserved and cannot be used as variable names:

```
put    into    print    if    then    repeat    times
```

---

## Identifiers (Variable Names)

- Must start with a letter (`a–z`, `A–Z`) or underscore (`_`)
- Can contain letters, digits (`0–9`), and underscores
- Case-sensitive: `score` and `Score` are different variables

**Valid:** `x`, `result`, `myVar`, `score2`, `_temp`  
**Invalid:** `2x`, `my-var`, `put` (reserved keyword)

---

## Comments

The base language does not include comments.

---

## Program Structure

- Each statement is on its own line
- Blank lines are ignored
- Indentation marks the body of `if` and `repeat` blocks
- There is no explicit `end` or closing brace — dedentation ends a block

---

## Complete Example Programs

### Program 1 — Arithmetic and Variables

```
put 10 into x
put 3 into y
put x + y * 2 into result
print result
```

Expected output:
```
16
```

---

### Program 2 — String Output

```
put "Sitare" into name
print name
print "Hello from BLOOP"
```

Expected output:
```
Sitare
Hello from BLOOP
```

---

### Program 3 — Conditional

```
put 85 into score
if score > 50 then:
    print "Pass"
```

Expected output:
```
Pass
```

---

### Program 4 — Loop

```
put 1 into i
repeat 4 times:
    print i
    put i + 1 into i
```

Expected output:
```
1
2
3
4
```

---

## Error Behaviour

| Situation | What happens |
|-----------|--------------|
| Variable used before assignment | `RuntimeException: Variable not defined: <name>` |
| Unknown token in source | `RuntimeException` with line number |
| Unexpected token during parsing | `RuntimeException` with description and line number |
