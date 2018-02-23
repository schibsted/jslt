
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

<!-- NUMERIC ===============================================================-->

## Numeric functions

### _is-number(object) -> boolean_

True iff the argument is a number.

### _number(object) -> integer|float_

Converts the argument into a number, if possible. Decimals and floats
will be returned untouched. Strings are parsed into numbers. `null`
returns `null`. All other types cause an error.

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
