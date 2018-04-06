
# JSTL 2.0 functions

This page documents all the functions that are built in to JSTL 2.0.

<!-- GENERAL ===============================================================-->

## General functions

### _contains(element, sequence) -> boolean_

True if _element_ is contained in _sequence_, false otherwise.
_sequence_ can be an array, a string, or an object.

If _sequence_ is an array, _element_ must be an item in the array.

If _sequence_ is a string, _element_ is converted to a string, and
must be a substring of _sequence_. If _element_ is `null` the result
is `false`.

If _sequence_ is an object, _element_ is converted to a string, and
must be a key in the object.

### _size(sequence) -> integer_

Returns the number of elements in the sequence, which can be an array,
an object, or a string. If _sequence_ is _null_ it returns _null_.

### _error(message)_

Halts the transform with an error. The message is the message given to
the user.


<!-- NUMERIC ===============================================================-->

## Numeric functions

### _is-number(object) -> boolean_

True iff the argument is a number.

### _number(object, fallback?) -> integer|float_

Converts the argument into a number, if possible. Decimals and floats
will be returned untouched. Strings are parsed into numbers. `null`
returns `null`. All other types cause an error, unless `fallback` is
specified.

If `fallback` is specified then if `object` is of the wrong type, or
if it is a string that cannot be parsed, then the `fallback` value is
returned.

### _round(float) -> integer_

Rounds its argument to the nearest integer. Integers and `null` are
returned untouched. All other types cause an error.

### _floor(float) -> integer_

Rounds its argument to the nearest integer equal to or less than
_float_.  Integers and `null` are returned untouched. All other types
cause an error.

### _ceiling(float) -> integer_

Rounds its argument to the nearest integer equal to or greater than
_float_.  Integers and `null` are returned untouched. All other types
cause an error.

### _random() -> float_

Returns a random number between `0.0` and `1.0`.

<!-- STRING =================================================================-->

## String functions

### _is-string(object) -> boolean_

True iff the argument is a string.

### _string(object) -> string_

Converts _object_ into a string representation of the object.
Numbers, null, and boolean become the JSON representation of the
object inside a string.

### _test(input, regexp) -> boolean_

True iff _input_ matches the _regexp_. It's sufficient for the regexp
to match part of the string, unless the anchors  `^` and `$` are used.
If _input_ is null the function returns `false`.

### _capture(input, regexp) -> object_

If _input_ matches the _regexp_ it returns an object where there is a
key for every named group in the regexp. If _input_ is null the
function returns `null`. If the regexp doesn't match an empty object
is returned.

### _split(input, regexp) -> array_

Splits the _input_ string with the _regexp_ and returns an array of
strings. If _input_ is `null` the function returns `null`.

### _join(array, separator) -> string_

Returns a string produced by concatenating the elements of the array
(converted to strings using the `string` function) with _separator_
between each element. If _array_ is `null` the function returns `null`.

### _lowercase(string) -> string_

Converts the input string to lowercase. Note that this is a naive
function that only handles ASCII characters.

### _uppercase(string) -> string_

Converts the input string to uppercase. Note that this is a naive
function that only handles ASCII characters.

### _starts-with(tested, prefix) -> boolean_

True iff the `tested` string starts with `prefix`.

### _ends-with(tested, suffix) -> boolean_

True iff the `tested` string ends with `suffix`.

### _from-json(string, fallback?) -> value_

Parses the string as JSON and returns the result. So parsing `"22"`
will return `22`. If the string is `null` then the function will
return `null`.

If the optional `fallback` argument is not specified JSON parse errors
will cause an error. If it is specified that value will be returned if
the JSON cannot be parsed. So `from-json("[1,2", "BAD")` will return
`"BAD"`.

### _to-json(value) -> string_

The opposite of `from-json`, in that it takes any JSON value and
returns it serialized as a string.

<!-- BOOLEAN ================================================================-->

## Boolean functions

### _boolean(value) -> boolean_

Converts the input value to a boolean. Everything is considered to be
`true`, except `null`, `[]`, `{}`, `""`, `false`, and `0`.

### _not(boolean) -> boolean_

Returns the opposite boolean value from the parameter. The input is
quietly converted to boolean, so `not(null)` will return `true`.

### _is-boolean(value) -> boolean_

True iff `value` is a boolean.

<!-- OBJECT =================================================================-->

## Object functions

### _is-object(value) -> boolean_

True iff `value` is an object.

### _get-key(object, key) -> value_

Does the same as `.key` on `object`, with the difference that here the
key can be dynamic. That is, it can come from a variable, be looked up
in input data, and so on. If the key does not exist, `null` is returned.


<!-- ARRAY ==================================================================-->

## Array functions

### _array(value) -> array_

Converts the input value to an array. Numbers, booleans, and strings
can't be converted to an array, so these cause errors.

Objects are converted to arrays of the form:

```
[
  {"key" : first key, "value" : first value},
  {"key" : second key, "value" : second value},
  {"key" : third key, "value" : third value},
  ...
]
```

### _is-array(value) -> boolean_

True iff `value` is an array.


<!-- TIME ===================================================================-->

## Time functions

### _now() -> double_

Returns the number of seconds since midnight, January 1, 1970 UTC in
the UTC timezone. Milliseconds are returned as decimals of the number.

### _parse-time(time, format, fallback?) -> double_

Parses `time` with `format` (specified in Java date/time format) and
returns the number of seconds since the epoch in the UTC timezone. If
no timezone is specified in the `time` string, the timezone is assumed
to be UTC.

If `fallback` is not specified, the function will cause an error if
`time` is of the wrong type or does not match the format. If
`fallback` is specified that value will be returned instead.

### _format-time(timestamp, format, timezone?) -> string_

Formats `timestamp` (the number of seconds since epoch) with `format`
and returns the formatted string. The timezone is assumed to be UTC,
but this can be overridden with the `timezone` argument.