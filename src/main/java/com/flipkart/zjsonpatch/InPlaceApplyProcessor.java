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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

class InPlaceApplyProcessor implements JsonPatchProcessor {

    private JsonElement target;
    private EnumSet<CompatibilityFlags> flags;

    InPlaceApplyProcessor(JsonElement target) {
        this(target, CompatibilityFlags.defaults());
    }

    InPlaceApplyProcessor(JsonElement target, EnumSet<CompatibilityFlags> flags) {
        this.target = target;
        this.flags = flags;
    }

    public JsonElement result() {
        return target;
    }

    @Override
    public void move(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException {
    	JsonElement valueNode = fromPath.evaluate(target);
        remove(fromPath);
        set(toPath, valueNode, Operation.MOVE);
    }

    @Override
    public void copy(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException {
    	JsonElement valueNode = fromPath.evaluate(target);
    	JsonElement valueToCopy = valueNode != null ? valueNode.deepCopy() : null;
        set(toPath, valueToCopy, Operation.COPY);
    }

    private static String show(JsonElement value) {
        if (value == null || value.isJsonNull())
            return "null";
        else if (value.isJsonArray())
            return "array";
        else if (value.isJsonObject())
            return "object";
        else
            return "value " + value.toString();     // Caveat: numeric may differ from source (e.g. trailing zeros)
    }

    @Override
    public void test(JsonPointer path, JsonElement value) throws JsonPointerEvaluationException {
    	JsonElement valueNode = path.evaluate(target);
        if (!valueNode.equals(value))
            throw new JsonPatchApplicationException(
                    "Expected " + show(value) + " but found " + show(valueNode), Operation.TEST, path);
    }

    @Override
    public void add(JsonPointer path, JsonElement value) throws JsonPointerEvaluationException {
        set(path, value, Operation.ADD);
    }

    @Override
    public void replace(JsonPointer path, JsonElement value) throws JsonPointerEvaluationException {
        if (path.isRoot()) {
            target = value;
            return;
        }

        JsonElement parentNode = path.getParent().evaluate(target);
        JsonPointer.RefToken token = path.last();
        if (parentNode.isJsonObject()) {
            if (!flags.contains(CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE) &&
                    !parentNode.getAsJsonObject().has(token.getField()))
                throw new JsonPatchApplicationException(
                        "Missing field \"" + token.getField() + "\"", Operation.REPLACE, path.getParent());
            parentNode.getAsJsonObject().add(token.getField(), value);
        } else if (parentNode.isJsonArray()) {
            if (token.getIndex() >= parentNode.getAsJsonArray().size())
                throw new JsonPatchApplicationException(
                        "Array index " + token.getIndex() + " out of bounds", Operation.REPLACE, path.getParent());
            parentNode.getAsJsonArray().set(token.getIndex(), value);
        } else {
            throw new JsonPatchApplicationException(
                    "Can't reference past scalar value", Operation.REPLACE, path.getParent());
        }
    }

    @Override
    public void remove(JsonPointer path) throws JsonPointerEvaluationException {
        if (path.isRoot())
            throw new JsonPatchApplicationException("Cannot remove document root", Operation.REMOVE, path);

        JsonElement parentNode = path.getParent().evaluate(target);
        JsonPointer.RefToken token = path.last();
        if (parentNode.isJsonObject()) {
            if (flags.contains(CompatibilityFlags.FORBID_REMOVE_MISSING_OBJECT) && !parentNode.getAsJsonObject().has(token.getField()))
                throw new JsonPatchApplicationException(
                        "Missing field " + token.getField(), Operation.REMOVE, path.getParent());
            parentNode.getAsJsonObject().remove(token.getField());
        }
        else if (parentNode.isJsonArray()) {
            if (!flags.contains(CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT) &&
                    token.getIndex() >= parentNode.getAsJsonArray().size())
                throw new JsonPatchApplicationException(
                        "Array index " + token.getIndex() + " out of bounds", Operation.REMOVE, path.getParent());
            if (token.getIndex() >= parentNode.getAsJsonArray().size()) {
            	// ignore
            } else {
            	parentNode.getAsJsonArray().remove(token.getIndex());
            }
        } else {
            throw new JsonPatchApplicationException(
                    "Cannot reference past scalar value", Operation.REMOVE, path.getParent());
        }
    }

    private void set(JsonPointer path, JsonElement value, Operation forOp) throws JsonPointerEvaluationException {
        if (path.isRoot())
            target = value;
        else {
            JsonElement parentNode = path.getParent().evaluate(target);
            if (!(parentNode.isJsonArray() || parentNode.isJsonObject()))
                throw new JsonPatchApplicationException("Cannot reference past scalar value", forOp, path.getParent());
            else if (parentNode.isJsonArray())
                addToArray(path, value, parentNode);
            else
                addToObject(path, parentNode, value);
        }
    }

    private void addToObject(JsonPointer path, JsonElement node, JsonElement value) {
        String key = path.last().getField();
        node.getAsJsonObject().add(key, value);
    }

    private void addToArray(JsonPointer path, JsonElement value, JsonElement parentNode) {
        final JsonArray target = parentNode.getAsJsonArray();
        int idx = path.last().getIndex();

        if (idx == JsonPointer.LAST_INDEX) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            if (idx > target.size())
                throw new JsonPatchApplicationException(
                        "Array index " + idx + " out of bounds", Operation.ADD, path.getParent());
            
            // Shift elements to the right to make space for the new element
            for (int i = target.size() - 1; i >= idx; i--) {
            	if (i+1 >= target.size()) {
            		target.add(target.get(i));
            	} else {
            		target.set(i + 1, target.get(i));
            	}
            }
            
            // Insert the new element at the specified index
            if (idx >= target.size()) {
            	target.add(value);
            } else {
            	target.set(idx, value);
            }
        }
    }
}
