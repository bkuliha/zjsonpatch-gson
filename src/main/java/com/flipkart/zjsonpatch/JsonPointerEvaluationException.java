package com.flipkart.zjsonpatch;

import com.google.gson.JsonElement;

public class JsonPointerEvaluationException extends Exception {
    private final JsonPointer path;
    private final JsonElement target;

    public JsonPointerEvaluationException(String message, JsonPointer path, JsonElement target) {
        super(message);
        this.path = path;
        this.target = target;
    }

    public JsonPointer getPath() {
        return path;
    }

    public JsonElement getTarget() {
        return target;
    }
}
