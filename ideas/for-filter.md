
# Filtering in `FOR` expressions

The `FOR` list comprehension is currently used to transform arrays and
dynamically transform objects. It's a natural place to put the filter
operation, because when we are making the list we already have the
node we want to operate on as the context, and it avoids having to
make list and then filter it.

Another point is that sorting and grouping is also naturally done
here, which is another reason to have filtering in the same place.

If we look at pure filtering, with a `filter` macro it looks like
this:

```
filter(.path.to.array, number(.price) > 20)
```

In the for expression it would look like so:

```
[for(.path.to.array) . if(number(.price) > 20)]
```

A more likely scenario is the one where we want the instance ID of all
EBS-optimized EC2 instances, which would look as follows with `filter`:

```
[for (filter(.Reservations.Instances, .EbsOptimized)) .InstanceId]
```

The for-filter is clearly more readable:

```
[for (.Reservations.Instances) .InstanceId if (.EbsOptimized)]
```

See also [this solution](https://github.com/schibsted/jslt/issues/14#issuecomment-407806222) to an issue raised by a user.

## Syntactic ambiguity

One issue is whether we may have an ambiguity at the end of the
statement, so that it's not clear whether the expression continues, or
whether we are looking at the `if` filter.

However, the only ambiguity we have at the moment is with the end of
dotted paths, and `if` expressions cannot be part of a dotted path, so
the ambiguity is not really possible.
