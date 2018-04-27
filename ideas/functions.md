
# Functions

They could look like this:

```
def name(param1, param2)
  expression
```

The parameters would obviously be variables inside the function, and
executing the function itself is just evaluating the expression with
the parameter values bound to new variables.

A minor challenge is allowing functions to call functions declared
later in the source code, since currently we verify the existence of
functions before accepting the call, but this can easily be solved.

A larger challenge is that adding this feature makes the language
Turing-complete, so analysis of JSTL expressions becomes much more
challenging. On the other hand we have made no use of this possibility
so far, and it's not clear that this is a real need.

On the positive side, the expressiveness of JSTL will increase
dramatically. Given a `sum` function, one could implement a function
counting the keys in an object this way:

```
def count(node)
  if (is-object($node))
    size($node) + sum([for ($node) count(.value)])
  else if (is-array($node))
    sum([for ($node) count(.)])
  else
    0
```

The key point is that functions allow recursive traversal of the JSON
structure, so that basically any computation becomes expressible. If
`sum` were not built in it could be expressed like this:

```
def sum(array)
  if ($array)
    $array[0] + sum($array[1 : ])
  else
    0
```

This could also allow for reuse of logic between templates. The
`amplitude-*.jstl2` templates in routing are all very similar.  Some
of the similarities could be implemented as functions, like this:

```
def origin_props(event)
  if ( $event.origin ) {
    "origin_id": $event.origin."@id",
    "origin_url": $event.origin.url,
    "origin_name": $event.origin.name,
    "origin_category": $event.origin.category
  } else
    {}
```

There could be a whole module of functions like this that templates
could then use if they wanted it, or implement their own if they did
not.

The set of functions could then be imported with:

```
import "amplitude-functions.jstl2" as am
```

to be called as `am:origin_props(.)` and so on. One could also make
the template itself callable as: `am( .foo )`. That would allow us to
implement the state-preserving transform in pure JSTL.

There is the spectre of cyclic imports, but that can be handled fairly
easily.

What if one redeclares a function already available, either built in,
or supplied from the outside? Probably this should be allowed, with
the declaration inside the template taking precedence.

The only real cost is that transforms will no longer be guaranteed to
eventually finish computing.