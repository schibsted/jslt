
# Sorting

The need for being able to sort JSON output comes up repeatedly, and
is a natural need. The question is how to work it into the language.

## What can be sorted?

Only arrays can be sorted.

## Syntax

Two obvious approaches present themselves immediately: a macro or an
`order by` operator.

### Custom syntax

One could imagine adding an `order by` to list comprehensions:

```[for (...) ... if (...) order by ... asc|desc]```

The same for object comprehensions doesn't make sense.

It would also be possible to have `order by` as simply an operator:

```.foo.bar order by .key```

Formally: `<expr> order by <expr>`, with priority to be established.

### Macro

A macro `sort` would work:

```sort(<array to sort>, <expression to produce sort key>?)```

Thus:

```
sort([5, 1, 3])           => [1, 3, 5]
sort([5, 1, 3], -1 * .)   => [5, 3, 1]
```

One could also imagine a third parameter to choose between ascending
and descending sort. However, a `reverse()` function might be useful
for other purposes, too, so perhaps it would be better to add that,
too.

## Value ordering

Numbers are easy: ordered according to natural numeric order.

Strings are also easy: ordered according to Unicode code point. We
make no attempt to support custom collations based on language or
other preferences. Anyone wanting that will need to define functions
to generate sort keys.

Booleans: `false` sorts before `true`.

Arrays: sorted according the nth element, beginning with the first. If
the nth element of one array is smaller, then so is the array. If one
array has no element in the nth position, that array is smaller. If
the elements are equal then comparison moves to `n+1`.

Objects? Sorting them is not really very nice. Python 3 does not allow
sorting dicts at all, but Python 2 did. However, refusing to sort
arrays with mixed types can easily lead to errors if the input is not
shaped as expected, and we generally try to avoid that. So objects
should be sortable.

We don't really care about the order of objects, so for the sake of
speed objects compare by size first. Then by the smallest key. Then by
the value of the smallest key. If the smallest keys are the same
comparison moves to the next key.

The order of the types is `null`, booleans, numbers, strings, arrays,
objects.

# Use case solutions

## Original motivation

From [the original issue](https://github.com/schibsted/jslt/issues/172#issue-781566400) we have this example: "e.g. you can sort by
the score field in the following example:"

```[ { "id": "a", "score": 4}, { "id": "b", "score": 3}]```

There is some missing context here, but with an operator this would be:

```. order by .score```

With a macro it would be:

```sort(., .score)```

Github user hemakumarg89 also wanted to sort "json objects in an
array(based on a field value)". That's basically the same need.

CONSIDER THE SAME THING IN A MORE EMBEDDED CONTEXT

## StackOverflow example

From StackOverflow comes [a slightly more advanced example](https://stackoverflow.com/questions/76236450/jslt-are-there-global-variables).

There is a defined ranking of keys:

```let ranking = {
 "DEC": 0,
 "SBS": 1,
 "CON": 2,
 "GCS": 3,
 "GMS": 4,
 "FXP": 5,
 "QAN": 6,
 "REF": 7,
 "PRO": 8
}
```

The input (presumably) looks something like:

```
[
  {..., "tag" : "CON"},
  {..., "tag" : "REF"},
  {..., "tag" : "DEC"},
  ...
]
```

and we want the value with the lowest ranking.

With an operator this would be:

```
{
  "data": {
    "segment": (.payload order by get-key($ranking, .tag)) [0]
   }
}
```

whereas with a macro it would be:

```
{
  "data": {
    "segment": sort(.payload, get-key($ranking, .tag))[0]
   }
}
```