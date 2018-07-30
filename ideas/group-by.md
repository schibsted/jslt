
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

Comparable functionality exists in SQL and XQuery.
jq also supports [group by](https://stackoverflow.com/questions/43221453/jq-group-and-key-by-property).
