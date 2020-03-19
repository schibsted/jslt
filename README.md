
# JSLT

JSLT is a complete query and transformation language for JSON.  The
language design is inspired by [jq](https://stedolan.github.io/jq/),
[XPath](https://www.w3.org/TR/1999/REC-xpath-19991116/), and
[XQuery](https://en.wikipedia.org/wiki/XQuery).

JSLT can be used as:
 * a query language to extract values from JSON (`.foo.bar[0]`),
 * a filter/check language to test JSON objects (`starts-with(.foo.bar[0], "http://")`) ,
 * a transformation language to convert between JSON formats.

Here is an example transform:

```
{
    "time": round(parse-time(.published, "yyyy-MM-dd'T'HH:mm:ssX") * 1000),
    "device_manufacturer": .device.manufacturer,
    "device_model": .device.model,
    "language": .device.acceptLanguage,
    "os_name": .device.osType,
    "os_version": .device.osVersion,
    "platform": .device.platformType,
    "user_properties": {
        "is_logged_in" : boolean(.actor."spt:userId")
    }
}
```

[**Demo playground**](http://spt-data-dev-public-web.s3-website-eu-west-1.amazonaws.com/jstl2.html).

[**Language tutorial**](tutorial.md).

[**Function documentation**](functions.md).

[**More examples**](examples/README.md).

[![Build Status](https://travis-ci.org/schibsted/jslt.svg?branch=master)](https://travis-ci.org/schibsted/jslt)

## Quick reference

| Operation     | Explanation |
| ------------- | ------------- |
| `.`             | The context node  |
| `.<name>`       | [Get value of key](tutorial.md#dot-accessors) `"<name>"` inside an object  |
| `.[<index>]`          | [Get value](tutorial.md#array-indexing) `<index>` inside an array |
| `.[<from> : <to>]`     | [Array slicing](tutorial.md#array-indexing) |
| `if (<expr>) <expr> else <expr>` | [If test](tutorial.md#if-expressions) to decide which value to return |
| `let <name> = <expr>` | [Define a variable](tutorial.md#variables) |
| `$<name>`             | [Refer to a variable](tutorial.md#variables) |
| `[for (<expr>) <expr>]` | [Transform an array](tutorial.md#for-expressions) |
| `{for (<expr>) <expr> : <expr>}` | [Transform an object](tutorial.md#object-for-expressions) |
| `def <name>(<name>, <name>...) <expr>` | [Declare a function](tutorial.md#function-declarations) |
| `// <anything up to end of line>` | Comment |
| `{ <key> : <expr> }`               | [Object constructor](tutorial.md#json-construction) |
| `{ <key> : <expr>, * : . }`        | Specify one key, [copy rest of input](tutorial.md#object-matching) |
| `5 * 7 + 23.2`        | Arithmetic operations |
| `7 < 5`        | Comparators |
| `7 < 5 and .foo == "yes"` | Boolean operators |

## Using the library

To include JSLT in your project, depend on:

```
<dependency>
  <groupId>com.schibsted.spt.data</groupId>
  <artifactId>jslt</artifactId>
  <version>0.1.9</version>
</dependency>
```

At runtime JSLT depends on Jackson, and nothing else.

To transform one `JsonNode` into another, do:

```
import com.schibsted.spt.data.jslt.Parser;
import com.schibsted.spt.data.jslt.Expression;

JsonNode input = ...;
Expression jslt = Parser.compileString(transform);
JsonNode output = jslt.apply(input);
```

For more alternatives, see [the
javadoc](http://javadoc.io/doc/com.schibsted.spt.data/jslt).

## Command-line

To run transforms on the command-line, first build with `./gradlew
clean shadowJar`. Then you can run with:

```
java -cp build/libs/*.jar com.schibsted.spt.data.jslt.cli.JSLT transform.jslt input.json
```

The result is written to standard out.

## Extension functions

You can implement your own functions and add them to the language.
See [the extension function tutorial](extensions.md).

## Feedback

If you have questions about how to use JSLT, please ask the question
on StackOverflow, with the tag `jslt`.

If you have problems, feature requests, or think you found a bug,
please open an issue.

## Status

The language design is not finished, so features may be added. The
language as it stands is not likely to change.

The entire language is implemented, and all of the function library.
Functions may be added.

The language has been used in production at Schibsted since January
2018, performing about 9 billion transforms per day, and many times
more queries.

## More information

Developing a language for JSON processing: [video of
talk](https://vimeo.com/289470470), [slides
only](https://www.slideshare.net/larsga/jslt-json-querying-and-transformation).

Anthony Sparks is working on a
[VM-based implementation in Java](https://github.com/tonysparks/jslt2)

[A paper](https://arxiv.org/abs/1908.10754) describing (among other
things) some of the ways Schibsted uses JSLT.

[Visual Studio syntax highlighter](https://marketplace.visualstudio.com/items?itemName=jarno-rajala.jslt-lang) for JSLT.

[Apache Camel JSLT component](https://camel.apache.org/components/latest/jslt-component.html).

## LICENSE

Copyright (c) 2018 Schibsted Marketplaces Products & Technology AS

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at `http://www.apache.org/licenses/LICENSE-2.0`

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## What is missing

Things to be done:
  * Move the tests out into YAML files.
  * Write a proper spec with EBNF and everything.
  * Fix the syntax ambiguity problem with `let` and `def`.
  * Implement toString() throughout the object tree, so that it's
    possible to turn expressions back to strings.
  * Optimizer:
     * Optimize `... and boolean( ... )` by removing `boolean()`.
     * Implement parse tree traversal API.
     * Make sure entire tree is traversed (inside function decls and
       variables, for example).
     * Complete constant folding. Particularly constant folding for variables
       would be valuable.
     * Inlining of functions.
     * Eliminate unused variables.
  * Use property-based testing and fuzz testing to harden the parser.

See also [the list of ideas](ideas/).
