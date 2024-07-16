package com.flipkart.zjsonpatch;

import java.util.EnumSet;

import com.google.gson.JsonElement;

class CopyingApplyProcessor extends InPlaceApplyProcessor {

    CopyingApplyProcessor(JsonElement target) {
        this(target, CompatibilityFlags.defaults());
    }

    CopyingApplyProcessor(JsonElement target, EnumSet<CompatibilityFlags> flags) {
        super(target.deepCopy(), flags);
    }
}
