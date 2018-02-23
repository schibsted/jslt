
# Time functions

The immediate need is for the Amplitude transforms, where we need to
be able to parse date/time formats into milliseconds since epoch.
However, people will almost certainly also need to format date/time
values.  So we need to add functions for this to the language.
Unfortunately, time is a complicated mess, so there are some tradeoffs
involved.

## Timestamps only

One approach is to implement these three functions:

```
parse-time("<datetime string>", "format") -> timestamp
format-time(timestamp, "format") -> "<datetime string>"
format-time(timestamp, "format", "timezone") -> "<datetime string>"
now() -> timestamp
```

The `timestamp` here is seconds since the epoch in UTC, expressed as a
float so that we get fractional seconds in there. So if the input
string has a timezone, that's normalized to UTC. Usually, you don't
care about this, and can format back to the format you want to display
in.  If you need to present the timestamp in a specific timezone you
can do that by specifying the optional argument.

The downside here is that you cannot:
 * reformat a date/time and preserve the timezone,
 * get at the component parts of a timestamp (day, hour, year), without
   adding one more function, or
 * extract the timezone from a date/time string.

## Timestamps and time objects

It would be possible to implement the following:

```
parse-time("<datetime string>", "format") -> time object
format-time(time object, "format") -> "<datetime string>"
mktime(time object) -> seconds since epoch
time-in-zone(seconds since epoch, "timezone") -> time object
now() -> seconds since epoch
```

A time object would be a JSON object with a structure like:

```
{
  "year" : 2018,
  "month" : 2,
  "day" : 16,
  "hour" : 14,
  "minute" : 43,
  "second" : 27,
  "fraction" : 0.72123242323, // or however many decimals are available
  "timezone" : "CET"
}
```

The downside here is:
 * our simple use case now involves two functions,
 * there are four functions, not three,
 * all use cases involve creating an expensive intermediate object, and
 * times may or may not carry timezone information, which is really
   presentation information, and not part of the time itself.

## Compromise

We could implement simply:

```
parse-timestamp("<datetime string>", "format") -> seconds since epoch UTC
```

This covers the simple use case, without burning any bridges or
introducing any unnecessary overhead.

## Alternative thoughts

Øyvind Løkling noted that one alternative would be to express the
timestamp as an integer in milliseconds instead of using decimal
seconds.

Fredrik Vraalsen wanted high precision if the timestamp is going to be
a decimal, preferring BigDecimal, then double, and definitely not float.

Mårten Rånge: "Anyway, double has caused me a lot of grief over the
years but that’s mainly when dealing with exact amounts. Time
measurements have uncertainity about them anyway. I think second since
epoch is what it should be and let subseconds be represented as a
fraction of a second"

## Other languages

Python `time.time()` returns a decimal number representing seconds,
with the decimal part being fractional seconds.

Ruby `Time.now` returns a `Datetime` object, where `to_i` will give
seconds since epoch as an integer, and `to_f` will give seconds since
epoch as a decimal number with fractions.

XPath 3.0 inherits the XML Schema type system and is therefore
[basically weird](https://www.w3.org/TR/xpath-functions-31/#parsing-dates-and-times).

JsonPath doesn't have time functions.

jq represents functions as arrays, basically with the same information
as the time objects suggested above.

JSONata has [limited time
support](http://docs.jsonata.org/string-functions.html#frommillisnumber),
but does represent time as milliseconds since Unix epoch.