
# JSTL 2.0

JSTL 2.0 is a redesign of JSTL that does *not* use jq as the query
language, because JSTL 2.0 is a complete query and transformation
language in one.

This has the following benefits:
 * better handling of missing data,
 * no more ugly `${ ... }` wrappers around the jq queries,
 * much faster implementation by compiling to Java bytecode,
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
 * Simple dot key accessors.

Next goal is to have this transform working (a part of
`pulse-cleanup.jstl`):

```
{
  "location" : {
    "latitude" : number( if (.location.latitude)
                           .location.latitude else .latitude)),
    "longitude" : number( if (.location.longitude)
                            .location.longitude else .longitude)),
    "accuracy" : number(.location.accuracy)
  }
}
```

That will require having `if` implemented as well as function calls
and the `number` function.

(In the future there will perhaps be a "fallback" operator or
function, so that it becomes possible to write this as:

```
{
  "location" : {
    "latitude" : number( fallback(.location.latitude, .latitude) ),
    "longitude" : number( fallback(.location.longitude, .longitude) ),
    "accuracy" : number(.location.accuracy)
  }
}
```

)