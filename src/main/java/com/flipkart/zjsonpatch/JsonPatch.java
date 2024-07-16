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

import java.util.EnumSet;
import java.util.Iterator;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

/**
 * User: gopi.vishwakarma
 * Date: 31/07/14
 */
public final class JsonPatch {

    private JsonPatch() {
    }

    private static JsonElement getPatchStringAttr(JsonElement jsonNode, String attr) {
    	JsonElement child = getPatchAttr(jsonNode, attr);

        if (!(child.isJsonPrimitive() && child.getAsJsonPrimitive().isString()))
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (non-text '" + attr + "' field)");

        return child;
    }

    private static JsonElement getPatchAttr(JsonElement jsonNode, String attr) {
    	if (!jsonNode.isJsonObject()) {
    		throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");
    	}
    	JsonElement child = jsonNode.getAsJsonObject().get(attr);
        if (child == null)
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");

        return child;
    }

    private static JsonElement getPatchAttrWithDefault(JsonElement jsonNode, String attr, JsonElement defaultValue) {
    	if (!jsonNode.isJsonObject()) {
    		return defaultValue;
    	}
    	JsonElement child = jsonNode.getAsJsonObject().get(attr);
        if (child == null)
            return defaultValue;
        else
            return child;
    }
    
    private static String getTextValue(JsonElement jsonElement) {
    	if (jsonElement.isJsonPrimitive()) {
    		if (jsonElement.getAsJsonPrimitive().isString()) {
    			return jsonElement.getAsJsonPrimitive().getAsString();
    		}
    	}
    	return null;
    }

    private static void process(JsonElement patch, JsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags)
            throws InvalidJsonPatchException {

        if (!patch.isJsonArray())
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an array)");
        Iterator<JsonElement> operations = patch.getAsJsonArray().iterator();
        while (operations.hasNext()) {
        	JsonElement jsonNode = operations.next();
            if (!jsonNode.isJsonObject()) throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
            Operation operation = Operation.fromRfcName(getTextValue(getPatchStringAttr(jsonNode, Constants.OP)));
            JsonPointer path = JsonPointer.parse(getTextValue(getPatchStringAttr(jsonNode, Constants.PATH)));

            try {
                switch (operation) {
                    case REMOVE: {
                        processor.remove(path);
                        break;
                    }

                    case ADD: {
                    	JsonElement value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonNode, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, JsonNull.INSTANCE);
                        processor.add(path, value.deepCopy());
                        break;
                    }

                    case REPLACE: {
                    	JsonElement value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonNode, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, JsonNull.INSTANCE);
                        processor.replace(path, value.deepCopy());
                        break;
                    }

                    case MOVE: {
                        JsonPointer fromPath = JsonPointer.parse(getTextValue(getPatchStringAttr(jsonNode, Constants.FROM)));
                        processor.move(fromPath, path);
                        break;
                    }

                    case COPY: {
                        JsonPointer fromPath = JsonPointer.parse(getTextValue(getPatchStringAttr(jsonNode, Constants.FROM)));
                        processor.copy(fromPath, path);
                        break;
                    }

                    case TEST: {
                    	JsonElement value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonNode, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonNode, Constants.VALUE, JsonNull.INSTANCE);
                        processor.test(path, value.deepCopy());
                        break;
                    }
                }
            }
            catch (JsonPointerEvaluationException e) {
                throw new JsonPatchApplicationException(e.getMessage(), operation, e.getPath());
            }
        }
    }

    public static void validate(JsonElement patch, EnumSet<CompatibilityFlags> flags) throws InvalidJsonPatchException {
        process(patch, NoopProcessor.INSTANCE, flags);
    }

    public static void validate(JsonElement patch) throws InvalidJsonPatchException {
        validate(patch, CompatibilityFlags.defaults());
    }

    public static JsonElement apply(JsonElement patch, JsonElement source, EnumSet<CompatibilityFlags> flags) throws JsonPatchApplicationException {
        CopyingApplyProcessor processor = new CopyingApplyProcessor(source, flags);
        process(patch, processor, flags);
        return processor.result();
    }

    public static JsonElement apply(JsonElement patch, JsonElement source) throws JsonPatchApplicationException {
        return apply(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(JsonElement patch, JsonElement source) {
        applyInPlace(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(JsonElement patch, JsonElement source, EnumSet<CompatibilityFlags> flags) {
        InPlaceApplyProcessor processor = new InPlaceApplyProcessor(source, flags);
        process(patch, processor, flags);
    }
}
