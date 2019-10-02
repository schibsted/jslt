
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
