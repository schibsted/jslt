
# Predicates

If we introduce [the `..` operator](dotdot.md) and/or [dotting into
arrays](dot-on-array.md) there will be a need to remove the elements
in the array that are not wanted.

Let's say we want to know which EC2 instances are EBS optimized.

```
{
    "Reservations": [
        {
            "Instances": [
                { ... description of instance ...},
                { ... description of instance ...},
                { ... description of instance ...},
                { ... description of instance ...},
                { ... description of instance ...},
                ...
            ]
        }
    ]
}
```

If we have [dotting into arrays](dot-on-array.md) we can do

```
.Reservations.Instances
```

and get all the instances, but filtering the result is tricky.

A nice path-like way to solve this is using XPath predicates, where
one adds a parenthetical boolean expression to filter the array of
alternatives at that point. The predicate filters by evaluating the
expression for each element in the array with that element as the
context node and keeping only the ones for which the result is `true`.

Let's say the syntax is `[? <boolean expr> ]`. Then we could do:

```
.Reservations.Instances [? .EbsOptimized ] .InstanceId
```

This would give us the instance IDs of the EBS-optimized nodes only.

A sticky point is what the syntax should be. XPath has `[ <expr> ]`,
but that's ambigious, because it could also be an array expression.
Jsonata solves this by saying that if the result of the expression is
a boolean, then it is a predicate. JSONPath uses the `[? <expr> ]`
syntax.

There is a close relation here with the possible `IF` or `WHERE`
inside `FOR` loops ([for-filter](for-filter.md)).

The `filter` macro would be another candidate.