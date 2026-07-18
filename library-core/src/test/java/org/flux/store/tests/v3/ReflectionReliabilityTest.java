package org.flux.store.tests.v3;

import org.flux.store.api.exceptions.InvalidActionException;
import org.flux.store.api.v2.Slice;
import org.flux.store.main.v3.ReflectionDuxSliceBuilder;
import org.flux.store.tests.domain.UserProfile;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionReliabilityTest {

    private static final String BASE_PACKAGE = "org.flux.store.tests.reflection";

    @Test
    public void reducersForOtherStoreAreIgnored() {
        Slice<UserProfile> slice = new ReflectionDuxSliceBuilder<UserProfile>()
                .setInitialState(new UserProfile("Karan", "karan@hello.com"))
                .setBasePackage(BASE_PACKAGE)
                .build();

        // DifferentStoreReducer has storeName "DifferentStore" and should be ignored.
        assertThrows(InvalidActionException.class, () -> slice.getAction("differentAction"));
        assertDoesNotThrow(() -> slice.getAction("setName"));
    }

    @Test
    public void missingBasePackageThrows() {
        assertThrows(RuntimeException.class, () ->
                new ReflectionDuxSliceBuilder<UserProfile>()
                        .setInitialState(new UserProfile("Karan", "karan@hello.com"))
                        .build()
        );
    }
}
