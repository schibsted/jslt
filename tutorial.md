
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

The `for` expression lets you transform an array. The syntax is

```
for (<expr>)
  <expr>
```

The first expression evaluates to an array, which we loop over. For
each element in it the second expression is evaluated (with `.` now
referring to the current array element).

So if we want an array of strings instead, we can say:

```
{
  "array" : for (.foo.bar) string(.),
  "size"  : size(.foo.bar)
}
```

This will produce:

```
{
  "array" : ["1","2","3","4","5"],
  "size" : 5
}
```

We also have `if` tests, which use the syntax:

```
if (<expr>) <expr> else <expr>
```

`if` always returns a value and does nothing else. The `else` part can
be left out, in which case the `if` will evaluate to `null` if the
condition is false.

Boolean `false`, `null`, empty objects, and empty arrays are all
considered to be `false`. That means we could write our transform as
follows:

```
if (.foo.bar)
  {
    "array" : for (.foo.bar) string(.),
    "size"  : size(.foo.bar)
  }
else
  "No array today"
```

In this case, if there is nothing (or an empty array) in the
`.foo.bar` key you will get the string instead. If you want to
distinguish between an empty array and no array at all you can do an
explicit comparison:

```
if (.foo.bar != null)
  {
    "array" : for (.foo.bar) string(.),
    "size"  : size(.foo.bar)
  }
else
  "No array today"
```
