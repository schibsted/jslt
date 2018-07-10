
# Adding JSLT extension functions

JSLT can be extended by plugging in functions written in Java (or some
other JVM language). This can be done in two ways.

## Using the FunctionWrapper

If you have an existing static Java method that you want to call from
JSLT you can do it easily with the `FunctionWrapper`.  Let's say we
want to call `pow(double, double)` from `java.lang.Math`, which raises
the first number to the power of the second.

All we need to do is:

```
Collection<Function> functions = Collections.singleton(
  FunctionUtils.wrapStaticMethod("power", "java.lang.Math", "pow")
);
```

Now we've created a function and put it in a Java `Collection`.

Then:

```
JsonNode context = mapper.readTree("{}");
Expression expr = Parser.compileString("power(2, 10)", functions);
JsonNode actual = expr.apply(context);
```

This will return 1024, which is 2 to the power of 10.

## Implementing your own functions

However, if we prefer, we can implement our own functions, too.  Let's
say we want to implement this function ourselves. Then we can do as
follows:

```
public class PowerFunction implements Function {
  public String getName() {
    return "power";
  }

  public int getMinArguments() {
    return 2;
  }

  public int getMaxArguments() {
    return 2;
  }

  public JsonNode call(JsonNode input, JsonNode[] params) {
    int base = params[0].asInt();
    int power = params[1].asInt();

    int result = 1;
    for (int ix = 0; ix < power; ix++)
      result = result * base;

    return new IntNode(result);
  }
}
```

Calling this function is done in much the same way:

```
Collection<Function> functions = Collections.singleton(new PowerFunction());
JsonNode context = mapper.readTree("{}");
Expression expr = Parser.compileString("power(2, 10)", functions);
JsonNode actual = expr.apply(context);
```
