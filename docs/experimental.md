
# JSLT experimental module

JSLT has a library of [../functions.md](built-in functions), but it
also has a second library of experimental functions. These have not
yet been added to the language proper, because it's not clear if the
design is right yet. Having them in the experimental library allows
users to try them out and find issues with them before they become
part of the language proper.

The functions in this module may be replaced by better alternatives,
if better alternatives are found.

To import the experimental module, do:

```
import "http://jslt.schibsted.com/2018/experimental" as exp
```

## _group-by(sequence, key expression, value expression) -> array_

This is not actually a function, but a macro. For each element in the
input sequence it computes the key expression with that element as the
context node, which produces a key. The grouped value for that key is
computed by the value expression, with the element as the context
node.

The output is an array of objects of this form:

```
[
  {"key" : <key>, "values" : [<value1>, <value2>, <value3>, ... ]},
  {"key" : <key>, "values" : [<value1>, <value2>, <value3>, ... ]},
  {"key" : <key>, "values" : [<value1>, <value2>, <value3>, ... ]},
  ...
]
```

All the elements in the input array that have the same key are grouped
together in the list of values for that key.

An example may be easier to follow. Let's say we want to group these
numbers by their first digit:

```
[673, 642, 320, 777, 351, 983, 359, 641, 950, 205]
```

We can do that like this:

```
import "http://jslt.schibsted.com/2018/experimental" as exp

exp:group-by(., string(.)[0], .)
```

This works because the first argument takes the input (the list of
numbers above) and feeds it to `group-by`.

The second expression is evaluated on each of those numbers, giving us
the first digit in the number as a string. That's the key to group by.

The third expression is also evaluated on each of those numbers, but
since it's just `.` we simply get the number itself as the value to
group.

The output is:

```
[ {
  "key" : "2",
  "values" : [ 205 ]
}, {
  "key" : "3",
  "values" : [ 320, 351, 359 ]
}, {
  "key" : "6",
  "values" : [ 673, 642, 641 ]
}, {
  "key" : "7",
  "values" : [ 777 ]
}, {
  "key" : "9",
  "values" : [ 983, 950 ]
} ]
```

That's a slightly contrived example, so here comes a more realistic,
but also more complicated one. Let's say you want to group by the keys
A, B, and C in this input:

```
{
  "data": [
    {
      "A": 12,
      "B": 23,
      "C": 15
    },
    {
      "A": 22,
      "B": 13,
      "C": 16
    }
  ]
}
```

First we need to flatten this out, which we can do with:

`flatten([for (.data) array(.)])`

This produces:

```
[ {
  "key" : "A",
  "value" : 12
}, {
  "key" : "B",
  "value" : 23
}, {
  "key" : "C",
  "value" : 15
}, {
  "key" : "A",
  "value" : 22
}, {
  "key" : "B",
  "value" : 13
}, {
  "key" : "C",
  "value" : 16
} ]
```

Now we can use `group-by`:

```
import "http://jslt.schibsted.com/2018/experimental" as exp

let step1 = flatten([for (.data) array(.)])
exp:group-by($step1, .key, .value)
```

This will output:

```
[ {
  "key" : "A",
  "values" : [ 12, 22 ]
}, {
  "key" : "B",
  "values" : [ 23, 13 ]
}, {
  "key" : "C",
  "values" : [ 15, 16 ]
} ]
```

If you want to get down to an object with A-C as the keys and the
numbers are the values that can be done, too:

```
import "http://jslt.schibsted.com/2018/experimental" as exp

let step1 = flatten([for (.data) array(.)])
let step2 = exp:group-by($step1, .key, .value)

{for ($step2) .key : .values}
```

Now the output is:

```
{
  "A" : [ 12, 22 ],
  "B" : [ 23, 13 ],
  "C" : [ 15, 16 ]
}
```
