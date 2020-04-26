
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

### _error(message)_

Halts the transform with an error. The message is the message given to
the user.

Examples:

```
if (not(is-array(.things)))
  error("'things' is not an array")
```

### _fallback(arg1, arg2, ...) -> value_

Returns the first argument that has a value. That is, the first
argument that is not `null`, `[]`, or `{}`.

Examples:

```
fallback(.not_existing_key, .another_not_existing, 1)  => 1
fallback(null, [], {}, "value")                        => "value"
```

### _min(arg1, arg2) -> value_

Returns the argument that compares as the smallest. If one argument is
`null` the result is `null`.

Examples:

```
min(10, 1)    -> 1
min("a", "b") -> "a"
min(10, null) -> null
```

### _max(arg1, arg2) -> value_

Returns the argument that compares as the largest. If one argument is
`null` the result is `null`.

Examples:

```
max(10, 1)    -> 10
max("a", "b") -> "b"
max(10, null) -> null
```

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

### _is-integer(object) -> boolean_

True iff the argument is an integral number.

Examples:

```
is-integer(null) => false
is-integer(1)    => true
is-integer(1.0)  => false
is-integer("1")  => false
```

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

### _number(object, fallback?) -> integer|float_

Converts the argument into a number, if possible. Decimals and
integers will be returned untouched. Strings are parsed into numbers.
`null` returns `null`. All other types cause an error, unless
`fallback` is specified.

If `fallback` is specified then if `object` is of the wrong type, or
if it is a string that cannot be parsed, then the `fallback` value is
returned.

The number format supported is the same as in JSON literals, except
that leading zeroes are allowed.

Examples:

```
number(23)    => 23
number("23")  => 23
number("023") => 23
number(23.0)  => 23.0
number(null)  => null
number("ab")  => error
```

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

### _random() -> float_

Returns a random number between `0.0` and `1.0`.

Examples:

```
random() => 0.24712712424
```

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

### _lowercase(string) -> string_

Converts the input string to lowercase. Note that this is a naive
function that does not handle all the special Unicode cases.

Examples:

```
lowercase("ABCÆØÅ") => "abcæøå"
lowercase(null)     => null
```

### _uppercase(string) -> string_

Converts the input string to uppercase. Note that this is a naive
function that only handles ASCII characters.

Examples:

```
uppercase("abcæøå") => "ABCÆØÅ"
uppercase(null)     => null
```

### _sha256-hex(string) -> string_

Generates a string with the hexadecimal representation of the SHA256 hash of the input string.

Examples:

```
sha256-hex("foo") => "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae"
sha256-hex("42")  => "73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049"
sha256-hex(42)    => "73475cb40a568e8da8a045ced110137e159f890ac4da883b6b17dc651b3a8049"
sha256-hex(null)  => null
```

### _starts-with(tested, prefix) -> boolean_

True iff the `tested` string starts with `prefix`.

Examples:

```
starts-with("prohibition", "pro") => true
starts-with("prohibition", "pre") => false
starts-with(null, "pre")          => false
```

### _ends-with(tested, suffix) -> boolean_

True iff the `tested` string ends with `suffix`.

Examples:

```
ends-with("prohibition", "pro") => false
ends-with("prohibition", "ion") => true
ends-with(null, "ion")          =>
```

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
boolean(false) => true
boolean([])    => false
boolean([1])   => true
```

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
not(false) => false
not([])    => true
not([1])   => false
```

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

### _is-array(value) -> boolean_

True iff `value` is an array.

Examples:

```
is-array(null)   => false
is-array([1, 2]) => true
is-array("123")  => false
```

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