
# The `filter` macro

In order to filter arrays one could define a macro `filter`, which
takes an array and an expression, and evaluates the expression once
for each element in the array. Something like this:

```
filter(.path.to.array, number(.price) > 20)
```

This can't be a function, because the second argument has to be
evaluated many times, but it's perfectly doable as a macro.

Alternatives are `FOR (...) ... IF (...)` [for-filter](for-filter.md)
and [predicates](predicates.md).