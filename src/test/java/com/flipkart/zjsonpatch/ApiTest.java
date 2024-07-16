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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.EnumSet;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * User: holograph
 * Date: 03/08/16
 */
public class ApiTest {

    @Test
    public void applyInPlaceMutatesSource() throws Exception {
    	JsonElement patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
    	JsonObject source = newObjectNode();
        JsonObject beforeApplication = source.deepCopy();
        JsonPatch.apply(patch, source);
        assertThat(source, is(beforeApplication));
    }

    @Test
    public void applyDoesNotMutateSource() throws Exception {
    	JsonElement patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
    	JsonObject source = newObjectNode();
        JsonPatch.applyInPlace(patch, source);
        assertThat(findValue(source, "b").getAsString(), is("b-value"));
    }

    @Test
    public void applyDoesNotMutateSource2() throws Exception {
    	JsonElement patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
    	JsonObject source = newObjectNode();
        JsonObject beforeApplication = source.deepCopy();
        JsonPatch.apply(patch, source);
        assertThat(source, is(beforeApplication));
    }

    @Test
    public void applyInPlaceMutatesSourceWithCompatibilityFlags() throws Exception {
    	JsonElement patch = readTree("[{ \"op\": \"add\", \"path\": \"/b\" }]");
        JsonObject source = newObjectNode();
        JsonPatch.applyInPlace(patch, source, EnumSet.of(CompatibilityFlags.MISSING_VALUES_AS_NULLS));
        assertTrue(findValue(source, "b").isJsonNull());
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void applyingNonArrayPatchShouldThrowAnException() throws IOException {
    	JsonElement invalid = objectMapper.fromJson("{\"not\": \"a patch\"}", JsonElement.class);
        JsonElement to = readTree("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void applyingAnInvalidArrayShouldThrowAnException() throws IOException {
    	JsonElement invalid = readTree("[1, 2, 3, 4, 5]");
        JsonElement to = readTree("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void applyingAPatchWithAnInvalidOperationShouldThrowAnException() throws IOException {
    	JsonElement invalid = readTree("[{\"op\": \"what\"}]");
    	JsonElement to = readTree("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void validatingNonArrayPatchShouldThrowAnException() throws IOException {
    	JsonElement invalid = readTree("{\"not\": \"a patch\"}");
        JsonPatch.validate(invalid);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void validatingAnInvalidArrayShouldThrowAnException() throws IOException {
    	JsonElement invalid = readTree("[1, 2, 3, 4, 5]");
        JsonPatch.validate(invalid);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void validatingAPatchWithAnInvalidOperationShouldThrowAnException() throws IOException {
    	JsonElement invalid = readTree("[{\"op\": \"what\"}]");
        JsonPatch.validate(invalid);
    }

    private static Gson objectMapper = new GsonBuilder().create();

    private static JsonElement readTree(String jsonString) throws IOException {
        return objectMapper.fromJson(jsonString, JsonElement.class);
    }

    private JsonObject newObjectNode() {
        return new JsonObject();
    }
    
    private static JsonElement findValue(JsonObject jsonObject, String fieldName) {
        for (String key : jsonObject.keySet()) {
            JsonElement element = jsonObject.get(key);

            if (key.equals(fieldName)) {
                return element;
            }

            if (element.isJsonObject()) {
                JsonElement foundElement = findValue(element.getAsJsonObject(), fieldName);
                if (foundElement != null) {
                    return foundElement;
                }
            }
        }
        return null;
    }
    
}

