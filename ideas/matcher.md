
# Object matcher

This is internal notes on the semantics of the object matcher to get a
clearer handle on how it should work.

```
{
  "foo" : .bar,
  * : .
}
```

Here we match against the context node, `.`.

```
{
  "foo" : {
    "bar" : .baz,
    * : .
  }
}
```

In this case the matcher is inside the `"foo"` key, so we are actually
matching against `.foo`.

We can get more advanced, and say:

```
{ "a": "b" }
+
{
  "type" : "Anonymized-View",
  * : .
}
```

Here we should still be matching against `.`, since the matcher is not
inside an object. We could try to combine the complications:

```
{
  "foo" :
    { "a": "b" } +
    {
      "type" : "Anonymized-View",
       * : .
    }
}
```

This doesn't really make it more complicated, because we're matching
against `.foo` as if the `+` hadn't been there.

To work out what the matcher is matching against, start from the
object containing the matcher and work upwards through the syntax
tree.

Traversing nodes works as follows:
  * Object key: add the key to the matcher query. So if the matcher
    is inside `"foo" : <expr>` then prepend .foo to the query.

  * `def function(...) <expr>`: report an error. Object matchers
    cannot be grounded in functions, because there is no context node.

  * `[ <expr> ]`: array node or array comprehension: report an error,
    because we don't know how to compute the query here.

  * Other node types: ignore and continue upwards.

  * If there is no parent node: stop. You now have the query.

Now, let's say that we add the proposed pipe operator to change the
context node. If we do, we'll need to update these rules to add one
more:

  * Pipe operator `<left> | <right>`: if we come from the left, do
    nothing. If we come from the right: stop. You've completed the
    matcher query.

Let's check if this works.

```
{
  "foo" : .bar,
  * : .
} | .baz
```

Here we first create an object (with the matcher working on `.`) and
then make that the context node, before extracing `.baz`. So that
clearly works.

```
.baz | {
  "foo" : .bar,
  * : .
}
```

Here we pick out `.baz` from the input node, then make the result the
context node. The object matcher then works against that context node
(the matcher query is `.`). Again it seems to work.

```
{
  "foo" :
    { "a": "b" } |
    {
      "type" : "Anonymized-View",
       * : .
    }
}
```

The object matcher now works against the `a:b` object, so the output
would be:

```
{
  "foo" :
    {
      "type" : "Anonymized-View",
       "a": "b"
    }
}
```

As far as I can tell, these rules make sense and work.
