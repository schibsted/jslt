
# JSTL 2.0

JSTL 2.0 is a complete query and transformation language for JSON.
The language design is inspired by
[jq](https://stedolan.github.io/jq/),
[XPath](https://www.w3.org/TR/1999/REC-xpath-19991116/), and
[XQuery](https://en.wikipedia.org/wiki/XQuery).

JSTL 2.0 can be used as:
 * a query language to extract values from JSON (`.provider.id`),
 * a filter/check language to test JSON objects (`starts-with(.provider.id, "sdrn:")`) ,
 * a transformation language to convert between JSON formats.

An example of a basic transform from the Pulse schema to the format
Amplitude expects:

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

The new language has the following benefits over JSTL 1.0:
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

[**Language tutorial**](tutorial.md).

[**Function documentation**](functions.md).

## Status

The language design is not finished, so features may be added. The
language as it stands is not likely to change.

The entire language is implemented, and all of the function library.
We may add some more functions, but essentially everything is
implemented.

[pulse-cleanup.jstl](cleanup.jstl2) has been translated to JSTL 2.0
and works. A performance test on 89,100 Pulse events ran the old JSTL
1.0 transform in ~6.3 seconds, and the new JSTL 2.0 in ~0.7 seconds.

Many transforms have been running in production for several months.
At the time of writing, JSTL2 performs ~7.5 billion transforms per day
and in addition about 20-30 billion filtering queries.

## Command-line

To run transforms on the command-line, first build with `./gradlew
clean shadowJar`. Then you can run with:

```
java -cp build/libs/*.jar com.schibsted.spt.data.jstl2.JSTL transform.jstl input.json
```

The result is written to standard out.

## What is missing

Things to be done:
  * Fix the syntax ambiguity problem with `let`.
  * Implement toString() throughout the object tree, so that it's
    possible to turn expressions back to strings.
  * More detailed definition of language semantics, especially error
    situations.
  * Optimizer: complete constant folding. Particularly constant
    folding for variables would be valuable. Also, need to make sure
    entire expression tree is traversed.
  * Avoid building more scope objects than necessary and avoid
    creating deep scope trees that require a lot of lookup.
  * Use property-based testing and fuzz testing to harden the parser.

See also [the list of ideas](ideas/).

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
to be done like this:

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