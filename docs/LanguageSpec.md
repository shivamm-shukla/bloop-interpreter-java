# Language Specification

> Formal definition of the Bloop programming language — grammar, types, operators, and execution semantics.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Lexical Structure](#2-lexical-structure)
3. [Data Types](#3-data-types)
4. [Grammar](#4-grammar)
5. [Expressions](#5-expressions)
6. [Statements](#6-statements)
7. [Indentation and Block Structure](#7-indentation-and-block-structure)
8. [Execution Semantics](#8-execution-semantics)
9. [Error Conditions](#9-error-conditions)

---

## 1. Overview

Bloop is a dynamically typed, interpreted, imperative language. Programs are sequences of statements. There are no functions, no classes, and no imports. Scope is global — all variables exist in a single flat environment throughout program execution.

**Key properties:**

- Dynamically typed — variables hold values, not typed slots
- Indentation-sensitive — block structure is defined by indentation, not delimiters
- Single-pass tree-walking execution — no compilation step
- Expressions are side-effect free; only statements produce side effects

---

## 2. Lexical Structure

### 2.1 Character Set

Bloop source files are plain UTF-8 text. Only ASCII characters are used in the language syntax.

### 2.2 Whitespace

Spaces and tabs are insignificant within a line (they are skipped during tokenization). They are significant at the start of a line, where they determine the indentation level. One tab is equivalent to four spaces.

Carriage returns (`\r`) are treated as insignificant inline whitespace.

### 2.3 Newlines

A newline (`\n`) ends a logical line. Blank lines (lines containing only spaces or tabs) are ignored entirely — they do not produce `NEWLINE` tokens and do not affect indentation processing.

### 2.4 Identifiers

```
identifier  ::= letter (letter | digit | '_')*
letter      ::= [a-zA-Z_]
digit       ::= [0-9]
```

Identifiers are case-sensitive. `score` and `Score` are distinct identifiers. No identifier may start with a digit.

### 2.5 Reserved Words (Keywords)

The following words are reserved and cannot be used as identifiers:

```
put   into   print   if   then   else   repeat   times
```

### 2.6 Number Literals

```
number  ::= digit+ ('.' digit+)?
```

Numbers are always stored internally as IEEE 754 double-precision floating-point values. Whole numbers are displayed without a decimal point. Multiple decimal points in a single token are a lexer error.

### 2.7 String Literals

```
string  ::= '"' character* '"'
character ::= any character except '"' and '\n'
            | escape_sequence
escape_sequence ::= '\n' | '\t' | '\\' | '\"'
```

String literals must open and close on the same line. An unmatched opening quote reaching end-of-line or end-of-file is a lexer error.

### 2.8 Operators and Symbols

| Token | Characters |
|---|---|
| `PLUS` | `+` |
| `MINUS` | `-` |
| `STAR` | `*` |
| `SLASH` | `/` |
| `EQUAL_EQUAL` | `==` |
| `NOT_EQUAL` | `!=` |
| `GREATER` | `>` |
| `GREATER_EQUAL` | `>=` |
| `LESS` | `<` |
| `LESS_EQUAL` | `<=` |
| `LEFT_PAREN` | `(` |
| `RIGHT_PAREN` | `)` |
| `COLON` | `:` |
| `COMMA` | `,` |

`=` and `!` alone (without `=` following) are lexer errors.

### 2.9 Layout Tokens

The lexer automatically inserts three layout tokens that the parser uses to detect block structure:

- `NEWLINE` — emitted at the end of every non-blank logical line
- `INDENT` — emitted when a new line has greater indentation than the current level
- `DEDENT` — emitted (once per level) when a new line returns to a lower indentation level
- `EOF` — emitted once, at the very end of the token stream

---

## 3. Data Types

Bloop has two runtime value types:

| Type | Java representation | Literal syntax |
|---|---|---|
| Number | `java.lang.Double` | `42`, `3.14`, `-7` |
| String | `java.lang.String` | `"hello"`, `"line\none"` |

There is no explicit Boolean type. Boolean values arise as the result of comparison expressions (`Double` operands → `java.lang.Boolean` result) and are consumed only by `if` conditions. A Boolean value cannot be stored in a variable or printed directly.

---

## 4. Grammar

The following is the complete context-free grammar for Bloop. Layout tokens (`INDENT`, `DEDENT`, `NEWLINE`) are shown where the parser explicitly expects them.

```
program         ::= statement* EOF

statement       ::= assign_stmt
                  | print_stmt
                  | if_stmt
                  | repeat_stmt

assign_stmt     ::= PUT expression INTO IDENTIFIER NEWLINE?

print_stmt      ::= PRINT expression NEWLINE?

if_stmt         ::= IF expression THEN COLON NEWLINE?
                    INDENT statement+ DEDENT
                    (ELSE COLON NEWLINE? INDENT statement+ DEDENT)?

repeat_stmt     ::= REPEAT expression TIMES COLON NEWLINE?
                    INDENT statement+ DEDENT

expression      ::= comparison

comparison      ::= addition (( EQUAL_EQUAL | NOT_EQUAL
                              | GREATER | GREATER_EQUAL
                              | LESS    | LESS_EQUAL ) addition)?

addition        ::= multiplication (( PLUS | MINUS ) multiplication)*

multiplication  ::= primary (( STAR | SLASH ) primary)*

primary         ::= MINUS primary
                  | LEFT_PAREN expression RIGHT_PAREN
                  | NUMBER
                  | STRING
                  | IDENTIFIER
```

Notes:
- Comparison is intentionally non-associative (no chaining: `a < b < c` is a parse error).
- Unary minus is right-associative and applies only at the primary level.
- Blocks (`INDENT … DEDENT`) must contain at least one statement; empty blocks are a parse error.

---

## 5. Expressions

### 5.1 Operator Precedence

Higher in the table means higher precedence (binds more tightly):

| Level | Operators | Associativity |
|---|---|---|
| 4 — Primary | literals, variables, `()`, unary `-` | right (unary) |
| 3 — Multiplicative | `*`, `/` | left |
| 2 — Additive | `+`, `-` | left |
| 1 — Comparison | `==`, `!=`, `>`, `>=`, `<`, `<=` | none |

### 5.2 Arithmetic Operators

All arithmetic operators require both operands to be Numbers. Applying an arithmetic operator to a String is a runtime error.

Division by zero is a runtime error.

### 5.3 Comparison Operators

`>`, `>=`, `<`, `<=` require both operands to be Numbers. Comparing a Number to a String with these operators is a runtime error.

`==` and `!=` compare two Numbers or two Strings. Comparing a Number to a String always returns `false` (for `==`) or `true` (for `!=`).

### 5.4 String Concatenation

There is no string concatenation operator. `+` is arithmetic only.

### 5.5 Unary Minus

Unary minus is syntactic sugar: `-expr` is parsed as `0 - expr`. It requires the operand to evaluate to a Number.

---

## 6. Statements

### 6.1 Assignment — `put`

```
put <expression> into <identifier>
```

Evaluates `expression` and binds the result to `identifier` in the environment. If the variable does not exist it is created. If it already exists its value is replaced. The new value need not be the same type as the old one.

### 6.2 Print — `print`

```
print <expression>
```

Evaluates `expression` and writes the result to standard output followed by a newline. Number formatting: if the value is a whole number (no fractional part and not infinite), it is printed as an integer (e.g. `5.0` → `5`). Otherwise the full decimal representation is used.

### 6.3 If / Else — `if`

```
if <condition> then:
    <then-body>
else:
    <else-body>
```

`condition` must evaluate to a Boolean. If it is not Boolean, a runtime error is raised. If the condition is `true`, the then-body executes. If it is `false` and an else-block is present, the else-body executes. The else-block is optional.

Both bodies must contain at least one statement. Empty blocks are a parse error.

### 6.4 Repeat Loop — `repeat`

```
repeat <count> times:
    <body>
```

`count` must evaluate to a non-negative whole Number. Fractional counts (e.g. `2.5`) and negative counts are runtime errors. Counts exceeding `Integer.MAX_VALUE` (2,147,483,647) are runtime errors. A count of `0` is valid — the body simply does not execute.

The body must contain at least one statement. The count expression is evaluated once before the loop starts; mutating a variable used in the count expression does not change the number of iterations.

---

## 7. Indentation and Block Structure

### 7.1 Rules

A block begins with the `:` at the end of an `if then:` or `repeat times:` line. The very next non-blank line must have strictly greater indentation than the line that opened the block. This generates an `INDENT` token.

A block ends when a non-blank line returns to the same or lower indentation level as the opener. One `DEDENT` token is emitted for each indentation level closed.

Dedenting to an indentation level that was never opened is a lexer error (IndentationError).

### 7.2 Consistency

Spaces and tabs may each be used as indentation within a program, but should not be mixed on the same line. One tab is always treated as four spaces when comparing indentation levels.

### 7.3 Nesting

Blocks may be nested to arbitrary depth. Each nested block must be indented further than its enclosing block.

```
repeat 3 times:
    if x > 0 then:
        print "positive"
    print "done"
```

In the above, `print "positive"` is inside the `if` block (8 spaces), and `print "done"` is inside the `repeat` block but outside the `if` block (4 spaces).

---

## 8. Execution Semantics

### 8.1 Environment

The environment is a single flat map from identifier names to values (`String → Object`). It is shared across all statements in a program. There is no scoping — variables created inside a block are visible after the block exits.

### 8.2 Variable Lifecycle

A variable comes into existence when first assigned with `put`. Reading a variable that has never been assigned is a runtime error.

### 8.3 Evaluation Order

Expressions are evaluated left-to-right. In a binary expression `left OP right`, `left` is always evaluated before `right`.

### 8.4 Short-circuit Evaluation

There are no logical operators (`and`, `or`, `not`), so short-circuit evaluation does not apply.

---

## 9. Error Conditions

All errors are fatal — the program stops at the first error encountered.

### 9.1 Lexer Errors (`BloopLexerException`)

| Condition | Example |
|---|---|
| Unterminated string (newline before closing quote) | `"hello` followed by newline |
| Unterminated string (EOF before closing quote) | `"hello` at end of file |
| Malformed number (multiple decimal points) | `3.14.15` |
| Unknown escape sequence | `"\z"` |
| Unrecognized character | `@`, `$`, `#` |
| Indentation error (dedent to unopened level) | Dedenting to 3 spaces when 4 and 0 are the only open levels |

### 9.2 Parse Errors (`BloopParseException`)

| Condition | Example |
|---|---|
| Unexpected token at statement start | `into x` as a statement |
| Missing keyword | `if x > 0:` (missing `then`) |
| Missing colon | `if x > 0 then` (missing `:`) |
| Empty block | An `if` or `repeat` block with no statements |
| Empty expression after `print` | `print` with nothing following |
| Unmatched parenthesis | `(1 + 2` without closing `)` |

### 9.3 Runtime Errors (`BloopRuntimeException`)

| Condition | Example |
|---|---|
| Undefined variable | `print x` when `x` was never assigned |
| Type error in arithmetic | `"hello" + 5` |
| Type error in comparison | `"hello" > 5` |
| Division by zero | `put 10 / 0 into x` |
| Non-Boolean condition in `if` | `if 42 then:` |
| Non-number repeat count | `repeat "five" times:` |
| Fractional repeat count | `repeat 2.5 times:` |
| Negative repeat count | `repeat -1 times:` |
| Repeat count too large | `repeat 3000000000 times:` |