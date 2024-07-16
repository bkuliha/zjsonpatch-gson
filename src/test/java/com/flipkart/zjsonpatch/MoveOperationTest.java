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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * @author ctranxuan (streamdata.io).
 */
public class MoveOperationTest extends AbstractTest {

	private static final Gson MAPPER = new GsonBuilder().create();

    @Parameterized.Parameters
    public static Collection<PatchTestCase> data() throws IOException {
        return PatchTestCase.load("move");
    }

    @Test
    public void testMoveValueGeneratedHasNoValue() throws IOException {
        JsonElement jsonNode1 = MAPPER.fromJson("{ \"foo\": { \"bar\": \"baz\", \"waldo\": \"fred\" }, \"qux\": { \"corge\": \"grault\" } }", JsonElement.class);
        JsonElement jsonNode2 = MAPPER.fromJson("{ \"foo\": { \"bar\": \"baz\" }, \"qux\": { \"corge\": \"grault\", \"thud\": \"fred\" } }", JsonElement.class);
        JsonElement patch = MAPPER.fromJson("[{\"op\":\"move\",\"from\":\"/foo/waldo\",\"path\":\"/qux/thud\"}]", JsonElement.class);

        JsonArray diff = JsonDiff.asJson(jsonNode1, jsonNode2);

        assertThat(diff, equalTo(patch));
    }

    @Test
    public void testMoveArrayGeneratedHasNoValue() throws IOException {
    	JsonElement jsonNode1 = MAPPER.fromJson("{ \"foo\": [ \"all\", \"grass\", \"cows\", \"eat\" ] }", JsonElement.class);
    	JsonElement jsonNode2 = MAPPER.fromJson("{ \"foo\": [ \"all\", \"cows\", \"eat\", \"grass\" ] }", JsonElement.class);
        JsonElement patch = MAPPER.fromJson("[{\"op\":\"move\",\"from\":\"/foo/1\",\"path\":\"/foo/3\"}]", JsonElement.class);

        JsonArray diff = JsonDiff.asJson(jsonNode1, jsonNode2);

        assertThat(diff, equalTo(patch));
    }
}
