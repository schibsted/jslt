
# JSLT functions

This page documents all the functions that are built in to JSLT.

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

Examples:

```
contains(null, [1, 2, 3])      => false
contains(1, [1, 2, 3])         => true
contains(0, [1, 2, 3])         => false
contains("no", {"no" : false}) => true
contains(1, {"1" : false})     => true
contains("ab", "abc")          => true
```

<details>
<summary><strong>Study Cases Contains</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "numbers": [1, 2, 3],
  "data": {
    "no": false,
    "1": false
  },
  "text": "abc"
}
```
<b>JSON FUNCTION</b>

```json
{
  "array_contains": "contains(1, .numbers)",
  "array_not_contains": "contains(0, .numbers)",
  "null_contains": "contains(null, .numbers)",
  "object_contains": "contains(\"no\", .data)",
  "object_numeric_key": "contains(1, .data)",
  "string_contains": "contains(\"ab\", .text)",
  "string_not_contains": "contains(\"xy\", .text)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "array_contains": true,
  "array_not_contains": false,
  "null_contains": false,
  "object_contains": true,
  "object_numeric_key": true,
  "string_contains": true,
  "string_not_contains": false
}
```
</details>
</br>

### _size(sequence) -> integer_

Returns the number of elements in the sequence, which can be an array,
an object, or a string. If _sequence_ is _null_ it returns _null_.

Examples:

```
size([1, 2, 3]) => 3
size({"1" : 3}) => 1
size("abcdef")  => 6
size(null)      => null
```

<details>
<summary><strong>Study Cases Size</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "numbers": [1, 2, 3],
  "data": {
    "1": 3
  },
  "text": "abcdef",
  "empty": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "array_size": "size(.numbers)",
  "object_size": "size(.data)",
  "string_size": "size(.text)",
  "null_size": "size(.empty)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "array_size": 3,
  "object_size": 1,
  "string_size": 6,
  "null_size": null
}
```
</details>
</br>

### _error(message)_

Halts the transform with an error. The message is the message given to
the user.

Examples:

```
if (not(is-array(.things)))
  error("'things' is not an array")
```

<details>
<summary><strong>Study Cases Error</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "things": "hello"
}
```
<b>JSON FUNCTION</b>

```json
{
  "function": "if (not(is-array(.things))) error(\"'things' is not an array\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "error": "'things' is not an array"
}
```
</details>
</br>

### _fallback(arg1, arg2, ...) -> value_

Returns the first argument that has a value. That is, the first
argument that is not `null`, `[]`, or `{}`.

Examples:

```
fallback(.not_existing_key, .another_not_existing, 1)  => 1
fallback(null, [], {}, "value")                        => "value"
```

<details>
<summary><strong>Study Cases Fallback</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "not_existing_key": null,
  "another_not_existing": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "missing_key_fallback": "fallback(.not_existing_key, .another_not_existing, 1)",
  "multiple_fallback": "fallback(null, [], {}, \"value\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "missing_key_fallback": 1,
  "multiple_fallback": "value"
}
```
</details>
</br>

### _min(arg1, arg2) -> value_

Returns the argument that compares as the smallest. If one argument is
`null` the result is `null`.

Examples:

```
min(10, 1)    -> 1
min("a", "b") -> "a"
min(10, null) -> null
```
<details>
<summary><strong>Study Cases Min</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "number_a": 10,
  "number_b": 1,
  "string_a": "a",
  "string_b": "b",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "number_min": "min(.number_a, .number_b)",
  "string_min": "min(.string_a, .string_b)",
  "null_min": "min(.number_a, .null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "number_min": 1,
  "string_min": "a",
  "null_min": null
}
```
</details>
</br>

### _max(arg1, arg2) -> value_

Returns the argument that compares as the largest. If one argument is
`null` the result is `null`.

Examples:

```
max(10, 1)    -> 10
max("a", "b") -> "b"
max(10, null) -> 10
```

<details>
<summary><strong>Study Cases Max</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "number_a": 10,
  "number_b": 1,
  "string_a": "a",
  "string_b": "b",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "number_max": "max(.number_a, .number_b)",
  "string_max": "max(.string_a, .string_b)",
  "null_max": "max(.number_a, .null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "number_max": 10,
  "string_max": "b",
  "null_max": 10
}
```
</details>
</br>

<!-- NUMERIC ===============================================================-->

## Numeric functions

### _is-number(object) -> boolean_

True iff the argument is a number.

Examples:

```
is-number(null) => false
is-number(1)    => true
is-number(1.0)  => true
is-number("1")  => false
```
<details>
<summary><strong>Study Cases Is Number Object</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "integer_value": 1,
  "decimal_value": 1.0,
  "string_value": "1"
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_check": "is-number(.null_value)",
  "integer_check": "is-number(.integer_value)",
  "decimal_check": "is-number(.decimal_value)",
  "string_check": "is-number(.string_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_check": false,
  "integer_check": true,
  "decimal_check": true,
  "string_check": false
}
```
</details>
</br>

### _is-integer(object) -> boolean_

True iff the argument is an integral number.

Examples:

```
is-integer(null) => false
is-integer(1)    => true
is-integer(1.0)  => false
is-integer("1")  => false
```
<details>
<summary><strong>Study Cases Is Integer Object</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "integer_value": 1,
  "decimal_value": 1.0,
  "string_value": "1"
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_check": "is-integer(.null_value)",
  "integer_check": "is-integer(.integer_value)",
  "decimal_check": "is-integer(.decimal_value)",
  "string_check": "is-integer(.string_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_check": false,
  "integer_check": true,
  "decimal_check": false,
  "string_check": false
}
```
</details>
</br>

### _is-decimal(object) -> boolean_

True iff the argument is a floating-point number. In this regard
`1.0` is considered floating-point number and `1` is not.

Examples:

```
is-decimal(null) => false
is-decimal(1)    => false
is-decimal(1.0)  => true
is-decimal("1.0")  => false
```

<details>
<summary><strong>Study Cases Is Decimal Object</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "integer_value": 1,
  "decimal_value": 1.0,
  "string_value": "1.0"
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_check": "is-decimal(.null_value)",
  "integer_check": "is-decimal(.integer_value)",
  "decimal_check": "is-decimal(.decimal_value)",
  "string_check": "is-decimal(.string_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_check": false,
  "integer_check": false,
  "decimal_check": true,
  "string_check": false
}
```
</details>
</br>

### _number(object, fallback?) -> integer|float_

Converts the argument into a number, if possible. Decimals and
integers will be returned untouched. Strings are parsed into numbers.
`null` returns `null`. All other types cause an error, unless
`fallback` is specified.

If `fallback` is specified then if `object` is of the wrong type, or
if it is a string that cannot be parsed, then the `fallback` value is
returned.

The number format supported is the same as in JSON literals, except
that leading zeroes are allowed, and it's allowed to omit the zero
before the period.

Examples:

```
number(23)      => 23
number("23")    => 23
number("023")   => 23
number(23.0)    => 23.0
number(".23")   =>  0.23
number("-.23")  => -0.23
number(null)    => null
number("ab")    => error
number("ab", 0) => 0
```
<details>
<summary><strong>Study Cases Number Object Fallback</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "integer_value": 23,
  "string_value": "23",
  "leading_zero": "023",
  "decimal_value": 23.0,
  "decimal_string": ".23",
  "negative_decimal_string": "-.23",
  "null_value": null,
  "invalid_string": "ab"
}
```
<b>JSON FUNCTION</b>

```json
{
  "integer": "number(.integer_value)",
  "string": "number(.string_value)",
  "leading_zero": "number(.leading_zero)",
  "decimal": "number(.decimal_value)",
  "decimal_string": "number(.decimal_string)",
  "negative_decimal_string": "number(.negative_decimal_string)",
  "null": "number(.null_value)",
  "invalid": "number(.invalid_string, 0)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "integer": 23,
  "string": 23,
  "leading_zero": 23,
  "decimal": 23.0,
  "decimal_string": 0.23,
  "negative_decimal_string": -0.23,
  "null": null,
  "invalid": 0
}
```
</details>
</br>

### _round(float) -> integer_

Rounds its argument to the nearest integer. Integers and `null` are
returned untouched. All other types cause an error.

Examples:

```
round(1)    => 1
round(1.0)  => 1
round(1.51) => 2
round(null) => null
```

<details>
<summary><strong>Study Cases Round</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "integer_value": 1,
  "decimal_value": 1.0,
  "rounded_up": 1.51,
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "integer": "round(.integer_value)",
  "decimal": "round(.decimal_value)",
  "rounded_up": "round(.rounded_up)",
  "null": "round(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "integer": 1,
  "decimal": 1,
  "rounded_up": 2,
  "null": null
}
```
</details>
</br>

### _floor(float) -> integer_

Rounds its argument to the nearest integer equal to or less than
_float_.  Integers and `null` are returned untouched. All other types
cause an error.

Examples:

```
floor(1)    => 1
floor(1.0)  => 1
floor(1.51) => 1
floor(null) => null
```

<details>
<summary><strong>Study Cases Floor</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "integer_value": 1,
  "decimal_value": 1.0,
  "rounded_down": 1.51,
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "integer": "floor(.integer_value)",
  "decimal": "floor(.decimal_value)",
  "rounded_down": "floor(.rounded_down)",
  "null": "floor(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "integer": 1,
  "decimal": 1,
  "rounded_down": 1,
  "null": null
}
```
</details>
</br>

### _ceiling(float) -> integer_

Rounds its argument to the nearest integer equal to or greater than
_float_.  Integers and `null` are returned untouched. All other types
cause an error.

Examples:

```
ceiling(1)    => 1
ceiling(1.0)  => 1
ceiling(1.51) => 2
ceiling(null) => null
```

<details>
<summary><strong>Study Cases Ceiling</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "integer_value": 1,
  "decimal_value": 1.0,
  "rounded_up": 1.51,
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "integer": "ceiling(.integer_value)",
  "decimal": "ceiling(.decimal_value)",
  "rounded_up": "ceiling(.rounded_up)",
  "null": "ceiling(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "integer": 1,
  "decimal": 1,
  "rounded_up": 2,
  "null": null
}
```
</details>
</br>

### _random() -> float_

Returns a random number between `0.0` and `1.0`.

Examples:

```
random() => 0.24712712424
```

<details>
<summary><strong>Study Cases Random</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{}
```
<b>JSON FUNCTION</b>

```json
{
  "random_value": "random()",
  "is_number": "is-number(random())",
  "in_range": "random() >= 0.0 and random() < 1.0"
}
```
<b>JSON OUTPUT</b>

```json
{
  "random_value": 0.24712712424,
  "is_number": true,
  "in_range": true
}
```
</details>
</br>

### _sum(array) -> number_

Returns the sum of all the numbers in the array. The parameter must be
an array, and all values in it must be numbers.

Examples:

```
sum([1,2,3])    => 6
sum([1])        => 1
sum([1.0, 2.0]) => 3.0
sum([])         => 0
sum(null)       => null
```

<details>
<summary><strong>Study Cases Sum</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "numbers": [1, 2, 3],
  "single": [1],
  "decimals": [1.0, 2.0],
  "empty": [],
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "numbers_sum": "sum(.numbers)",
  "single_sum": "sum(.single)",
  "decimals_sum": "sum(.decimals)",
  "empty_sum": "sum(.empty)",
  "null_sum": "sum(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "numbers_sum": 6,
  "single_sum": 1,
  "decimals_sum": 3.0,
  "empty_sum": 0,
  "null_sum": null
}
```
</details>
</br>

### _mod(a,d) -> integer_

Returns `a` modulo `d`. This function is the same as the familiar `%`
operator in most programming languages, except that it behaves
differently for negative numbers. The result is always in the range
`0..abs(d)`.

Mathematically, the function is defined as:

```
a = d * floor(a / d) + mod(a, d)
```

Note, however, that the division operator in question here is
Euclidean division. An explanation is given in [Division and Modulus
for Computer Scientists, Daan Leijen,
2001](https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/divmodnote-letter.pdf). For
more background, see [the original pull
request](https://github.com/schibsted/jslt/pull/43).

Examples:

```
mod(10, 2)    => 0
mod(10, 3)    => 1
mod(10, 4)    => 2
mod(-10, 3)   => 2
mod(-10, -3)  => 2
mod(10, -3)   => 1
mod(null, 2)  => null
mod(10, null) => null
mod(10.5, 2)  => error
mod(10, 2.1)  => error
mod(10, "2")  => error
```

<details>
<summary><strong>Study Cases Mod</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "a": 10,
  "negative_a": -10,
  "d2": 2,
  "d3": 3,
  "d4": 4,
  "negative_d": -3,
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "mod_10_2": "mod(.a, .d2)",
  "mod_10_3": "mod(.a, .d3)",
  "mod_10_4": "mod(.a, .d4)",
  "mod_neg10_3": "mod(.negative_a, .d3)",
  "mod_neg10_neg3": "mod(.negative_a, .negative_d)",
  "mod_10_neg3": "mod(.a, .negative_d)",
  "mod_null_2": "mod(.null_value, .d2)",
  "mod_10_null": "mod(.a, .null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "mod_10_2": 0,
  "mod_10_3": 1,
  "mod_10_4": 2,
  "mod_neg10_3": 2,
  "mod_neg10_neg3": 2,
  "mod_10_neg3": 1,
  "mod_null_2": null,
  "mod_10_null": null
}
```
</details>
</br>

### _hash-int(object) -> int_

Returns a hash value of the given object. It differs from `sha256-hex` in that
it gives a numeric integral hash value instead of a string. There is no guarantee that the same
input will produce the same output in different environments (JVM versions, etc.)

Examples:

```
hash-int("test") => 3556808
hash-int("") => 310
hash-int({}) => 8
hash-int([]) => 1
hash-int([1,2]) => 8928
hash-int([2,1]) => 9858
hash-int([1,2]) != hash-int([2,1]) => true
hash-int(1) => 248
hash-int(null) => 6
hash-int({"a":1,"b":2}) => 10519540
hash-int({"b":2,"a":1}) => 10519540
hash-int({"a":1,"b":2}) == hash-int({"b":2,"a":1}) => true
```

<details>
<summary><strong>Study Cases Hash Int</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "text": "test",
  "empty_string": "",
  "empty_object": {},
  "empty_array": [],
  "array_a": [1, 2],
  "array_b": [2, 1],
  "number_value": 1,
  "null_value": null,
  "object_a": {
    "a": 1,
    "b": 2
  },
  "object_b": {
    "b": 2,
    "a": 1
  }
}
```
<b>JSON FUNCTION</b>

```json
{
  "text_hash": "hash-int(.text)",
  "empty_string_hash": "hash-int(.empty_string)",
  "empty_object_hash": "hash-int(.empty_object)",
  "empty_array_hash": "hash-int(.empty_array)",
  "array_a_hash": "hash-int(.array_a)",
  "array_b_hash": "hash-int(.array_b)",
  "arrays_differ": "hash-int(.array_a) != hash-int(.array_b)",
  "number_hash": "hash-int(.number_value)",
  "null_hash": "hash-int(.null_value)",
  "object_a_hash": "hash-int(.object_a)",
  "object_b_hash": "hash-int(.object_b)",
  "objects_equal": "hash-int(.object_a) == hash-int(.object_b)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "text_hash": 3556808,
  "empty_string_hash": 310,
  "empty_object_hash": 8,
  "empty_array_hash": 1,
  "array_a_hash": 8928,
  "array_b_hash": 9858,
  "arrays_differ": true,
  "number_hash": 248,
  "null_hash": 6,
  "object_a_hash": 10519540,
  "object_b_hash": 10519540,
  "objects_equal": true
}
```
</details>
</br>

<!-- STRING =================================================================-->

## String functions

### _is-string(object) -> boolean_

True iff the argument is a string.

Examples:

```
is-string(null)  => false
is-string("123") => true
is-string(123)   => false
```

<details>
<summary><strong>Study Cases Is String Object</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "string_value": "123",
  "number_value": 123
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_check": "is-string(.null_value)",
  "string_check": "is-string(.string_value)",
  "number_check": "is-string(.number_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_check": false,
  "string_check": true,
  "number_check": false
}
```
</details>
</br>

### _string(object) -> string_

Converts _object_ into a string representation of the object.
Numbers, null, and boolean become the JSON representation of the
object inside a string.

Examples:

```
string(null)  => "null"
string(123)   => "123"
string("123") => "123"
```

<details>
<summary><strong>Study Cases String Object</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "number_value": 123,
  "string_value": "123"
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_string": "string(.null_value)",
  "number_string": "string(.number_value)",
  "string_string": "string(.string_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_string": "null",
  "number_string": "123",
  "string_string": "123"
}
```
</details>
</br>

### _test(input, regexp) -> boolean_

True iff _input_ matches the _regexp_. It's sufficient for the regexp
to match part of the string, unless the anchors  `^` and `$` are used.
If _input_ is null the function returns `false`.

Some examples:

```
test("123", "\d+")       => Error (\d not a known escape code)
test("123", "\\d+")      => true
test("abc123", "\\d+")   => true (matching part is enough)
test("abc123", "^\\d+$") => false
```

<details>
<summary><strong>Study Cases Test</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "digits": "123",
  "mixed": "abc123"
}
```
<b>JSON FUNCTION</b>

```json
{
  "digits_match": "test(.digits, \"\\\\d+\")",
  "partial_match": "test(.mixed, \"\\\\d+\")",
  "full_match": "test(.mixed, \"^\\\\d+$\")",
  "null_match": "test(null, \"\\\\d+\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "digits_match": true,
  "partial_match": true,
  "full_match": false,
  "null_match": false
}
```
</details>
</br>

### _capture(input, regexp) -> object_

If _input_ matches the _regexp_ it returns an object where there is a
key for every named group in the regexp. If _input_ is null the
function returns `null`. If the regexp doesn't match an empty object
is returned.

Given the following input:

```
{"schema" : "http://schemas.schibsted.io/thing/pulse-simple.json#1.json"}
```

we can match it with:

```
capture(.schema, "http://(?<host>[^/]+)/(?<rest>.+)")
```

The two named groups will match different parts of the string, and so
the output will be one key for each named group:

```
{
  "host" : "schemas.schibsted.io",
  "rest" : "thing/pulse-simple.json#1.json"
}
```

<details>
<summary><strong>Study Cases Capture</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "schema": "http://schemas.schibsted.io/thing/pulse-simple.json#1.json",
  "unmatched": "not-a-url",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "captured": "capture(.schema, \"http://(?<host>[^/]+)/(?<rest>.+)\")",
  "no_match": "capture(.unmatched, \"http://(?<host>[^/]+)/(?<rest>.+)\")",
  "null_capture": "capture(.null_value, \"http://(?<host>[^/]+)/(?<rest>.+)\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "captured": {
    "host": "schemas.schibsted.io",
    "rest": "thing/pulse-simple.json#1.json"
  },
  "no_match": {},
  "null_capture": null
}
```
</details>
</br>

### _split(input, regexp) -> array_

Splits the _input_ string with the _regexp_ and returns an array of
strings. If _input_ is `null` the function returns `null`.

Examples:

```
split("1,2,3,4,5", ",") => ["1", "2", "3", "4", "5"]
split("1,2,3,4,5", ";") => ["1,2,3,4,5"]
split(null, ";")        => null
split(",2", ",")        => ["", "2"]
split("2,", ",")        => ["2"]
```

<details>
<summary><strong>Study Cases Split</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "csv": "1,2,3,4,5",
  "leading_comma": ",2",
  "trailing_comma": "2,",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "split_comma": "split(.csv, \",\")",
  "split_semicolon": "split(.csv, \";\")",
  "split_null": "split(.null_value, \";\")",
  "split_leading": "split(.leading_comma, \",\")",
  "split_trailing": "split(.trailing_comma, \",\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "split_comma": ["1", "2", "3", "4", "5"],
  "split_semicolon": ["1,2,3,4,5"],
  "split_null": null,
  "split_leading": ["", "2"],
  "split_trailing": ["2"]
}
```
</details>
</br>

### _join(array, separator) -> string_

Returns a string produced by concatenating the elements of the array
(converted to strings using the `string` function) with _separator_
between each element. If _array_ is `null` the function returns `null`.

Examples:

```
join(["a", "b", "c"], " ") => "a b c"
join(["a"], " ")            => "a"
join(null, "-")            => null
join([1], "-")             => "1"
```

<details>
<summary><strong>Study Cases Join</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "letters": ["a", "b", "c"],
  "single": ["a"],
  "number_array": [1],
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "join_space": "join(.letters, \" \")",
  "join_single": "join(.single, \" \")",
  "join_null": "join(.null_value, \"-\")",
  "join_number": "join(.number_array, \"-\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "join_space": "a b c",
  "join_single": "a",
  "join_null": null,
  "join_number": "1"
}
```
</details>
</br>

### _lowercase(string) -> string_

Converts the input string to lowercase. Note that this is a naive
function that does not handle all the special Unicode cases.

Examples:

```
lowercase("ABCÆØÅ") => "abcæøå"
lowercase(null)     => null
```

<details>
<summary><strong>Study Cases Lowercase</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "text": "ABCÆØÅ",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "lower": "lowercase(.text)",
  "null_lower": "lowercase(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "lower": "abcæøå",
  "null_lower": null
}
```
</details>
</br>

### _uppercase(string) -> string_

Converts the input string to uppercase. Note that this is a naive
function that only handles ASCII characters.

Examples:

```
uppercase("abcæøå") => "ABCÆØÅ"
uppercase(null)     => null
```

<details>
<summary><strong>Study Cases Uppercase</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "text": "abcæøå",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "upper": "uppercase(.text)",
  "null_upper": "uppercase(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "upper": "ABCÆØÅ",
  "null_upper": null
}
```
</details>
</br>

### _sha256-hex(string) -> string_

Generates a string with the hexadecimal representation of the SHA256 hash of the input string.

Examples:

```
sha256-hex("foo") => "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"
sha256-hex("42")  => "73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049"
sha256-hex(42)    => "73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049"
sha256-hex(null)  => null
```

<details>
<summary><strong>Study Cases Sha256 Hex</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "text": "foo",
  "string_number": "42",
  "number_value": 42,
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "text_hash": "sha256-hex(.text)",
  "string_number_hash": "sha256-hex(.string_number)",
  "number_hash": "sha256-hex(.number_value)",
  "null_hash": "sha256-hex(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "text_hash": "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae",
  "string_number_hash": "73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049",
  "number_hash": "73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049",
  "null_hash": null
}
```
</details>
</br>

### _starts-with(tested, prefix) -> boolean_

True iff the `tested` string starts with `prefix`.

Examples:

```
starts-with("prohibition", "pro") => true
starts-with("prohibition", "pre") => false
starts-with(null, "pre")          => false
```

<details>
<summary><strong>Study Cases Starts With</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "text": "prohibition",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "starts_pro": "starts-with(.text, \"pro\")",
  "starts_pre": "starts-with(.text, \"pre\")",
  "starts_null": "starts-with(.null_value, \"pre\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "starts_pro": true,
  "starts_pre": false,
  "starts_null": false
}
```
</details>
</br>

### _ends-with(tested, suffix) -> boolean_

True iff the `tested` string ends with `suffix`.

Examples:

```
ends-with("prohibition", "pro") => false
ends-with("prohibition", "ion") => true
ends-with(null, "ion")          => false
```

<details>
<summary><strong>Study Cases Ends With</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "text": "prohibition",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "ends_pro": "ends-with(.text, \"pro\")",
  "ends_ion": "ends-with(.text, \"ion\")",
  "ends_null": "ends-with(.null_value, \"ion\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "ends_pro": false,
  "ends_ion": true,
  "ends_null": false
}
```
</details>
</br>

### _from-json(string, fallback?) -> value_

Parses the string as JSON and returns the result. So parsing `"22"`
will return `22`. If the string is `null` then the function will
return `null`.

If the optional `fallback` argument is not specified JSON parse errors
will cause an error. If it is specified that value will be returned if
the JSON cannot be parsed.

Examples:

```
from-json("[1,2]")       => [1, 2]
from-json("[1,2", "BAD") => "BAD"
from-json("[1,2")        => error
from-json(null)          => null
```

<details>
<summary><strong>Study Cases From Json</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "valid_json": "[1,2]",
  "invalid_json": "[1,2",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "parsed": "from-json(.valid_json)",
  "fallback": "from-json(.invalid_json, \"BAD\")",
  "null_parsed": "from-json(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "parsed": [1, 2],
  "fallback": "BAD",
  "null_parsed": null
}
```
</details>
</br>

### _to-json(value) -> string_

The opposite of `from-json`, in that it takes any JSON value and
returns it serialized as a string.

Examples:

```
to-json([1,2])       => "[1, 2]"
to-json(1)           => "1"
to-json("foo")       => "\"foo\""
to-json(null)        => "null"
```

<details>
<summary><strong>Study Cases To Json</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "array_value": [1, 2],
  "number_value": 1,
  "string_value": "foo",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "array_json": "to-json(.array_value)",
  "number_json": "to-json(.number_value)",
  "string_json": "to-json(.string_value)",
  "null_json": "to-json(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "array_json": "[1, 2]",
  "number_json": "1",
  "string_json": "\"foo\"",
  "null_json": "null"
}
```
</details>
</br>

### _replace(value, regexp, out) -> string_

Replaces every substring that in `value` that matches `regexp` with
`out`.  If `value` is not a string, it's converted to a string, except
if it is `null`. `regexp` and `out` must be strings.

It is an error for `regexp` ever to match an empty string.

Examples:

```
replace("abc def ghi", " ", "-")      => "abc-def-ghi"
replace("abc def ghi", "\\s+", "-")   => "abc-def-ghi"
replace(null, "\\s+", "-")            => null
replace("   whoah", "^\\s+", "")      => "whoah"
replace("abc def ghi", "[a-z]", "x")  => "xxx xxx xxx"
replace("abc def ghi", "[a-z]+", "x") => "x x x"
```

<details>
<summary><strong>Study Cases Replace</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "text": "abc def ghi",
  "padded": "   whoah",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "replace_space": "replace(.text, \" \", \"-\")",
  "replace_whitespace": "replace(.text, \"\\\\s+\", \"-\")",
  "replace_null": "replace(.null_value, \"\\\\s+\", \"-\")",
  "trim_leading": "replace(.padded, \"^\\\\s+\", \"\")",
  "replace_chars": "replace(.text, \"[a-z]\", \"x\")",
  "replace_words": "replace(.text, \"[a-z]+\", \"x\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "replace_space": "abc-def-ghi",
  "replace_whitespace": "abc-def-ghi",
  "replace_null": null,
  "trim_leading": "whoah",
  "replace_chars": "xxx xxx xxx",
  "replace_words": "x x x"
}
```
</details>
</br>

### _trim(string) -> string_

Removes leading and trailing whitespace in the input string. If the
input is `null`, so is the output. Other non-string input values are
converted to string.

Examples:

```
trim("  abc  ")    => "abc"
trim("abc")        => "abc"
trim("abc \t\r\n") => "abc"
trim(false)        => "false"
trim(null)         => null
```

<details>
<summary><strong>Study Cases Trim</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "padded": "  abc  ",
  "plain": "abc",
  "trailing_ws": "abc \t\r\n",
  "boolean_value": false,
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "trim_padded": "trim(.padded)",
  "trim_plain": "trim(.plain)",
  "trim_trailing": "trim(.trailing_ws)",
  "trim_boolean": "trim(.boolean_value)",
  "trim_null": "trim(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "trim_padded": "abc",
  "trim_plain": "abc",
  "trim_trailing": "abc",
  "trim_boolean": "false",
  "trim_null": null
}
```
</details>
</br>

### _uuid(long, long) -> string_

Generates a formatted UUID string with dashes.
If no parameter are passed, a random UUID version 4 will be generated.
If two parameter are passed, a UUID version 1 is generated based on two long values where the first parameter represents the MSB (most significant bytes) and the second parameter the LSB (least significant bytes) of the UUID.
If both parameter are null a NIL UUID (00000000-0000-0000-0000-000000000000 is generated)

Examples:

```
uuid()                       => "b02c39c0-6f8f-4250-97cd-78500af36e27"
uuid(1234567890, 1234567890) => "00000000-4996-102d-8000-0000499602d2"
uuid(0, 0)                   => "00000000-0000-1000-8000-000000000000"
uuid(null, null)             => "00000000-0000-0000-0000-000000000000"
```

<details>
<summary><strong>Study Cases Uuid</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "msb": 1234567890,
  "lsb": 1234567890,
  "zero": 0,
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "random_uuid": "uuid()",
  "version1_uuid": "uuid(.msb, .lsb)",
  "zero_uuid": "uuid(.zero, .zero)",
  "nil_uuid": "uuid(.null_value, .null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "random_uuid": "b02c39c0-6f8f-4250-97cd-78500af36e27",
  "version1_uuid": "00000000-4996-102d-8000-0000499602d2",
  "zero_uuid": "00000000-0000-1000-8000-000000000000",
  "nil_uuid": "00000000-0000-0000-0000-000000000000"
}
```
</details>
</br>

<!-- BOOLEAN ================================================================-->

## Boolean functions

### _boolean(value) -> boolean_

Converts the input value to a boolean. Everything is considered to be
`true`, except `null`, `[]`, `{}`, `""`, `false`, and `0`.

Examples:

```
boolean(null)  => false
boolean("")    => false
boolean(" ")   => true
boolean(0)     => false
boolean(1)     => true
boolean(true)  => true
boolean(false) => false
boolean([])    => false
boolean([1])   => true
```

<details>
<summary><strong>Study Cases Boolean</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "empty_string": "",
  "space_string": " ",
  "zero": 0,
  "one": 1,
  "true_value": true,
  "false_value": false,
  "empty_array": [],
  "array_value": [1]
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_bool": "boolean(.null_value)",
  "empty_string_bool": "boolean(.empty_string)",
  "space_string_bool": "boolean(.space_string)",
  "zero_bool": "boolean(.zero)",
  "one_bool": "boolean(.one)",
  "true_bool": "boolean(.true_value)",
  "false_bool": "boolean(.false_value)",
  "empty_array_bool": "boolean(.empty_array)",
  "array_bool": "boolean(.array_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_bool": false,
  "empty_string_bool": false,
  "space_string_bool": true,
  "zero_bool": false,
  "one_bool": true,
  "true_bool": true,
  "false_bool": false,
  "empty_array_bool": false,
  "array_bool": true
}
```
</details>
</br>

### _not(boolean) -> boolean_

Returns the opposite boolean value from the parameter. The input is
quietly converted to boolean, so `not(null)` will return `true`.

Examples:

```
not(null)  => true
not("")    => true
not(" ")   => false
not(0)     => true
not(1)     => false
not(true)  => false
not(false) => true
not([])    => true
not([1])   => false
```

<details>
<summary><strong>Study Cases Not</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "empty_string": "",
  "space_string": " ",
  "zero": 0,
  "one": 1,
  "true_value": true,
  "false_value": false,
  "empty_array": [],
  "array_value": [1]
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_not": "not(.null_value)",
  "empty_string_not": "not(.empty_string)",
  "space_string_not": "not(.space_string)",
  "zero_not": "not(.zero)",
  "one_not": "not(.one)",
  "true_not": "not(.true_value)",
  "false_not": "not(.false_value)",
  "empty_array_not": "not(.empty_array)",
  "array_not": "not(.array_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_not": true,
  "empty_string_not": true,
  "space_string_not": false,
  "zero_not": true,
  "one_not": false,
  "true_not": false,
  "false_not": true,
  "empty_array_not": true,
  "array_not": false
}
```
</details>
</br>

### _is-boolean(value) -> boolean_

True iff `value` is a boolean.

Examples:

```
is-boolean(null)  => false
is-boolean(true)  => true
is-boolean(false) => true
is-boolean("")    => false
is-boolean(" ")   => false
```

<details>
<summary><strong>Study Cases Is Boolean</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "true_value": true,
  "false_value": false,
  "empty_string": "",
  "space_string": " "
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_check": "is-boolean(.null_value)",
  "true_check": "is-boolean(.true_value)",
  "false_check": "is-boolean(.false_value)",
  "empty_string_check": "is-boolean(.empty_string)",
  "space_string_check": "is-boolean(.space_string)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_check": false,
  "true_check": true,
  "false_check": true,
  "empty_string_check": false,
  "space_string_check": false
}
```
</details>
</br>

<!-- OBJECT =================================================================-->

## Object functions

### _is-object(value) -> boolean_

True iff `value` is an object.

Examples:

```
is-object(null)  => false
is-object({})    => true
is-object([])    => false
is-object("")    => false
```

<details>
<summary><strong>Study Cases Is Object</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "empty_object": {},
  "empty_array": [],
  "empty_string": ""
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_check": "is-object(.null_value)",
  "object_check": "is-object(.empty_object)",
  "array_check": "is-object(.empty_array)",
  "string_check": "is-object(.empty_string)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_check": false,
  "object_check": true,
  "array_check": false,
  "string_check": false
}
```
</details>
</br>

### _get-key(object, key, fallback?) -> value_

Does the same as `.key` on `object`, with the difference that here the
key can be dynamic. That is, it can come from a variable, be looked up
in input data, and so on.

If the key does not exist, `null` is returned if the `fallback`
argument is not given. If `fallback` is specified the fallback value
will be returned if the key does not exist.

Example:

```
let lookup = {
  "no" : "Norway,
  "se" : "Sweden"
}

get-key($lookup, "no")
```

This will return `"Norway"`. If we use the fallback:

```
let lookup = {
  "no" : "Norway,
  "se" : "Sweden"
}

get-key($lookup, "dk", "<unknown>")
```

it will return `"<unknown>"`.

<details>
<summary><strong>Study Cases Get Key</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "lookup": {
    "no": "Norway",
    "se": "Sweden"
  },
  "existing_key": "no",
  "missing_key": "dk"
}
```
<b>JSON FUNCTION</b>

```json
{
  "found": "get-key(.lookup, .existing_key)",
  "missing": "get-key(.lookup, .missing_key)",
  "missing_with_fallback": "get-key(.lookup, .missing_key, \"<unknown>\")"
}
```
<b>JSON OUTPUT</b>

```json
{
  "found": "Norway",
  "missing": null,
  "missing_with_fallback": "<unknown>"
}
```
</details>
</br>


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

Examples:

```
array(null)   => null
array([1, 2]) => [1, 2]
array("123")  => error

array({"a": 1, "b": 2}) =>
  [
    {"key" : "a", "value" : 1},
    {"key" : "b", "value" : 2}
  ]
```

<details>
<summary><strong>Study Cases Array</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "array_value": [1, 2],
  "object_value": {
    "a": 1,
    "b": 2
  }
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_array": "array(.null_value)",
  "same_array": "array(.array_value)",
  "object_array": "array(.object_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_array": null,
  "same_array": [1, 2],
  "object_array": [
    {"key": "a", "value": 1},
    {"key": "b", "value": 2}
  ]
}
```
</details>
</br>

### _is-array(value) -> boolean_

True iff `value` is an array.

Examples:

```
is-array(null)   => false
is-array([1, 2]) => true
is-array("123")  => false
```

<details>
<summary><strong>Study Cases Is Array</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "null_value": null,
  "array_value": [1, 2],
  "string_value": "123"
}
```
<b>JSON FUNCTION</b>

```json
{
  "null_check": "is-array(.null_value)",
  "array_check": "is-array(.array_value)",
  "string_check": "is-array(.string_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "null_check": false,
  "array_check": true,
  "string_check": false
}
```
</details>
</br>

### _flatten(array) -> array_

Flattens an array containing other arrays so that every value inside a
sub-array is contained directly in the output array. All sub-arrays at
any level of nesting are flattened, but objects and other values are
left untouched.

Examples:

```
flatten([[1,2], [3,4]])         => [1,2,3,4]
flatten([1, 2, 3, 4])           => [1,2,3,4]
flatten([1, [2, [3, [4, []]]]]) => [1,2,3,4]
flatten(null)                   => null
```

<details>
<summary><strong>Study Cases Flatten</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "nested_pairs": [[1, 2], [3, 4]],
  "flat": [1, 2, 3, 4],
  "deep": [1, [2, [3, [4, []]]]],
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "flatten_pairs": "flatten(.nested_pairs)",
  "flatten_flat": "flatten(.flat)",
  "flatten_deep": "flatten(.deep)",
  "flatten_null": "flatten(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "flatten_pairs": [1, 2, 3, 4],
  "flatten_flat": [1, 2, 3, 4],
  "flatten_deep": [1, 2, 3, 4],
  "flatten_null": null
}
```
</details>
</br>

### _all(array) -> boolean_

True iff all elements of `array` evaluates to `true`

Examples:

```
all([true, true, true])         => true
all([true, true, false])        => false
all(null)                       => null
all([])                         => true
all("")                         => error
```

<details>
<summary><strong>Study Cases All</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "all_true": [true, true, true],
  "has_false": [true, true, false],
  "null_value": null,
  "empty": []
}
```
<b>JSON FUNCTION</b>

```json
{
  "all_true_check": "all(.all_true)",
  "has_false_check": "all(.has_false)",
  "null_check": "all(.null_value)",
  "empty_check": "all(.empty)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "all_true_check": true,
  "has_false_check": false,
  "null_check": null,
  "empty_check": true
}
```
</details>
</br>

### _any(array) -> boolean_

True iff any elements of `array` evaluates to `true`.

Examples:

```
any([false, false, false])      => false
any([false, false, true])       => true
any(null)                       => null
any([])                         => false
any("")                         => error
```

<details>
<summary><strong>Study Cases Any</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "all_false": [false, false, false],
  "has_true": [false, false, true],
  "null_value": null,
  "empty": []
}
```
<b>JSON FUNCTION</b>

```json
{
  "all_false_check": "any(.all_false)",
  "has_true_check": "any(.has_true)",
  "null_check": "any(.null_value)",
  "empty_check": "any(.empty)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "all_false_check": false,
  "has_true_check": true,
  "null_check": null,
  "empty_check": false
}
```
</details>
</br>

### _zip(array1, array2) -> array_

Folds the two arrays into a new array consisting of two-element
arrays, where the first two-element array contains the first element
of `array1`and the first element of `array2`, the second has the two
second elements, and so on. It is an error if the two arrays are of
different lengths.

Examples:

```
zip(["a", "b", "c"], [1, 2, 3]) => [["a", 1], ["b", 2], ["c", 3]]
zip(["a", "b", "c"], null)      => null
zip(null, [1, 2, 3])            => null
zip([], [])                     => []
zip([1], [])                    => error
```

<details>
<summary><strong>Study Cases Zip</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "letters": ["a", "b", "c"],
  "numbers": [1, 2, 3],
  "empty": [],
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "zipped": "zip(.letters, .numbers)",
  "zip_null_second": "zip(.letters, .null_value)",
  "zip_null_first": "zip(.null_value, .numbers)",
  "zip_empty": "zip(.empty, .empty)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "zipped": [["a", 1], ["b", 2], ["c", 3]],
  "zip_null_second": null,
  "zip_null_first": null,
  "zip_empty": []
}
```
</details>
</br>

### _zip-with-index(array) ->  array_

Turns an array into a new array where each element in the input array
is mapped to an object of the form `{"value" : <array element>,
"index" : <index of element>"}`. The indexes start at zero. It is an
error not to pass an array (or `null`).

```
zip-with-index(["a", "b", "c"]) => [{"value" : "a", "index" : 0},
                                    {"value" : "b", "index" : 1},
                                    {"value" : "c", "index" : 2}]
zip-with-index([])              => []
zip-with-index(null)            => null
zip-with-index("abc")           => error
```

<details>
<summary><strong>Study Cases Zip With Index</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "letters": ["a", "b", "c"],
  "empty": [],
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "indexed": "zip-with-index(.letters)",
  "empty_indexed": "zip-with-index(.empty)",
  "null_indexed": "zip-with-index(.null_value)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "indexed": [
    {"value": "a", "index": 0},
    {"value": "b", "index": 1},
    {"value": "c", "index": 2}
  ],
  "empty_indexed": [],
  "null_indexed": null
}
```
</details>
</br>

### _index-of(array, value) -> integer_

Returns the index of `value` within `array`, or `-1` if it cannot be
found. It's an error if `array` is not an array (or `null`).

```
index-of([], 1)                 => -1
index-of([0, 1, 2], 1)          => 1
index-of([0, 1, 2, null], null) => 3
index-of([0, 1, 2], null)       => -1
index-of(null, 1)               => null
index-of(1, 1)                  => error
```

<details>
<summary><strong>Study Cases Index Of</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "empty": [],
  "numbers": [0, 1, 2],
  "with_null": [0, 1, 2, null],
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "empty_index": "index-of(.empty, 1)",
  "found_index": "index-of(.numbers, 1)",
  "null_in_array": "index-of(.with_null, null)",
  "null_not_found": "index-of(.numbers, null)",
  "null_array": "index-of(.null_value, 1)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "empty_index": -1,
  "found_index": 1,
  "null_in_array": 3,
  "null_not_found": -1,
  "null_array": null
}
```
</details>
</br>

<!-- TIME ===================================================================-->

## Time functions

### _now() -> double_

Returns the number of seconds since midnight, January 1, 1970 UTC in
the UTC timezone. Milliseconds are returned as decimals of the number.

Examples:

```
now()        -> 1.529677371698E9
round(now()) -> 1529677391
```

<details>
<summary><strong>Study Cases Now</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{}
```
<b>JSON FUNCTION</b>

```json
{
  "now_value": "now()",
  "now_rounded": "round(now())",
  "is_number": "is-number(now())"
}
```
<b>JSON OUTPUT</b>

```json
{
  "now_value": 1529677371.698,
  "now_rounded": 1529677391,
  "is_number": true
}
```
</details>
</br>

### _parse-time(time, format, fallback?) -> double_

Parses `time` with `format` (specified in Java date/time format) and
returns the number of seconds since the epoch in the UTC timezone. If
no timezone is specified in the `time` string, the timezone is assumed
to be UTC.

If `fallback` is not specified, the function will cause an error if
`time` is of the wrong type or does not match the format. If
`fallback` is specified that value will be returned instead.

Examples:

```
parse-time("2018-05-30T11:46:37Z", "yyyy-MM-dd'T'HH:mm:ssX") => 1.527680797E9
parse-time("2018-05-30T11:46:37", "yyyy-MM-dd'T'HH:mm:ssX")  => error
parse-time("2018-05-30T11:46:37", "yyyy-MM-dd'T'HH:mm:ssX", null)  => null
parse-time(null, "yyyy-MM-dd'T'HH:mm:ssX")                   => null
```

<details>
<summary><strong>Study Cases Parse Time</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "utc_time": "2018-05-30T11:46:37Z",
  "no_timezone": "2018-05-30T11:46:37",
  "format": "yyyy-MM-dd'T'HH:mm:ssX",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "parsed": "parse-time(.utc_time, .format)",
  "invalid_with_fallback": "parse-time(.no_timezone, .format, null)",
  "null_parsed": "parse-time(.null_value, .format)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "parsed": 1527680797.0,
  "invalid_with_fallback": null,
  "null_parsed": null
}
```
</details>
</br>

### _format-time(timestamp, format, timezone?) -> string_

Formats `timestamp` (the number of seconds since epoch) with `format`
and returns the formatted string. The timezone is assumed to be UTC,
but this can be overridden with the `timezone` argument.

Examples:

```
format-time(1529677391, "yyyy-MM-dd'T'HH:mm:ss") => "2018-06-22T14:23:11"
format-time(0, "yyyy-MM-dd")                     => "1970-01-01"
format-time(null, "yyyy-MM-dd")                  => null
```

<details>
<summary><strong>Study Cases Format Time</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "timestamp": 1529677391,
  "epoch": 0,
  "datetime_format": "yyyy-MM-dd'T'HH:mm:ss",
  "date_format": "yyyy-MM-dd",
  "null_value": null
}
```
<b>JSON FUNCTION</b>

```json
{
  "formatted": "format-time(.timestamp, .datetime_format)",
  "epoch_date": "format-time(.epoch, .date_format)",
  "null_formatted": "format-time(.null_value, .date_format)"
}
```
<b>JSON OUTPUT</b>

```json
{
  "formatted": "2018-06-22T14:23:11",
  "epoch_date": "1970-01-01",
  "null_formatted": null
}
```
</details>
</br>

<!-- Misc ===================================================================-->

## Miscellaneous functions

### _parse-url(url) -> object_

Parses `url` and returns an object with keys [`scheme`, `userinfo`, `host`, `port` `path`, `query`, `parameters`, `fragment` ]

```
parse-url("http://example.com").scheme => "http"
parse-url("http://example.com").host => "example.com"
parse-url("http://example.com").path => null
parse-url("http://example.com/").path = "/"
parse-url("https://www.example.com/?aa=1&aa=2&bb=&cc").query =>  "aa=1&aa=2&bb=&cc"
parse-url("https://www.example.com/?aa=1&aa=2&bb=&cc").parameters.aa =>  ["1", "2"]
parse-url("https://www.example.com/?aa=1&aa=2&bb=&cc").parameters.bb =>  [null]
parse-url("https://www.example.com/?aa=1&aa=2&bb=&cc").parameters.cc =>  [null]
parse-url("ftp://username:password@host.com/").userinfo => "username:password"
parse-url("https://example.com:8443").port => 8443
```

<details>
<summary><strong>Study Cases Parse Url</strong></summary>
</br>
<b>JSON INPUT</b>

```json
{
  "simple": "http://example.com",
  "with_slash": "http://example.com/",
  "with_query": "https://www.example.com/?aa=1&aa=2&bb=&cc",
  "with_userinfo": "ftp://username:password@host.com/",
  "with_port": "https://example.com:8443"
}
```
<b>JSON FUNCTION</b>

```json
{
  "scheme": "parse-url(.simple).scheme",
  "host": "parse-url(.simple).host",
  "path_missing": "parse-url(.simple).path",
  "path_slash": "parse-url(.with_slash).path",
  "query": "parse-url(.with_query).query",
  "param_aa": "parse-url(.with_query).parameters.aa",
  "param_bb": "parse-url(.with_query).parameters.bb",
  "param_cc": "parse-url(.with_query).parameters.cc",
  "userinfo": "parse-url(.with_userinfo).userinfo",
  "port": "parse-url(.with_port).port"
}
```
<b>JSON OUTPUT</b>

```json
{
  "scheme": "http",
  "host": "example.com",
  "path_missing": null,
  "path_slash": "/",
  "query": "aa=1&aa=2&bb=&cc",
  "param_aa": ["1", "2"],
  "param_bb": [null],
  "param_cc": [null],
  "userinfo": "username:password",
  "port": 8443
}
```
</details>
</br>

## Implementing extension functions

Documented [here](extensions.md).
