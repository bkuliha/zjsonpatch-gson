package com.flipkart.zjsonpatch;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * @author isopropylcyanide
 */
public class JsonSplitReplaceOpTest {

	private static Gson OBJECT_MAPPER = new GsonBuilder().create();

    @Test
    public void testJsonDiffSplitsReplaceIntoAddAndRemoveOperationWhenFlagIsAdded() throws Exception {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\", \"F4\" ] }";
        JsonElement sourceNode = OBJECT_MAPPER.fromJson(source, JsonElement.class);
        JsonElement targetNode = OBJECT_MAPPER.fromJson(target, JsonElement.class);

        JsonArray diff = JsonDiff.asJson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(3, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("op")));
        assertEquals("/ids/1", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("path")));
        assertEquals("F3", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("value")));

        assertEquals(Operation.ADD.rfcName(), TestUtils.getTextValue(diff.get(1).getAsJsonObject().get("op")));
        assertEquals("/ids/1", TestUtils.getTextValue(diff.get(1).getAsJsonObject().get("path")));
        assertEquals("F6", TestUtils.getTextValue(diff.get(1).getAsJsonObject().get("value")));

        assertEquals(Operation.ADD.rfcName(), TestUtils.getTextValue(diff.get(2).getAsJsonObject().get("op")));
        assertEquals("/ids/2", TestUtils.getTextValue(diff.get(2).getAsJsonObject().get("path")));
        assertEquals("F4", TestUtils.getTextValue(diff.get(2).getAsJsonObject().get("value")));
    }

    @Test
    public void testJsonDiffDoesNotSplitReplaceIntoAddAndRemoveOperationWhenFlagIsNotAdded() throws Exception {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\", \"F4\" ] }";
        JsonElement sourceNode = OBJECT_MAPPER.fromJson(source, JsonElement.class);
        JsonElement targetNode = OBJECT_MAPPER.fromJson(target, JsonElement.class);

        JsonArray diff = JsonDiff.asJson(sourceNode, targetNode);
        System.out.println(diff);
        assertEquals(2, diff.size());
        assertEquals(Operation.REPLACE.rfcName(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("op")));
        assertEquals("/ids/1", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("path")));
        assertEquals("F6", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("value")));

        assertEquals(Operation.ADD.rfcName(), TestUtils.getTextValue(diff.get(1).getAsJsonObject().get("op")));
        assertEquals("/ids/2", TestUtils.getTextValue(diff.get(1).getAsJsonObject().get("path")));
        assertEquals("F4", TestUtils.getTextValue(diff.get(1).getAsJsonObject().get("value")));
    }

    @Test
    public void testJsonDiffDoesNotSplitsWhenThereIsNoReplaceOperationButOnlyRemove() throws Exception {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F3\"] }";

        JsonElement sourceNode = OBJECT_MAPPER.fromJson(source, JsonElement.class);
        JsonElement targetNode = OBJECT_MAPPER.fromJson(target, JsonElement.class);

        JsonArray diff = JsonDiff.asJson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(1, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("op")));
        assertEquals("/ids/0", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("path")));
        assertEquals("F1", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("value")));
    }

    @Test
    public void testJsonDiffDoesNotSplitsWhenThereIsNoReplaceOperationButOnlyAdd() throws Exception {
        String source = "{ \"ids\": [ \"F1\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\"] }";

        JsonElement sourceNode = OBJECT_MAPPER.fromJson(source, JsonElement.class);
        JsonElement targetNode = OBJECT_MAPPER.fromJson(target, JsonElement.class);

        JsonArray diff = JsonDiff.asJson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(1, diff.size());
        assertEquals(Operation.ADD.rfcName(), TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("op")));
        assertEquals("/ids/1", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("path")));
        assertEquals("F6", TestUtils.getTextValue(diff.get(0).getAsJsonObject().get("value")));
    }
}
