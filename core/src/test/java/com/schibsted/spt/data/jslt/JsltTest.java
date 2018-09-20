
package com.schibsted.spt.data.jslt;

import org.junit.Test;

public class JsltTest extends TestBase {
    @Test
    public void testRewriteObjectRootLevelMatcher() {
        String query =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#2.json\",\n" +
                "  \"taip\" : .type,\n" +
                "  * : .\n" +
                "}";
        String input =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#1.json\",\n" +
                "  \"id\" : \"94b27ca1-8729-4773-986b-1c0517dd6af1\",\n" +
                "  \"published\" : \"2017-05-04T09:13:29+02:00\",\n" +
                "  \"type\" : \"View\",\n" +
                "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\",\n" +
                "  \"url\" : \"http://www.aftenposten.no/\"\n" +
                "}\n";
        String result =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#2.json\",\n" +
                "  \"id\" : \"94b27ca1-8729-4773-986b-1c0517dd6af1\",\n" +
                "  \"published\" : \"2017-05-04T09:13:29+02:00\",\n" +
                "  \"taip\" : \"View\",\n" +
                "  \"type\" : \"View\",\n" +
                "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\",\n" +
                "  \"url\" : \"http://www.aftenposten.no/\"\n" +
                "}\n";
        check(input, query, result);
    }

    @Test
    public void testRewriteObjectNestedMatcher() {
        String query =
                "{\n" +
                "  \"foo\" : {\n" +
                "    \"hey\" : \"på deg\",\n" +
                "    * : .\n" +
                "  }\n" +
                "}\n";
        String input =
                "{\n" +
                "  \"foo\" : {\n" +
                "    \"type\" : \"View\",\n" +
                "    \"hey\" : \"ho\"\n" +
                "  }\n" +
                "}\n";
        String result =
                "{\n" +
                "  \"foo\" : {\n" +
                "    \"hey\" : \"på deg\",\n" +
                "    \"type\" : \"View\"\n" +
                "  }\n" +
                "}\n";
        check(input, query, result);
    }

    @Test
    public void testRewriteObjectLetMatcher() {
        String query =
                "{\n" +
                "  let bar = {* : .}\n" +
                "  \"fish\" : \"barrel\",\n" +
                "  \"copy\" : $bar\n" +
                "}";
        String input =
                "{\n" +
                "  \"if\" : \"View\",\n" +
                "  \"else\" : \"false\"\n" +
                "}\n";
        String result =
                "{\n" +
                "  \"fish\" : \"barrel\",\n" +
                "  \"copy\" : {\n" +
                "    \"if\" : \"View\",\n" +
                "    \"else\" : \"false\"\n" +
                "  }\n" +
                "}\n";
        check(input, query, result);
    }


    @Test
    public void testProduceEmptyObject() {
        String query =
                "{\n" +
                "  \"fish\" : \"barrel\",\n" +
                "  \"copy\" : {\n" +
                "    let bar = { * : . }\n" +
                "    \"dongle\" : $bar\n" +
                "  }\n" +
                "}\n";
        String input =
                "{\n" +
                "  \"if\" : \"View\",\n" +
                "  \"else\" : \"false\"\n" +
                "}\n";
        String result =
                "{\n" +
                "  \"fish\" : \"barrel\"\n" +
                "}\n";
        check(input, query, result);
    }

    @Test
    public void testProduceCopyOfObject() {
        String query =
                "{\n" +
                "  \"fish\" : \"barrel\",\n" +
                "  \"foo\" : if (.foo) { * : . }\n" +
                "}\n";
        String input =
                "{\n" +
                "  \"foo\" : {\n" +
                "    \"type\" : \"View\",\n" +
                "    \"hey\" : \"ho\"\n" +
                "  }\n" +
                "}\n";
        String result =
                "{\n" +
                "  \"fish\" : \"barrel\",\n" +
                "  \"foo\" : {\n" +
                "    \"type\" : \"View\",\n" +
                "    \"hey\" : \"ho\"\n" +
                "  }\n" +
                "}\n";
        check(input, query, result);
    }

    @Test
    public void testProduceArrayOfTransformedObjects() {
        String query =
                "{\"bar\" : [for (.list)\n" +
                "  {\"loop\" : \"for\",\n" +
                "   * : . }]\n" +
                "}\n";
        String input =
                "{\n" +
                "  \"list\" : [\n" +
                "    {\"bar\" : 1},\n" +
                "    {\"bar\" : 2},\n" +
                "    {\"bar\" : 3},\n" +
                "    {\"bar\" : 4},\n" +
                "    {\"bar\" : 5}\n" +
                "  ]\n" +
                "}\n";
        String result =
                "{\n" +
                "  \"bar\" : [ {\n" +
                "    \"loop\" : \"for\",\n" +
                "    \"bar\" : 1\n" +
                "  }, {\n" +
                "    \"loop\" : \"for\",\n" +
                "    \"bar\" : 2\n" +
                "  }, {\n" +
                "    \"loop\" : \"for\",\n" +
                "    \"bar\" : 3\n" +
                "  }, {\n" +
                "    \"loop\" : \"for\",\n" +
                "    \"bar\" : 4\n" +
                "  }, {\n" +
                "    \"loop\" : \"for\",\n" +
                "    \"bar\" : 5\n" +
                "  } ]\n" +
                "}\n";
        check(input, query, result);
    }

    @Test
    public void testRemoveType() {
        String query =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#2.json\",\n" +
                "  \"taip\" : .type ,\n" +
                "  * - \"type\" : .\n" +
                "}";
        String input =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#1.json\",\n" +
                "  \"id\" : \"94b27ca1-8729-4773-986b-1c0517dd6af1\",\n" +
                "  \"published\" : \"2017-05-04T09:13:29+02:00\",\n" +
                "  \"type\" : \"View\",\n" +
                "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\",\n" +
                "  \"url\" : \"http://www.aftenposten.no/\"\n" +
                "}\n";
        String result =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#2.json\",\n" +
                "  \"id\" : \"94b27ca1-8729-4773-986b-1c0517dd6af1\",\n" +
                "  \"published\" : \"2017-05-04T09:13:29+02:00\",\n" +
                "  \"taip\" : \"View\",\n" +
                "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\",\n" +
                "  \"url\" : \"http://www.aftenposten.no/\"\n" +
                "}\n";
        check(input, query, result);
    }

    @Test
    public void testRemoveTypeAndId() {
        String query =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#2.json\",\n" +
                "  \"taip\" : .type,\n" +
                "  * - \"type\", \"id\" : .\n" +
                "}";
        String input =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#1.json\",\n" +
                "  \"id\" : \"94b27ca1-8729-4773-986b-1c0517dd6af1\",\n" +
                "  \"published\" : \"2017-05-04T09:13:29+02:00\",\n" +
                "  \"type\" : \"View\",\n" +
                "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\",\n" +
                "  \"url\" : \"http://www.aftenposten.no/\"\n" +
                "}\n";
        String result =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#2.json\",\n" +
                "  \"published\" : \"2017-05-04T09:13:29+02:00\",\n" +
                "  \"taip\" : \"View\",\n" +
                "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\",\n" +
                "  \"url\" : \"http://www.aftenposten.no/\"\n" +
                "}\n";
        check(input, query, result);
    }

    @Test
    public void testRemoveTypeRemove() {
        String query =
                "{\n" +
                "  \"type\" : if (.type and .type != \"View\") .type,\n" +
                "  * : .\n" +
                "}";
        String input =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#1.json\",\n" +
                "  \"id\" : \"94b27ca1-8729-4773-986b-1c0517dd6af1\",\n" +
                "  \"published\" : \"2017-05-04T09:13:29+02:00\",\n" +
                "  \"type\" : \"View\",\n" +
                "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\",\n" +
                "  \"url\" : \"http://www.aftenposten.no/\"\n" +
                "}\n";
        String result =
                "{\n" +
                "  \"schema\" : \"http://schemas.schibsted.io/thing/pulse-simple.json#1.json\",\n" +
                "  \"id\" : \"94b27ca1-8729-4773-986b-1c0517dd6af1\",\n" +
                "  \"published\" : \"2017-05-04T09:13:29+02:00\",\n" +
                "  \"environmentId\" : \"urn:schibsted.com:environment:uuid\",\n" +
                "  \"url\" : \"http://www.aftenposten.no/\"\n" +
                "}\n";
        check(input, query, result);
    }

    @Test
    public void testRemoveBazNested() {
        String query =
                "{\n" +
                "    \"foo\" : {\n" +
                "    \"bar\" : {\n" +
                "      * - \"baz\" : .\n" +
                "    }\n" +
                "    }\n" +
                "}\n";
        String input =
                "{\n" +
                "    \"foo\" : {\n" +
                "    \"bar\" : {\n" +
                "        \"baz\" : 1,\n" +
                "        \"quux\" : 2\n" +
                "    }\n" +
                "    }\n" +
                "}\n";
        String result =
                "{\n" +
                "    \"foo\" : {\n" +
                "    \"bar\" : {\n" +
                "        \"quux\" : 2\n" +
                "    }\n" +
                "    }\n" +
                "}\n";
        check(input, query, result);
    }
}
