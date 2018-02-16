
# Time functions

The immediate need is for the Amplitude transforms, where we need to
be able to parse date/time formats into seconds since epoch.  However,
people will almost certainly also need to format date/time values. So
we need to add functions for this to the language. Unfortunately, time
is a complicated mess, so there are some tradeoffs involved.

## Timestamps only

One approach is to implement these two functions:

```
parse-time("<datetime string>", "format") -> timestamp
format-time(timestamp, "format") -> "<datetime string>"
format-time(timestamp, "format", "timezone") -> "<datetime string>"
```

The `timestamp` here is seconds since the epoch in UTC. So if the
input string has a timezone, that's normalized to UTC. Usually, you
don't care about this, and can format back to the format you want.  If
you need to present the timestamp in a specific timezone you can do
that by specifying the optional argument.

The downside here is that you cannot:
 * reformat a date/time and preserve the timezone,
 * get at the component parts of a timestamp (day, hour, year), without
   adding one more functoin, or
 * extract the timezone from a date/time string.

## Timestamps and time objects

It would be possible to implement the following:

```
parse-time("<datetime string>", "format") -> time object
format-time(time object, "format") -> "<datetime string>"
mktime(time object) -> seconds since epoch
localtime(seconds since epoch) -> time object
utctime(seconds since epoch) -> time object
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
  "timezone" : "CET"
}
```

The downside here is:
 * our simple use case now involves two functions,
 * there are five functions, not two,
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