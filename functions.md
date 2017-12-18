
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

### _random() -> float_

Returns a random number between `0.0` and `1.0`.
