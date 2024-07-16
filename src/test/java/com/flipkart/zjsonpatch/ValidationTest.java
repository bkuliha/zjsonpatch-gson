package com.flipkart.zjsonpatch;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.Strictness;

public class ValidationTest {
    // the test cases have comments in them to explain the specific test case
    // hence use a custom Object mapper that allows Json comments
    private static final Gson MAPPER = new GsonBuilder()
            .setStrictness(Strictness.LENIENT) // Allow non-JSON elements like comments
            .create();

    @ParameterizedTest
    @MethodSource("argsForValidationTest")
    public void testValidation(String patch) {
        assertThrows(
            InvalidJsonPatchException.class,
            () -> JsonPatch.validate(MAPPER.fromJson(patch, JsonElement.class))
        );
    }

    public static Stream<Arguments> argsForValidationTest() throws IOException {
        JsonElement patches = MAPPER.fromJson(TestUtils.loadFromResources("/testdata/invalid-patches.json"), JsonElement.class);

        List<Arguments> args = new ArrayList<>();

        for (JsonElement patch : patches.getAsJsonArray().asList()) {
            args.add(Arguments.of(patch.toString()));
        }

        return args.stream();
    }
}
