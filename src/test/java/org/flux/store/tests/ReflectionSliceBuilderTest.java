package org.flux.store.tests;

import org.flux.store.api.InvalidActionException;
import org.flux.store.api.Slice;
import org.flux.store.main.DuxSliceBuilder;
import org.flux.store.main.ReflectionDuxSliceBuilder;
import org.flux.store.tests.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionSliceBuilderTest {

    private Slice<UserProfile> slice;

    private boolean sampleState;

    @BeforeEach
    public void init() {
        this.slice = new ReflectionDuxSliceBuilder<UserProfile>()
                .setInitialState(new UserProfile("Karan Gupta", "karan@hello.com"))
                .setStoreName("MyStore")
                .setBasePackage("org.flux.store.tests.reflection")
                .build();
    }
    @Test
    public void canUpdateSliceWithValidAction() throws InvalidActionException {
        String newName = "Manoj Gupta";
        String newEmail = "manoj@hello.com";
        Consumer setEmail = slice.getAction("setEmail");
        Consumer setName = slice.getAction("setName");
        setName.accept(newName);
        setEmail.accept(newEmail);
        assertEquals(newName, slice.getState().getName());
        assertEquals(newEmail, slice.getState().getEmail());
        assertFalse(sampleState);
    }

    @Test
    public void failUpdateSliceWithInvalidAction() {
        assertThrows(InvalidActionException.class, () -> slice.getAction("updateEmail"));
    }

}
