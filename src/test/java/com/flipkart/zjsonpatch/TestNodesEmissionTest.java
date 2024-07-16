package com.flipkart.zjsonpatch;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.EnumSet;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class TestNodesEmissionTest {

    private static Gson mapper = new GsonBuilder().create();

    private static EnumSet<DiffFlags> flags;

    static {
        flags = DiffFlags.defaults();
        flags.add(DiffFlags.EMIT_TEST_OPERATIONS);
    }

    @Test
    public void testNodeEmittedBeforeReplaceOperation() throws IOException {
        JsonElement source = mapper.fromJson("{\"key\":\"original\"}", JsonElement.class);
        JsonElement target = mapper.fromJson("{\"key\":\"replaced\"}", JsonElement.class);

        JsonElement diff = JsonDiff.asJson(source, target, flags);

        JsonElement testNode = mapper.fromJson("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}", JsonElement.class);
        assertEquals(2, diff.getAsJsonArray().size());
        assertEquals(testNode, diff.getAsJsonArray().iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeCopyOperation() throws IOException {
        JsonElement source = mapper.fromJson("{\"key\":\"original\"}", JsonElement.class);
        JsonElement target = mapper.fromJson("{\"key\":\"original\", \"copied\":\"original\"}", JsonElement.class);

        JsonElement diff = JsonDiff.asJson(source, target, flags);

        JsonElement testNode = mapper.fromJson("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}", JsonElement.class);
        assertEquals(2, diff.getAsJsonArray().size());
        assertEquals(testNode, diff.getAsJsonArray().iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeMoveOperation() throws IOException {
        JsonElement source = mapper.fromJson("{\"key\":\"original\"}", JsonElement.class);
        JsonElement target = mapper.fromJson("{\"moved\":\"original\"}", JsonElement.class);

        JsonElement diff = JsonDiff.asJson(source, target, flags);

        JsonElement testNode = mapper.fromJson("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}", JsonElement.class);
        assertEquals(2, diff.getAsJsonArray().size());
        assertEquals(testNode, diff.getAsJsonArray().iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveOperation() throws IOException {
        JsonElement source = mapper.fromJson("{\"key\":\"original\"}", JsonElement.class);
        JsonElement target = mapper.fromJson("{}", JsonElement.class);

        JsonElement diff = JsonDiff.asJson(source, target, flags);

        JsonElement testNode = mapper.fromJson("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}", JsonElement.class);
        assertEquals(2, diff.getAsJsonArray().size());
        assertEquals(testNode, diff.getAsJsonArray().iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveFromMiddleOfArray() throws IOException {
        JsonElement source = mapper.fromJson("{\"key\":[1,2,3]}", JsonElement.class);
        JsonElement target = mapper.fromJson("{\"key\":[1,3]}", JsonElement.class);

        JsonElement diff = JsonDiff.asJson(source, target, flags);

        JsonElement testNode = mapper.fromJson("{\"op\":\"test\",\"path\":\"/key/1\",\"value\":2}", JsonElement.class);
        assertEquals(2, diff.getAsJsonArray().size());
        assertEquals(testNode, diff.getAsJsonArray().iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveFromTailOfArray() throws IOException {
        JsonElement source = mapper.fromJson("{\"key\":[1,2,3]}", JsonElement.class);
        JsonElement target = mapper.fromJson("{\"key\":[1,2]}", JsonElement.class);

        JsonElement diff = JsonDiff.asJson(source, target, flags);

        JsonElement testNode = mapper.fromJson("{\"op\":\"test\",\"path\":\"/key/2\",\"value\":3}", JsonElement.class);
        assertEquals(2, diff.getAsJsonArray().size());
        assertEquals(testNode, diff.getAsJsonArray().iterator().next());
    }
}
