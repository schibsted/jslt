
# The `import` statement

Part of the point of introducing [functions](functions.md) was to make
it possible to modularize transforms so that they are more
maintainable.  For example, the many related `amplitude-*.jstl2`
transforms could all be based on a library of shared functions
(`amplitude-shared.jstl2`) for the parts of the logic that are shared.

However, for this to work it has to be possible for one transform file
to get logic from another file.

We have that same issue when it comes to the LoginStatePreserved
transform, which now basically does a simple test, then runs one of
two already existing transforms. It has to be implemented in Scala at
the moment, because there is no import function.

# The actual statement (simple)

The import statement could look like this:

```
import "reference/to/other/file.jstl"
```

where the import then makes all variables and functions defined in the
other file available in the current scope. (This includes variables
and functions imported from other files into `file.jstl`.)

The value of the actual transform would not be evaluated and would not
be available. We would need to modify the grammar so that not having a
final expression at all (only variables and functions) would be
allowed.

This would solve the amplitude use case, but for the state preserved
case to work we would need to rewrite those templates to be functions.
That can be done, of course.

# The actual statement (with prefix)

Import could also look like this

```
import "reference/to/other/file.jstl" as foo
```

Now all functions in that file would be available as `foo:function`
and all variables as `$foo:variable`. The transform itself would be
available as the function `foo(input)`. Anything imported into
`file.jstl` would be invisible.

With this we could still solve the amplitude use case, and now the
state preserving case would be solved by doing:

```
import "transforms/pulse-identified.jstl2" as identified
import "transforms/pulse-anonymized.jstl2" as anonymized

if (.actor."spt:userId" and not(ends-with(":null", .actor."spt:userId")))
  identified(.)
else
  anonymized(.)
```

## Refinements

We could perhaps allow both mechanisms. If you leave off the prefix
the import goes straight into your local scope with no prefix, and the
transform body (the top-level expression at the end) is not
accessible.  If you include the prefix you get the behaviour described
above.

# Resolving the file

By default, this should first attempt to resolve on the classpath. If
that fails, resolve in the file system.

However, the `Parser` should support plugging in a resolver that can
take the `"reference/to/other/file.jstl"` string and return a
`Reader`.  Those that need it can then override the resolution
mechanism in their own projects.
