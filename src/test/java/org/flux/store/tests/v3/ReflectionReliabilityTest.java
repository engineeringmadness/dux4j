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
                .setStoreName("MyStore")
                .setBasePackage(BASE_PACKAGE)
                .build();

        // DifferentStoreReducer has storeName "DifferentStore" and should be ignored.
        assertThrows(InvalidActionException.class, () -> slice.getAction("differentAction"));
        assertDoesNotThrow(() -> slice.getAction("setName"));
    }

    @Test
    public void missingStoreNameMatchesNothing() {
        Slice<UserProfile> slice = new ReflectionDuxSliceBuilder<UserProfile>()
                .setInitialState(new UserProfile("Karan", "karan@hello.com"))
                .setBasePackage(BASE_PACKAGE)
                .build();

        assertThrows(InvalidActionException.class, () -> slice.getAction("setName"));
    }

    @Test
    public void missingBasePackageThrows() {
        assertThrows(RuntimeException.class, () ->
                new ReflectionDuxSliceBuilder<UserProfile>()
                        .setInitialState(new UserProfile("Karan", "karan@hello.com"))
                        .setStoreName("MyStore")
                        .build()
        );
    }

    @Test
    public void duplicateReducerTypesAreResolved() throws InvalidActionException {
        // Both SetNameReducer and DuplicateNameReducer are annotated with MyStore and handle "setName".
        // The slice must still be usable; whichever reducer wins, the action should be dispatchable.
        Slice<UserProfile> slice = new ReflectionDuxSliceBuilder<UserProfile>()
                .setInitialState(new UserProfile("Karan", "karan@hello.com"))
                .setStoreName("MyStore")
                .setBasePackage(BASE_PACKAGE)
                .build();

        Consumer setName = slice.getAction("setName");
        setName.accept("Alice");
        String result = slice.getState().getName();
        assertTrue("Alice".equals(result) || "ALICE".equals(result),
                "One of the duplicate reducers should have won");
    }
}
