# BLOOP User Guide

> A simple, readable, indentation-based programming language.

---

## Table of Contents

1. [Getting Started](#1-getting-started)
2. [Variables](#2-variables)
3. [Data Types](#3-data-types)
4. [Arithmetic Operators](#4-arithmetic-operators)
5. [Comparison Operators](#5-comparison-operators)
6. [Print Statement](#6-print-statement)
7. [If / Else Statement](#7-if--else-statement)
8. [Repeat Loop](#8-repeat-loop)
9. [Indentation Rules](#9-indentation-rules)
10. [String Escape Sequences](#10-string-escape-sequences)
11. [Error Reference](#11-error-reference)
12. [Quick Reference](#12-quick-reference)

---

## 1. Getting Started

BLOOP is a simple programming language that uses **indentation** to define code blocks. Every block of code is defined by its indentation level, not by braces `{}`.

**Your first BLOOP program:**

```
print "Hello, World!"
```

---

## 2. Variables

BLOOP uses `put ... into` syntax to assign values to variables.

**Syntax:**

```
put <value> into <variable>
```

**Examples:**

```
put 42 into age
put 3.14 into pi
put "Alice" into name
put age into backup
```

**Rules:**

- Variable names can contain **letters, digits, and underscores**
- Variable names **cannot start with a digit**
- Variable names are **case-sensitive** — `score` and `Score` are different

```
put 10 into score
put 20 into Score
print score        # 10
print Score        # 20
```

---

## 3. Data Types

BLOOP has three data types:

| Type | Example | Description |
|---|---|---|
| Number | `42`, `3.14` | Integer or decimal number |
| String | `"hello"` | Text enclosed in double quotes |
| Identifier | `x`, `my_var` | Variable name |

---

## 4. Arithmetic Operators

| Operator | Symbol | Example |
|---|---|---|
| Addition | `+` | `a + b` |
| Subtraction | `-` | `a - b` |
| Multiplication | `*` | `a * b` |
| Division | `/` | `a / b` |

**Examples:**

```
put 10 into a
put 3 into b
put a + b into sum          # 13
put a - b into difference   # 7
put a * b into product      # 30
put a / b into quotient     # 3.33...
```

**Expression Precedence** — standard math rules apply:

```
put 2 + 3 * 4 into result    # 14, not 20
put (2 + 3) * 4 into result  # 20
```

---

## 5. Comparison Operators

| Operator | Symbol | Meaning |
|---|---|---|
| Equal | `==` | Both sides are equal |
| Not Equal | `!=` | Both sides are not equal |
| Greater Than | `>` | Left is greater |
| Greater or Equal | `>=` | Left is greater or equal |
| Less Than | `<` | Left is smaller |
| Less or Equal | `<=` | Left is smaller or equal |

---

## 6. Print Statement

**Syntax:**

```
print <value or expression>
```

**Examples:**

```
print "Hello, World!"
print 42
print 10 + 5
put "Alice" into name
print name
```

---

## 7. If / Else Statement

**Syntax:**

```
if <condition> then:
    <statements>
else:
    <statements>
```

The `else` block is optional.

**Examples:**

```
put 18 into age

if age >= 18 then:
    print "You are an adult"
```

```
if age >= 18 then:
    print "You are an adult"
else:
    print "You are a minor"
```

**Nested If:**

```
put 85 into marks

if marks >= 90 then:
    print "Grade A"
else:
    if marks >= 75 then:
        print "Grade B"
    else:
        print "Grade C"
```

---

## 8. Repeat Loop

**Syntax:**

```
repeat <number> times:
    <statements>
```

**Examples:**

```
repeat 5 times:
    print "Hello!"
```

```
put 3 into count
repeat count times:
    print "BLOOP!"
```

**Nested Repeat:**

```
repeat 3 times:
    repeat 2 times:
        print "inner"
    print "outer"
```

---

## 9. Indentation Rules

BLOOP uses indentation to define blocks — **no curly braces needed.**

**Rules:**

- Use **spaces or tabs** — don't prefer to use both on the same line
- **1 tab = 4 spaces**
- Every block must be consistently indented
- Dedent must return to a previously opened level

**Valid — spaces:**

```
if age >= 18 then:
    print "adult"
    print "welcome"
```

**Valid — tabs:**

```
if age >= 18 then:
	print "adult"
```

**Invalid — inconsistent dedent:**

```
if age >= 18 then:
    print "adult"    # 4 spaces opened
  print "oops"       # ERROR — 2 spaces never opened
```

---

## 10. String Escape Sequences

| Sequence | Meaning |
|---|---|
| `\n` | New line |
| `\t` | Tab |
| `\\` | Backslash |
| `\"` | Double quote |

**Examples:**

```
print "Hello\nWorld"
print "Name:\tAlice"
print "say \"hello\""
```

---

## 11. Error Reference

| Error | Cause |
|---|---|
| `Unterminated string` | String not closed before line end or EOF |
| `Malformed number` | Multiple decimal points e.g. `3.14.15` |
| `IndentationError` | Dedent to a level that was never opened |
| `Unexpected character` | Unrecognized symbol e.g. `@`, `$` |
| `Unknown escape sequence` | Invalid escape in string e.g. `"\z"` |

---

## 12. Quick Reference

```
put <value> into <var>       # assign variable
print <expr>                 # print output

if <condition> then:         # if block
    <statements>
else:                        # optional else
    <statements>

repeat <n> times:            # repeat loop
    <statements>

Arithmetic:  + - * /
Comparison:  == != > >= < <=
Grouping:    ( )
```

---

> BLOOP is designed to be **simple, readable, and beginner-friendly.**
> Happy coding!