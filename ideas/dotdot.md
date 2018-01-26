
# The `..` operator

The use case is for JSON files that contain lists where one wants to
extract the same property from all the objects in the list. For
example, `aws ec2 describe-instances` returns the structure:

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

So if you want to find the IP address of all instances, you currently
have to write:

```
for (.Reservations)
  for (.Instances)
    .PrivateIpAddress
```

which will give you something like:

```
[ [ "10.0.0.90", "10.0.0.91", "10.0.0.92" ], [ "10.6.16.69" ], [ "10.0.0.177" ],
  [ "10.0.0.57" ], [ null ], [ "172.31.16.62" ], [ "172.31.7.19" ],
  [ "172.31.38.173" ], [ "10.6.16.122" ], [ null ], [ "172.31.23.235" ],
  [ "172.31.37.247" ], [ "10.0.1.20" ], [ "10.0.1.213" ], [ "172.31.17.39" ],
  [ "10.0.5.203" ], [ null, null, null, null, null, null, null ], ...
]
```

There are several issues with this:
  * Having to use `for`.
  * Having to nest the `for`s.
  * Ending up with nested arrays.
  * Getting `null` values.

The `..` operator, which is like `.`, except it also recurses through
sub-objects, would solve this. With that operator we could solve the
above with `..PrivateIpAddress`. Done.

The exact rules would be:
 * Can appear anywhere `.foo` can appear today.
 * Returns a list of all the matches.
 * For objects, if the key is there, add the value to the matches.
 * For objects, traverse all values that are objects or arrays.
 * For arrays, same.

## Related ideas

 * [Using `.foo` on arrays](dot-on-array.md)

## Schibsted relevance

We haven't seen any need for this crop up just yet.

## Other languages

[JsonPath](https://github.com/json-path/JsonPath) has this exact
operator.

[jsonata](http://jsonata.org/) doesn't seem to have any equivalent.

jq has something similar, but it's not exactly equivalent.