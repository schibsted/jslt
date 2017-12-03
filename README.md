
# JSTL 2.0

JSTL 2.0 is a redesign of JSTL that does *not* use jq as the query
language, because JSTL 2.0 is a complete query and transformation
language in one.

This has the following benefits:
 * better handling of missing data,
 * no more ugly `${ ... }` wrappers around the jq queries,
 * much faster implementation (first by byte-compiling to a custom
   virtual machine, later by compiling to Java bytecode),
 * no dependencies on the Scala runtime library,
 * no dependencies on the parser generator,
 * sane syntax for `not`,
 * sane syntax for function calls,
 * much easier to call Java methods as JSTL functions,
 * implementation no longer bound to Jackson.

This is still *very* much a work in progress. In fact, it's nothing
like feature-complete. So don't even think about using it.

What is working:
 * JSON parsing.
 * Comments.
 * Dot key accessors.
 * Function calls.
 * `if` and `let` statements.
 * Variables.
 * Boolean comparator `==`.
 * Arithmetic operator `+`.
 * The `number`, `fallback`, `not`, `test`, `capture`, and `split` functions.
 * `(` Parenthetical expressions `)`.
 * Array indexing.
 * Object matching (`* : .`).

## Working example

This transform now works (a part of `pulse-cleanup.jstl`):

```
  "actor" : {
    // first find the user ID value
    let userid = if ( test(.actor."@id", "^(sd|u)rn:[^:]+:(user|person):.*") )
      .actor."@id"
    else
      .actor."spt:userId"

    // then transform the user id into the right format
    let good_user_id =
      if ( test($userid, "^(u|sd)rn:[^:]+:user:null") )
        null // user modeling complains about these fake IDs
      else if ( test($userid, "(u|sd)rn:[^:]+:(person|user|account):.*") )
        // :person: -> :user: (and urn: -> sdrn:)
        let parts = capture($userid, "(u|sd)rn:(?<site>[^:]+):(person|user|account):(?<id>.*)")
        let site = if ( $parts.site == "spid.se" ) "schibsted.com" else $parts.site

        // Split the ID by : and pick the last element
        if ($parts.id)
          "sdrn:" + $site + ":user:" + split($parts.id, ":")[-1]

    "@id" : $good_user_id,
    "spt:userId" : $good_user_id,
    * : .
  }
```

## Next step

The next step is to finish translating all the matcher (`* : .`) test
cases from JSTL 1.0 to the JSTL 2.0 test suite.

## Possible extensions

In the future there will perhaps be a "fallback" operator or function,
so that it becomes possible to write the `location` cleanup as:

```
{
  "location" : {
    "latitude" : number( fallback(.location.latitude, .latitude) ),
    "longitude" : number( fallback(.location.longitude, .longitude) ),
    "accuracy" : number(.location.accuracy),
    * : .
  }
}
```
