
# JSTL 2.0

JSTL 2.0 is a redesign of JSTL that does *not* use jq as the query
language, because JSTL 2.0 is a complete query and transformation
language in one. The language design is inspired by
[jq](https://stedolan.github.io/jq/),
[XPath](https://www.w3.org/TR/1999/REC-xpath-19991116/), and
[XQuery](https://en.wikipedia.org/wiki/XQuery).

This has the following benefits:
 * better handling of missing data,
 * no more ugly `${ ... }` wrappers around the jq queries,
 * much faster implementation,
 * safe transforms (limited expressivity means there's no way to
   write transforms that use up all memory or that never terminate),
 * introspectability (programs can analyze the expressions to see what
   they do),
 * sane syntax for `not`,
 * sane syntax for function calls,
 * much easier to call Java methods as JSTL functions,
 * no dependencies on the Scala runtime library,
 * no dependencies on the parser generator,
 * implementation no longer bound to Jackson.

[**Demo playground**](http://spt-data-dev-public-web.s3-website-eu-west-1.amazonaws.com/jstl2.html).

## Status

This is still *very* much a work in progress. In fact, it's nothing
like feature-complete. So don't even think about using it.

What is working:
 * JSON parsing.
 * Comments.
 * Dot key accessors.
 * Function calls.
 * `if`, `let`, and `for` statements.
 * Variables.
 * Operators `+`, `-`, `*`, and `/`.
 * Boolean comparators `==`, `!=`, and `>=`.
 * Boolean operators `and` and `or`.
 * The `number`, `round`, `fallback`, `not`, `test`, `capture`, `split`, `join`,
   `is-array`, `is-object`, `starts-with`, `ends-with`, `contains`, `size`,
   and `lowercase` functions.
 * `(` Parenthetical expressions `)`.
 * Array and string indexing and slicing.
 * Object matching (`* : .`).

[pulse-cleanup.jstl](cleanup.jstl2) has been translated to JSTL 2.0
and works. A performance test on 89,100 Pulse events ran the old JSTL
1.0 transform in ~6.3 seconds, and the new JSTL 2.0 in ~0.7 seconds.

## What is missing

The following is still missing:
  * Escape syntax in strings.
  * The rest of the boolean comparators.
  * `contains` should support strings and objects.
  * The rest of the function library (not 100% designed yet).
  * More detailed definition of language semantics, especially error
    situations.
  * Adding all the object matcher tests from 1.0.
  * Documentation.
  * Optimizations: complete constant folding.
  * Many more tests.

## Examples of improvements

In DQT checks used to be written like this, using jq:

```
.published | select(test("regexp"))
```

With JSTL 2.0 you can write:

```
test(.published, "regexp")
```

The `select` used to be necessary to avoid the function crashing on
missing data. With JSTL 2.0 the pipe disappears, and the function will
just return `false` if the input is `null`.

Similarly, testing if a value was one of a number of alternatives used
be done like this:

```
.object."@type" | (. == "Article" or . == "ClassifiedAd" or . == "Content" or
                   . == "Product")
```

In JSTL 2.0 you can use the `contains` function:

```
contains(.object."@type", ["Article", "ClassifiedAd", "Content", "Product"])"
```

The first part of pulse-cleanup also becomes simpler. Here is the
original:

```
  "actor" : {
    // first find the user ID value
    let userid = if ${ .actor."@id" | tostring | select(test("^(sd|u)rn:[^:]+:(user|person):.*")) }
      ${ .actor."@id" }
    else
      ${ .actor."spt:userId" }
```

In JSTL 2.0 this reduces to:

```
  "actor" : {
    // first find the user ID value
    let userid = if (test(.actor."@id", "^(sd|u)rn:[^:]+:(user|person):.*"))
      .actor."@id"
    else
      .actor."spt:userId"
```

because we no longer need the `${ ... }` wrappers around jq
expressions, `test` now converts its arguments to strings by default,
and `select` is no longer needed.