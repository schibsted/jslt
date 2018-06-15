
# Convenient query API

Right now there is only `apply(JsonNode) -> JsonNode`, which doesn't
really have the best Java integration. What would be nice would be to
return proper Java types. Unfortunately, we don't know what type the
user wants, so a general API is difficult. The user knows, however, so
we could make one method for each alternative.

That is, we could extend `Expression` with methods like these:

```
public String queryString(JsonNode input);

public Collection<String> queryStrings(JsonNode input);

public boolean queryBoolean(JsonNode input);

public int queryInt(JsonNode input);

public Collection<Integer> queryInts(JsonNode input);

public long queryLong(JsonNode input);

public Collection<Long> queryLongs(JsonNode input);
```

That should make it much easier to use the API in Java code.

Another possibility is to have a `Jslt` class which has direct methods
like:

```
public JsonNode apply(String query, JsonNode input);

public JsonNode queryString(String query, JsonNode input);

// ...
```

This saves the user from having to first compile and then apply the
expression. The performance cost of doing compilation over and over
again could be resolved by having a simple cache inside the class.
