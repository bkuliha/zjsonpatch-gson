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

import java.util.EnumMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

enum NodeType {
    /**
     * Array nodes
     */
    ARRAY("array"),
    /**
     * Boolean nodes
     */
    BOOLEAN("boolean"),
    /**
     * Integer nodes
     */
    INTEGER("integer"),
    /**
     * Number nodes (ie, decimal numbers)
     */
    NULL("null"),
    /**
     * Object nodes
     */
    NUMBER("number"),
    /**
     * Null nodes
     */
    OBJECT("object"),
    /**
     * String nodes
     */
    STRING("string");

    /**
     * The name for this type, as encountered in a JSON schema
     */
    private final String name;

    private static final Map<JsonElementType, NodeType> TOKEN_MAP = new EnumMap<>(JsonElementType.class);

    static {
        TOKEN_MAP.put(JsonElementType.ARRAY, ARRAY);
        TOKEN_MAP.put(JsonElementType.BOOLEAN, BOOLEAN);
        TOKEN_MAP.put(JsonElementType.INTEGER, INTEGER);
        TOKEN_MAP.put(JsonElementType.NUMBER, NUMBER);
        TOKEN_MAP.put(JsonElementType.NULL, NULL);
        TOKEN_MAP.put(JsonElementType.OBJECT, OBJECT);
        TOKEN_MAP.put(JsonElementType.STRING, STRING);
    }

    NodeType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static NodeType getNodeType(final JsonElement element) {
        JsonElementType elementType = getElementType(element);
        final NodeType ret = TOKEN_MAP.get(elementType);
        if (ret == null) throw new NullPointerException("unhandled element type " + elementType);
        return ret;
    }
    
    private static JsonElementType getElementType(JsonElement element) {
        if (element.isJsonArray()) {
            return JsonElementType.ARRAY;
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return JsonElementType.BOOLEAN;
            } else if (primitive.isNumber()) {
                return primitive.getAsNumber() instanceof Integer ? JsonElementType.INTEGER : JsonElementType.NUMBER;
            } else if (primitive.isString()) {
                return JsonElementType.STRING;
            }
        } else if (element.isJsonNull()) {
            return JsonElementType.NULL;
        } else if (element.isJsonObject()) {
            return JsonElementType.OBJECT;
        }
        throw new IllegalArgumentException("Unknown element type: " + element);
    }

    private enum JsonElementType {
        ARRAY,
        BOOLEAN,
        INTEGER,
        NUMBER,
        NULL,
        OBJECT,
        STRING
    }
    
}
