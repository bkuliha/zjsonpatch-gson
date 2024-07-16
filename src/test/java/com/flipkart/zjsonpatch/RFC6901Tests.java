package com.flipkart.zjsonpatch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class RFC6901Tests {
    @Test
    void testRFC6901Compliance() throws IOException {
        JsonElement data = TestUtils.loadResourceAsJsonNode("/rfc6901/data.json");
        JsonElement testData = data.getAsJsonObject().get("testData");

        JsonObject emptyJson = new JsonObject();
        JsonArray patch = JsonDiff.asJson(emptyJson, testData);
        JsonElement result = JsonPatch.apply(patch, emptyJson);
        assertEquals(testData, result);
    }
}
