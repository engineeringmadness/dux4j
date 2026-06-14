package org.flux.store.tests.v3;

import org.flux.store.api.exceptions.InvalidActionException;
import org.flux.store.api.v2.Slice;
import org.flux.store.main.v3.ReflectionDuxSliceBuilder;
import org.flux.store.tests.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionSliceBuilderTest {

    private Slice<UserProfile> slice;

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
    }

    @Test
    public void failUpdateSliceWithInvalidAction() {
        assertThrows(InvalidActionException.class, () -> slice.getAction("updateEmail"));
    }

}
