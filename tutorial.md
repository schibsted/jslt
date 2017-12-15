
# JSTL 2.0 tutorial

In JSTL 2.0 you write expressions. The expression evaluates to JSON.
So the result can be an object, a number, a string, null, etc.

JSON is a subset of JSTL 2.0, so anywhere you can write an expression
you can write JSON. JSON evaluates to itself.

This is valid JSTL 2.0, and will produce itself:

```
{"foo" : {"bar" : [1,2,3,4,5]}}
```

JSTL is always evaluated against an input, which is called "the
context node." If the JSON above is the input you can do simple
navigation with dot expressions. `.` always returns the context node,
so that would give us back the same JSON.

You can access object keys with `.key`, so that `.foo` (on the same
input) would give us:

```
{"bar" : [1,2,3,4,5]}
```

These can be chained, so `.foo.bar` would produce:

```
[1,2,3,4,5]
```

You can also do array indexing with `[ index ]`, so that `.foo.bar[0]`
would give you `1`.

Inside JSON you can write expressions anywhere you can write a JSON
literal, so (still working on same input) you could write a new object
like this:

```
{
  "array" : .foo.bar
}
```

which would then produce:

```
{
  "array" : [1,2,3,4,5]
}
```

JSTL has a number of built-in functions, like `size`, which returns
the number of elements in an object or array, or the number of
characters in a string. The function syntax is the common one, so we
could also write:

```
{
  "array" : .foo.bar,
  "size"  : size(.foo.bar)
}
```

which would then produce:

```
{
  "array" : [1,2,3,4,5],
  "size" : 5
}
```
