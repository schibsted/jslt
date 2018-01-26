
# '.foo' on arrays

Just as with [the `..` operator](dotdot.md) being able to do `.foo` on
an array of objects is useful in cases where an array contains objects
and one wants to index into those objects.

For example, `aws ec2 describe-instances` returns the structure:

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

Getting the IP address of each instance is tricky with the current
JSTL 2 (see [the `..` operator](dotdot.md)), but simply by allowing
dot access on arrays we could solve it with:

```
.Reservations.Instances.PrivateIpAddress
```

Here the first step would return a list of objects, `.Instances` would
return the instances inside each, so that the result would be a flat
array of instances. Then, the third step would pick the
`.PrivateIpAddress` from each, and skip the ones that didn't have this
field.