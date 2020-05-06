
# Using the JSLT API

Some features are only available through the API, and those are
documented here. For details, see [the
javadoc](http://javadoc.io/doc/com.schibsted.spt.data/jslt).

## Object key filter

By default, JSLT omits from the output object keys where the value is
`null` or an empty object or array. In some cases this is not
acceptable, and in those cases you can tweak the behaviour to be
whatever you want.

You can set the filter like this:

```
Expression e = new Parser(reader)
  .withObjectFilter(filter)
  .parse();
```

The filter can be either a string containing JSLT. The JSLT expression
needs to return `true` for the object values that should be included.
So to get the default behaviour you would set the filter to:

```
. == null or . == {} or . == []
```

If you simply want no object keys to ever be omitted, set it to:

```
true
```

If you don't want to set the filter using JSLT, you can implement the
`JsonFilter` interface in Java, and set the filter to be your new
object instead of a JSLT string. (Internally, the parser will
translate your JSLT string to a `JsonFilter` object using the JSLT
expression.)

## Passing values to JSLT

If you have one or more values in your code that you want to pass in
to the JSLT expression, you can do that through the API. Let's say
your code has a setting for the maximum string length of some field,
and you need your JSLT transform to obey that maximum.

To solve that you can do as follows:

```
Expression jslt = Parser.compileString(str);
JsonNode output = jslt.apply(
  Collections.singletonMap("maxLength", (JsonNode) new IntNode(cfg.getMaxLength())),
  input
);
```

Then, in your JSLT transform you can use `maxLength` as though it were
a normal variable, although the value comes from outside the JSLT:

```
{
  ...
  "title" : .document.name[ : $maxLength],
  ...
}
```
