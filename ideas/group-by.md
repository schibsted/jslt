
# Group by

Being able to take a set of values and group them by a key is very
often necessary in more complex transformations. [Here's an
example](https://github.com/schibsted/jslt/issues/16) use case.

The natural way to support this is to extend the array for
expressions, so that one can write:

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

## jq approach in JSLT

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

[for (.)
  let key = .parent
  {"parent" : $key, "children" : [for (.) .id]}
group by $key]

The benefit is that we get the simple structure from jq without having
to re-evaluate the key.

The downside is that solutions become a little longer because of the
variable that has to be introduced, and that the implementation
becomes a little more complex, because it is no longer simply a
preprocessing step.

## Others

Comparable functionality exists in SQL.
