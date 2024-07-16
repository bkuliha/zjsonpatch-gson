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

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RunWith(Parameterized.class)
public abstract class AbstractTest {

    @Parameter
    public PatchTestCase p;

    protected boolean matchOnErrors() {
        return true;
    }

    @Test
    public void test() throws Exception {
        if (p.isOperation()) {
            testOperation();
        } else {
            testError();
        }
    }

    private void testOperation() throws Exception {
        JsonElement node = p.getNode();

        JsonElement doc = node.getAsJsonObject().get("node");
        JsonElement expected = node.getAsJsonObject().get("expected");
        JsonElement patch = node.getAsJsonObject().get("op");
        String message = node.getAsJsonObject().has("message") ? node.getAsJsonObject().get("message").toString() : "";

        JsonElement result = JsonPatch.apply(patch, doc);
        String failMessage = "The following test failed: \n" +
                "message: " + message + '\n' +
                "at: " + p.getSourceFile();
        assertEquals(failMessage, expected, result);
    }

    private Class<?> exceptionType(String type) throws ClassNotFoundException {
        return Class.forName(type.contains(".") ? type : "com.flipkart.zjsonpatch." + type);
    }

    private String errorMessage(String header) throws Exception {
        return errorMessage(header, null);
    }
    private String errorMessage(String header, Exception e) throws Exception {
        StringBuilder res =
                new StringBuilder()
                        .append(header)
                        .append("\nFull test case (in file ")
                        .append(p.getSourceFile())
                        .append("):\n")
                        .append(new GsonBuilder().setPrettyPrinting().create().toJson(p.getNode()));
        if (e != null) {
            res.append("\nFull error: ");
            e.printStackTrace(new PrintWriter(new StringBuilderWriter(res)));
        }
        return res.toString();
    }

    private void testError() throws Exception, ClassNotFoundException {
        JsonObject node = p.getNode().getAsJsonObject();
        JsonElement first = node.get("node");
        JsonElement patch = node.get("op");
        JsonElement message = node.get("message");
        Class<?> type =
                node.has("type") ? exceptionType(TestUtils.getTextValue(node.get("type"))) : JsonPatchApplicationException.class;

        try {
            JsonPatch.apply(patch, first);

            fail(errorMessage("Failure expected: " + message));
        } catch (Exception e) {
            if (matchOnErrors()) {
                StringWriter fullError = new StringWriter();
                e.printStackTrace(new PrintWriter(fullError));

                assertThat(
                        errorMessage("Operation failed but with wrong exception type", e),
                        e,
                        instanceOf(type));
                if (message != null) {
                    assertThat(
                            errorMessage("Operation failed but with wrong message", e),
                            e.toString(),
                            containsString(TestUtils.getTextValue(message)));    // equalTo would be better, but fail existing tests
                }
            }
        }
    }
}
