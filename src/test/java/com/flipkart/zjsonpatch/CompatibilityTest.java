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

import static com.flipkart.zjsonpatch.CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE;
import static com.flipkart.zjsonpatch.CompatibilityFlags.FORBID_REMOVE_MISSING_OBJECT;
import static com.flipkart.zjsonpatch.CompatibilityFlags.MISSING_VALUES_AS_NULLS;
import static com.flipkart.zjsonpatch.CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CompatibilityTest {

    Gson mapper;
    JsonElement addNodeWithMissingValue;
    JsonElement replaceNodeWithMissingValue;
    JsonElement removeNoneExistingArrayElement;
    JsonElement replaceNode;
    JsonElement removeNode;

    @Before
    public void setUp() throws Exception {
        mapper = new GsonBuilder().create();
        addNodeWithMissingValue = mapper.fromJson("[{\"op\":\"add\",\"path\":\"/a\"}]", JsonElement.class);
        replaceNodeWithMissingValue = mapper.fromJson("[{\"op\":\"replace\",\"path\":\"/a\"}]", JsonElement.class);
        removeNoneExistingArrayElement = mapper.fromJson("[{\"op\": \"remove\",\"path\": \"/b/0\"}]", JsonElement.class);
        replaceNode = mapper.fromJson("[{\"op\":\"replace\",\"path\":\"/a\",\"value\":true}]", JsonElement.class);
        removeNode = mapper.fromJson("[{\"op\":\"remove\",\"path\":\"/b\"}]", JsonElement.class);
    }

    @Test
    public void withFlagAddShouldTreatMissingValuesAsNulls() throws IOException {
    	JsonElement expected = mapper.fromJson("{\"a\":null}", JsonElement.class);
    	JsonElement result = JsonPatch.apply(addNodeWithMissingValue, new JsonObject(), EnumSet.of(MISSING_VALUES_AS_NULLS));
        assertThat(result, equalTo(expected));
    }

    @Test
    public void withFlagAddNodeWithMissingValueShouldValidateCorrectly() {
        JsonPatch.validate(addNodeWithMissingValue, EnumSet.of(MISSING_VALUES_AS_NULLS));
    }

    @Test
    public void withFlagReplaceShouldTreatMissingValuesAsNull() throws IOException {
    	JsonElement source = mapper.fromJson("{\"a\":\"test\"}", JsonElement.class);
        JsonElement expected = mapper.fromJson("{\"a\":null}", JsonElement.class);
        JsonElement result = JsonPatch.apply(replaceNodeWithMissingValue, source, EnumSet.of(MISSING_VALUES_AS_NULLS));
        assertThat(result, equalTo(expected));
    }

    @Test
    public void withFlagReplaceNodeWithMissingValueShouldValidateCorrectly() {
        JsonPatch.validate(addNodeWithMissingValue, EnumSet.of(MISSING_VALUES_AS_NULLS));
    }

    @Test
    public void withFlagIgnoreRemoveNoneExistingArrayElement() throws IOException {
    	JsonElement source = mapper.fromJson("{\"b\": []}", JsonElement.class);
    	JsonElement expected = mapper.fromJson("{\"b\": []}", JsonElement.class);
    	JsonElement result = JsonPatch.apply(removeNoneExistingArrayElement, source, EnumSet.of(REMOVE_NONE_EXISTING_ARRAY_ELEMENT));
        assertThat(result, equalTo(expected));
    }

    @Test
    public void withFlagReplaceShouldAddValueWhenMissingInTarget() throws Exception {
    	JsonElement expected = mapper.fromJson("{\"a\": true}", JsonElement.class);
    	JsonElement result = JsonPatch.apply(replaceNode, new JsonObject(), EnumSet.of(ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE));
        assertThat(result, equalTo(expected));
    }

    @Test(expected = JsonPatchApplicationException.class)
    public void withFlagRemoveMissingValueShouldThrow() throws Exception {
    	JsonElement source = mapper.fromJson("{\"a\": true}", JsonElement.class);
        JsonPatch.apply(removeNode, source, EnumSet.of(FORBID_REMOVE_MISSING_OBJECT));
    }

    @Test
    public void withFlagRemoveShouldRemove() throws Exception {
    	JsonElement source = mapper.fromJson("{\"b\": true}", JsonElement.class);
    	JsonElement expected = mapper.fromJson("{}", JsonElement.class);
    	JsonElement result = JsonPatch.apply(removeNode, source, EnumSet.of(FORBID_REMOVE_MISSING_OBJECT));
        assertThat(result, equalTo(expected));
    }
}
