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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonElement;

public class PatchTestCase {

    private final boolean operation;
    private final JsonElement node;
    private final String sourceFile;

    private PatchTestCase(boolean isOperation, JsonElement node, String sourceFile) {
        this.operation = isOperation;
        this.node = node;
        this.sourceFile = sourceFile;
    }

    public boolean isOperation() {
        return operation;
    }

    public JsonElement getNode() {
        return node;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public static Collection<PatchTestCase> load(String fileName) throws IOException {
        String path = "/testdata/" + fileName + ".json";
        JsonElement tree = TestUtils.loadResourceAsJsonNode(path);

        List<PatchTestCase> result = new ArrayList<PatchTestCase>();
        for (JsonElement node : tree.getAsJsonObject().get("errors").getAsJsonArray()) {
            if (isEnabled(node)) {
                result.add(new PatchTestCase(false, node, path));
            }
        }
        for (JsonElement node : tree.getAsJsonObject().get("ops").getAsJsonArray()) {
            if (isEnabled(node)) {
                result.add(new PatchTestCase(true, node, path));
            }
        }
        return result;
    }

    private static boolean isEnabled(JsonElement node) {
    	if (!node.isJsonObject()) {
    		return true;
    	}
    	JsonElement disabled = node.getAsJsonObject().get("disabled");
        return (disabled == null || !disabled.isJsonPrimitive() || !disabled.getAsBoolean());
    }
}
