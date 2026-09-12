
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

## Run in JS (browser)

The playground page also offers **Run in JS**, which evaluates transforms
entirely in the browser using
[jslt-js](https://github.com/amkraev697642/jslt-js) loaded from
[npm](https://www.npmjs.com/package/jslt-js) via unpkg (`@latest`, so each page
load picks up the current published release). This works without building or
running the Java server — open the HTML via the server above, or host
`lambda.html` on any static file server (network required for unpkg).

The **Run (Java server)** button still uses the original Java JSLT engine via
POST (as on [garshol.priv.no/jslt-demo](http://www.garshol.priv.no/jslt-demo)).
