
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

## Dot accessors

You can access object keys with `.key`, so that `.foo` (on the same
input) would give us:

```
{"bar" : [1,2,3,4,5]}
```

These can be chained, so `.foo.bar` would produce:

```
[1,2,3,4,5]
```

If your key contains special characters you can put it in quotes, so
while `.Key - Type Strange` would not work `."Key - Type Strange"`
would.

## Array indexing

You can also do array indexing with `[ index ]`, so that `.foo.bar[0]`
would give you `1`.

Array slicing is supported, so you can write `[1 : 3]` and get `[2,
3]`.  The first index is inclusive, and the last index is exclusive.

You can use negative indexes to refer to elements starting from the
end of the array, so that `[-1]` would return the last element of the
array, `5`. This also works with slicing, so to remove the first and
the last element, write `[1 : -1]`.

## JSON construction

Inside JSON you can write expressions anywhere you can write a JSON
literal, so (still working on same input) you could make a new object
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

## Functions

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

The full set of functions is documented on [another page](functions.md).

## For expressions

The `for` expression lets you take an array and transform it into a
new array, by evaluating an expression on each of the array elements.
The syntax is

```
for (<expr>)
  <expr>
```

The first expression, inside the parenthesis, evaluates to an array,
which we loop over. For each element in it the second expression is
evaluated (with `.` now referring to the current array element).

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

## If expressions

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

## Object for expressions

It's also possible to use `for` expressions to produce objects. As
before, we process each element in the input array separately, but in
this case we make a key in the object for each element in the array.
When the key and value are evaluated, `.` refers to the current array
element.

We can iterate over the same array we've been working on, and turn the
array into an object as follows:

```
{for (.foo.bar) string(.) : .}
```

This would produce the following object:

```
{
  "1" : 1,
  "2" : 2,
  "3" : 3,
  "4" : 4,
  "5" : 5
}
```

That's a rather strange object, of course, but we can also loop over
objects (with both types of `for` loop). Looping over an object turns
the object into an array, where each key/value pair in the object has
become an object of the form:

```
{"key" : the key, "value" : the value}
```

So let's say we want to process an object, and add `"custom"` as a
prefix to each of the existing keys. Let's say our input was:

```
{
  "foo" : 1,
  "bar" : 2
}
```

We can process this with:

```
{for (.) "custom_" + .key : .value}
```

and get:

```
{
  "custom_foo" : 1,
  "custom_bar" : 2
}
```

## Operators

The usual operators are supported. For arithmetic you can use `+`,
`-`, `*`, and `/` as expected.

`+` also works on strings, arrays, and objects. For objects, if the
same key appears on both sides, the value on the left will "win".

The usual comparison operators are also supported, so you could
process the 1-5 array with

```
for (.foo.bar) . > 2
```

and get

```
[false, false, true, true, true]
```

You can also combine boolean values with `and` and `or`. `not` is a
function.

## Object matching

In some cases, you want to insert or override some values in an
object, but leave the rest untouched. For example, let's say we want
to multiply `"foo"` by 10, but keep the rest of the object. This can
be done with object matching, where we write an object like this:

```
{
  "foo" : .foo * 10,
  * : .
}
```

This is a normal JSON object, except for the last part, `* : .`. The
`*` matches all keys in the input object, except for those that are
already specified (`"foo"` in this case) and copies them into the
output object. The `.` is a normal expression, evaluated with the
value of each key in turn.

So the output is:

```
{
  "foo" : 10,
  "bar" : 2
}
```

Since `.` is a normal expression, you could actually write:

```
{ * : . * 10 }
```

That would match and copy all keys, but all values would be multiplied
by 10, so the output would be:

```
{
  "foo" : 10,
  "bar" : 20
}
```

If there are some keys you don't want to copy you can explicitly omit
them:

```
{
  "foo" : .foo * 10,
  * - bar, baz, quux : .
}
```

This would produce:

```
{"foo" : 10}
```

If your input is a nested object you can still use the matching. So if
you write:

```
{
  "foo" : {
    "baz" : .hey.ho,
    * : .
  },
  "bar" : 24
}
```

Then this `*` will match the object inside the `"foo"` key in the
input object.

## Variables

You can set variables to break up complex computations, or to avoid
computing things more than once. The syntax is:

```
let foo = .foo
```

From that point on, you can refer to the value with `$foo`.

Let statements are allowed at the top level, at the start of objects,
inside `for` (before the expression), and inside `if`. Variables
defined inside an object, `for`, or `if` are only visible inside those
expressions.

It's also possible to set variables from the code running a JSTL 2.0
transform, so that the value is "injected" into the transform. This
can be useful for configuration, passwords, and similar values.