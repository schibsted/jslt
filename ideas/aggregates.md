
# Aggregate functions

A recurring need is for functions that compute aggregates from a
sequence. Obvious examples are `sum`, `average`, `any`, and `all`.

These are all easily implementable as user-defined functions, but the
result is not very efficient, since it has to be formulated like this:

```
def sum(numbers)
  if ($numbers)
    $numbers[0] + sum($numbers[1 : ])
  else
    0
```

The function creates `n` sub-arrays for an array of `n` elements.

We could build the most commonly wanted aggregates into the language,
but it's not clear that this is a closed set. Users may easily want
aggregates that we don't add, and we run the risk of bloating the
library, and even then there may be more user-specific examples that
we don't cover.

Another approach would be to add a macro like this:

```
reduce(sequence, reduce expression)
```

Returns the sequence reduced to a single value by computing the reduce
expression on pairs of values. The reduce expression gets the two
values as the variables `$left` and `$right`. If the sequence is empty
the result is `null`.

Using this we could implement `sum`, `average`, `any`, and `all` easily
and efficiently:

```
def sum(numbers)
  reduce($numbers, $left + $right)

def average(numbers)
  sum($numbers) / size($numbers)

def any(booleans)
  reduce($booleans, $left or $right)

def all(booleans)
  reduce($booleans, $left and $right)
```

In the end it comes down to two decisions that need to be made:

  * Implement `reduce`: yes/no.

  * Implement some set of aggregation functions: decide which, if any.

`map` is usually thought of as a natural companion to `reduce`, but
the `[for (<expr>) <expr>]` already effectively provides `map`, so
there is no need for that macro.
