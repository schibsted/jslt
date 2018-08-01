
# Group by

Being able to take a set of values and group them by a key is very
often necessary in more complex transformations. [Here's an
example](https://github.com/schibsted/jslt/issues/16) use case.

One way to support this is to extend the array for expressions, so
that one can write:

```
[for (<expr>)
  <expr>
group by <expr>]
```

This would then loop over not the original array produced by the first
expression, but would take that, and group the values in that array by
key (the value computed by the `group by`).

So for expression would actually iterate over a structure like this:

```
[
  {"key" : key1,
   "values" : obj1, obj2, obj3, ... },

  {"key" : key2,
   "values" : obj4, obj5, obj6, ... },

  ...
]
```

Let's look at an example to make it more concrete. Let's say this is
the input:

```
[
  {"id" : 5, "parent" : 72},
  {"id" : 3, "parent" : 2},
  {"id" : 1, "parent" : 72},
  {"id" : 4, "parent" : 1},
  {"id" : 7, "parent" : 2}
]
```

We could now rewrite this to a normalized structure by doing:

```
[for (.)
  {"parent" : .key, "children" : [for (.values) .id]}
group by .parent]
```

The output would be:

```
[
  {"parent" : 72, "children" : [5, 1]},
  {"parent" : 2, "children" : [3, 7]},
  {"parent" : 1, "children" : [4]}
]
```

## jq

The functionality is implemented by the `group_by(<expr>)` macro,
which also does sorting. The output is different. In jq, the same
input processed with

```
group_by(.parent)
```

would produce

```
[
  [
    {"id" : 4, "parent" : 1},
  ],
  [
    {"id" : 3, "parent" : 2},
    {"id" : 7, "parent" : 2}
  ],
  [
    {"id" : 5, "parent" : 72},
    {"id" : 1, "parent" : 72}
  ]
]
```

This means to get the same output one would need to recompute the key,
which is of course possible. It can be implemented like this:

```
group_by(.parent) |
  map({"parent" : .[0].parent, "children" : . | map(.id)})
```

[Another example](https://stackoverflow.com/questions/43221453/jq-group-and-key-by-property).

## jq structure in JSLT

If `group by` in JSLT produced the same structure as in jq, the
implementation would look as follows:

```
[for (.)
  {"parent" : .[0].id, "children" : [for (.) .id]}
group by .parent]
```

The biggest difference is having to recompute the key, which looks a
little odd. An array of arrays may also be slightly faster than
building an array of objects.

## Execution order

Array comprehensions already support `if` filters, and in the future
they are very likely to also get `order by`. If this happens, the
question becomes what the order of operations should be. The syntax
might be

```
[for (<for expr>)
  <let bindings>
    <out expr>
  if (<if expr>)
  group by <grp expr>
  order by <sort expr>]
```

The order of operations would then be:

 * The `for expr` is evaluated, producing the input array.
 * Any let bindings are evaluated, producing variable bindings
   that will remain available throughout.
 * The input array is filtered with the `if expr`.
 * The filtered array is grouped on the `grp expr`.
 * The grouped structure is sorted with `sort expr`.
 * The sorted array is mapped with the `out expr`.

The `for expr` is evaluated on the original context.

Then iteration over the result value begins, with the context node
being the iteration value for each value, and the let bindings and if
expression are both evaluated on that context.

The grouping expression is also evaluated on the same context, but
after that it gets complicated. The sort and out expressions should
both be evaluated on a different context, which is the context after
the group by transform.

## Alternatives to array comprehensions

It's not a given that `group by` must be part of array comprehensions.
For example, a `group-by(<input expr>, <grouping expr>)` macro could
be introduced that only does the grouping transform and nothing else.

With that the original example would be implemented as:

```
[for (group-by(., .parent)) {
  "parent" : .key,
  "children" : [for (.values) .id]
}]
```

Ordering could here be done in the normal way, since it would
obviously operate on the post-grouping structure. The downside is that
filtering would now have to be done separately to happen before the
`group-by`, turning the implementation into something like:

```
[for (group-by([for (.) . if (<expr>)], .parent)) {
  "parent" : .key,
  "children" : [for (.values) .id]
}]
```

That would not be very readable without the use of variables.
However, with the pipe operator it might not be so bad:

```
[for (.) . if (<expr>)] |
  group-by(., .parent) |
  [for (.) {
    "parent" : .key,
    "children" : [for (.values) .id]
  }]
```

Order by could now be either part of the array comprehension, or a
separate macro after.

## XQuery

In XQuery the `group by` expression [must be a variable](https://www.w3.org/TR/xquery-30/#id-group-by). This means that you have to write something like:

```
for $groupedvals in <expr>
  let $key = <expr>
  group by $key
  return <some-element>{
    <expr-to-produce-contents>
  }</some-element>
```

With this approach, each iteration of the `for` will be evaluated with
the grouping value in `key` (and there may actually be several
variables), and the grouped input values in `groupedvals`.
Effectively, the two variables perform the same function as the keys
inside the temporary objects in the JSLT proposal above.

## XQuery approach in JSLT

We could try the same thing in JSLT: require the `group by` to be a
variable, and let the structure that the array comprehension is
evaluated on be the same as in jq. With that the implementation would
be:

```
[for (.)
  let key = .parent
  {"parent" : $key, "children" : [for (.) .id]}
group by $key]
```

The benefit is that we get the simple structure from jq without having
to re-evaluate the key.

The downside is that solutions become a little longer because of the
variable that has to be introduced, and that the implementation
becomes a little more complex, because it is no longer simply a
preprocessing step.

The biggest issue, however, is how this affects the ordering/context
mixing issue. Now the situation will be that the if, group by, and
order by all work on the pre-grouping input (but the key is
available), but the out expression works on the post-grouping output.
Will this be confusing? Maybe.

## What is better, grouping in comprehensions or a macro?

The benefit of having everything in comprehensions is that you get to
express the whole computation as one unit, with the expression
contexts set up just right for what you're doing. And since your
expression is at a high semantic level doing optimizations is easy.

The downside is that understanding the model becomes a little more
complicated, and you have to learn the order of execution and the
execution context for each expression.

The macros with the pipe operator are more explicit, but also more
low-level.

## Others

Comparable functionality exists in SQL.
