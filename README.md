
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
