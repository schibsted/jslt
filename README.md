
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
let user_id = fallback(.actor."spt:userId", .account."@id")

{
    "event_type" :  if (contains("backend", .schema)) (
      "Backend - "+ ."@type" +" "+ .object."@type"
    ) else
      ."@type" + " " + .object."@type",
    "device_id" : sha256-hex( $salt + .device.environmentId ),
    "time": round(parse-time(.published, "yyyy-MM-dd'T'HH:mm:ssX") * 1000),
    "device_manufacturer": .device.manufacturer,
    "device_model": .device.model,
    "language": .device.acceptLanguage,
    "os_name": .device.osType,
    "os_version": .device.osVersion,
    "platform": .device.platformType,
    "user_id" : if ($user_id) sha256-hex( $salt + $user_id) ,
    "user_properties": {
        "is_logged_in" : boolean($user_id)
    }
}
```

[**Demo playground**](http://spt-data-dev-public-web.s3-website-eu-west-1.amazonaws.com/jstl2.html).

[**Language tutorial**](tutorial.md).

[**Function documentation**](functions.md).

## Quick reference

| Operation     | Explanation |
| ------------- | ------------- |
| `.`             | The context node  |
| `.<name>`       | Get value of key `"<name>"` inside an object  |
| `.[<index>]`          | Get value `<index>` inside an array |
| `.[<from> : <to>]`     | Array slicing |
| `if (<expr>) <expr> else <expr>` | If test to decide which value to return |
| `let <name> = <expr>` | Define a variable |
| `$<name>`             | Refer to a variable |
| `[for (<expr>) <expr>]` | Transform an array |
| `{for (<expr>) <expr> : <expr>}` | Transform an object |
| `def <name>(<name>, <name>...) <expr>` | Declare a function |

## Status

The language design is not finished, so features may be added. The
language as it stands is not likely to change.

The entire language is implemented, and all of the function library.
Functions may be added.

The language has been used in production at Schibsted since January
2018, performing about 9 billion transforms per day, and many times
more queries.

## Using the library

To include JSLT in your project, depend on *coming*

JSLT depends on Jackson.

*introduce API here (but review first)*

## Command-line

To run transforms on the command-line, first build with `./gradlew
clean shadowJar`. Then you can run with:

```
java -cp build/libs/*.jar com.schibsted.spt.data.jstl2.JSTL transform.jstl input.json
```

The result is written to standard out.

## LICENSE

Copyright (c) 2018 Schibsted Marketplaces Products & Technology AS

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at `http://www.apache.org/licenses/LICENSE-2.0`

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## What is missing

Things to be done:
  * Move the tests out into JSON files.
  * Write a proper spec with EBNF and everything.
  * Fix the syntax ambiguity problem with `let`.
  * Implement toString() throughout the object tree, so that it's
    possible to turn expressions back to strings.
  * Optimizer:
     * Implement parse tree traversal API.
     * Make sure entire tree is traversed (inside function decls and
       variables, for example).
     * Complete constant folding. Particularly constant folding for variables
       would be valuable.
     * Inlining of functions.
     * Eliminate unused variables.
  * Avoid building more scope objects than necessary and avoid
    creating deep scope trees that require a lot of lookup.
  * Use property-based testing and fuzz testing to harden the parser.

See also [the list of ideas](ideas/).
