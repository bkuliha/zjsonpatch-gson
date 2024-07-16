package com.flipkart.zjsonpatch;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class TestUtils {

    public static final Gson DEFAULT_MAPPER = new GsonBuilder().create();

    private TestUtils() {
    }

    public static JsonElement loadResourceAsJsonNode(String path) throws IOException {
        String testData = loadFromResources(path);
        return DEFAULT_MAPPER.fromJson(testData, JsonElement.class);
    }

    public static String loadFromResources(String path) throws IOException {
        InputStream resourceAsStream = PatchTestCase.class.getResourceAsStream(path);
        return IOUtils.toString(resourceAsStream, "UTF-8");
    }
    
    public static String getTextValue(JsonElement jsonElement) {
    	if (jsonElement.isJsonPrimitive()) {
    		if (jsonElement.getAsJsonPrimitive().isString()) {
    			return jsonElement.getAsJsonPrimitive().getAsString();
    		}
    	}
    	return null;
    }
    
}
