/*
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.zjsonpatch;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Unit test
 */
public class JsonDiffTest {
    private static Gson objectMapper = new GsonBuilder().create();
    private static JsonArray jsonNode;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String path = "/testdata/sample.json";
        InputStream resourceAsStream = JsonDiffTest.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        jsonNode = (JsonArray) objectMapper.fromJson(testData, JsonArray.class);
    }

    @Test
    public void testSampleJsonDiff() {
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonElement first = jsonNode.get(i).getAsJsonObject().get("first");
            JsonElement second = jsonNode.get(i).getAsJsonObject().get("second");
            JsonArray actualPatch = JsonDiff.asJson(first, second);
            JsonElement secondPrime = JsonPatch.apply(actualPatch, first);
            Assert.assertEquals("JSON Patch not symmetrical [index=" + i + ", first=" + first + "]", second, secondPrime);
        }
    }

    @Test
    public void testGeneratedJsonDiff() {
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
        	JsonElement first = TestDataGenerator.generate(random.nextInt(10));
        	JsonElement second = TestDataGenerator.generate(random.nextInt(10));
        	JsonArray actualPatch = JsonDiff.asJson(first, second);
        	JsonElement secondPrime = JsonPatch.apply(actualPatch, first);
            Assert.assertEquals(second, secondPrime);
        }
    }

    @Test
    public void testRenderedRemoveOperationOmitsValueByDefault() {
        JsonObject source = new JsonObject();
        JsonObject target = new JsonObject();
        source.addProperty("field", "value");

        JsonArray diff = JsonDiff.asJson(source, target);

        Assert.assertEquals(Operation.REMOVE.rfcName(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("op")));
        Assert.assertEquals("/field", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("path")));
        Assert.assertNull(diff.get(0).getAsJsonObject().get("value"));
    }

    @Test
    public void testRenderedRemoveOperationRetainsValueIfOmitDiffFlagNotSet() {
        JsonObject source = new JsonObject();
        JsonObject target = new JsonObject();
        source.addProperty("field", "value");

        EnumSet<DiffFlags> flags = DiffFlags.defaults().clone();
        Assert.assertTrue("Expected OMIT_VALUE_ON_REMOVE by default", flags.remove(DiffFlags.OMIT_VALUE_ON_REMOVE));
        JsonArray diff = JsonDiff.asJson(source, target, flags);

        Assert.assertEquals(Operation.REMOVE.rfcName(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("op")));
        Assert.assertEquals("/field", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("path")));
        Assert.assertEquals("value", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("value")));
    }

    @Test
    public void testRenderedOperationsExceptMoveAndCopy() throws Exception {
    	JsonElement source = objectMapper.fromJson("{\"age\": 10}", JsonElement.class);
        JsonElement target = objectMapper.fromJson("{\"height\": 10}", JsonElement.class);

        EnumSet<DiffFlags> flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone(); //only have ADD, REMOVE, REPLACE, Don't normalize operations into MOVE & COPY

        JsonArray diff = JsonDiff.asJson(source, target, flags);

        for (JsonElement d : diff) {
            Assert.assertNotEquals(Operation.MOVE.rfcName(), TestUtils.getTextValue(d.getAsJsonObject().get("op")));
            Assert.assertNotEquals(Operation.COPY.rfcName(), TestUtils.getTextValue(d.getAsJsonObject().get("op")));
        }

        JsonElement targetPrime = JsonPatch.apply(diff, source);
        Assert.assertEquals(target, targetPrime);
    }

    @Test
    public void testPath() throws Exception {
    	JsonElement source = objectMapper.fromJson("{\"profiles\":{\"abc\":[],\"def\":[{\"hello\":\"world\"}]}}", JsonElement.class);
    	JsonElement patch = objectMapper.fromJson("[{\"op\":\"copy\",\"from\":\"/profiles/def/0\", \"path\":\"/profiles/def/0\"},{\"op\":\"replace\",\"path\":\"/profiles/def/0/hello\",\"value\":\"world2\"}]", JsonElement.class);

    	JsonElement target = JsonPatch.apply(patch, source);
    	JsonElement expected = objectMapper.fromJson("{\"profiles\":{\"abc\":[],\"def\":[{\"hello\":\"world2\"},{\"hello\":\"world\"}]}}", JsonElement.class);
        Assert.assertEquals(target, expected);
    }

    @Test
    public void testJsonDiffReturnsEmptyNodeExceptionWhenBothSourceAndTargetNodeIsNull() {
    	JsonElement diff = JsonDiff.asJson(null, null);
        assertEquals(0, diff.getAsJsonArray().size());
    }

    @Test
    public void testJsonDiffShowsDiffWhenSourceNodeIsNull() throws Exception {
        String target = "{ \"K1\": {\"K2\": \"V1\"} }";
        JsonArray diff = JsonDiff.asJson(null, objectMapper.fromJson(target, JsonElement.class));
        assertEquals(1, diff.getAsJsonArray().size());

        System.out.println(diff);
        assertEquals(Operation.ADD.rfcName(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("op")));
        assertEquals(JsonPointer.ROOT.toString(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("path")));
        assertEquals("V1", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("value").getAsJsonObject().get("K1").getAsJsonObject().get("K2")));
    }

    @Test
    public void testJsonDiffShowsDiffWhenTargetNodeIsNullWithFlags() throws Exception {
        String source = "{ \"K1\": \"V1\" }";
        JsonElement sourceNode = objectMapper.fromJson(source, JsonElement.class);
        JsonArray diff = JsonDiff.asJson(sourceNode, null, EnumSet.of(DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE));

        assertEquals(1, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("op")));
        assertEquals(JsonPointer.ROOT.toString(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("path")));
        assertEquals("V1", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("value").getAsJsonObject().get("K1")));
    }
}
