
# JSLT example transformations

A list of example transformations taken from other sites around the
web.

## Collapsing repeated objects

Input:

```
{
  "menu": {
    "popup": {
      "menuitem": [
       {
          "value": "Open",
          "onclick": "OpenDoc()"
        },
        {
          "value": "Close",
          "onclick": "CloseDoc()"
        }
      ]
    }
  }
}
```

The desired output is:

```
{
  "result" : {
    "Open" : "OpenDoc()",
    "Close" : "CloseDoc()"
  }
}
```

This can be done two different ways, depending on whether one wants to
hard-wire the output, or make it dynamic.

Hard-wired:

```
{
  "result" : {
    "Open" : .menu.popup.menuitem[0].onclick,
    "Close" : .menu.popup.menuitem[1].onclick
  }
}
```

Or, we can turn each `value/onclick` object into a key/value pair in
the output object dynamically, like this:

```
{
  "result" : {for (.menu.popup.menuitem)
    .value : .onclick
  }
}
```

Taken from [JUST](https://www.codeproject.com/Articles/1187172/JUST-JSON-Under-Simple-Transformation).


## Pick one of two values, or complain

Input:

```
[
    {
      "a": "123",
      "b": ""
    },
    {
      "a": "",
      "b": "456"
    },
    {
      "a": "789",
      "b": "789"
    },
    {
      "a": "10",
      "b": "11"
    }
]
```

Output:

```
[
    {
      "x": "123"
    },
    {
      "x": "456"
    },
    {
      "x": "789"
    },
    {
      "x": "Error!"
    }
]
```

```

[for (.) {
  "x" : if (.a and .b) (
    if (.a == .b)
      .a
    else
      "Error!" // or you can fail with the error function
  ) else if (.a)
    .a
  else
    .b
}]
```

Taken from [JOLT](https://github.com/bazaarvoice/jolt/issues/626).

## N-queens

If you want something more hardcore, look at [solving the N-queens
problem in JSLT](queens.jslt)
