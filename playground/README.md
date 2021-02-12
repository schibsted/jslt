
# JSLT Playground

To build the playground yourself, simply do:

```
./gradlew :playground:shadowJar
```

That will make the file `playground/build/libs/playground-0.0.1-all.jar`

To run it, give this command:

```
java -cp playground/build/libs/playground-0.0.1-all.jar no.priv.garshol.jslt.playground.PlaygroundServer 9999 &
```

Then go to `http://localhost:9999/jslt` and you'll have the playground
right there.
