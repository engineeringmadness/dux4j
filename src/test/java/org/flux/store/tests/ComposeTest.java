package org.flux.store.tests;

import com.github.javafaker.Faker;
import org.flux.store.api.Middleware;
import org.flux.store.api.Reducer;
import org.flux.store.main.DuxStore;
import org.flux.store.utils.Utilities;
import org.flux.store.tests.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ComposeTest {

    public static final String INITIAL_EMAIL = "karan@hello.com";
    public static final String INITIAL_NAME = "Karan Gupta";
    public static final String ACTION_SET_NAME = "SET_NAME";

    private DuxStore<UserProfile> myStore;

    @BeforeEach
    public void init() {
        UserProfile initialState = new UserProfile(INITIAL_NAME, INITIAL_EMAIL);

        Reducer<UserProfile> mockReducer = (action, state) -> {
            if(action.getType().equalsIgnoreCase(ACTION_SET_NAME)) {
                Faker faker = new Faker();
                state.setName(faker.name().fullName());
            }
            return state;
        };

        Middleware<UserProfile> middleware1 = (store, next, action) -> {
            System.out.println("Old State: " + store.getState());
            next.accept(action);
        };

        Middleware<UserProfile> middleware2 = (store, next, action) -> {
            next.accept(action);
            System.out.println("New State: " + store.getState());
        };

        Middleware<UserProfile> combined = Utilities.compose(middleware1, middleware2);
        myStore = new DuxStore<>(initialState, mockReducer, combined);

    }

    @Test
    public void canCombineMiddlewareSoDispatchOnlyRunsOnce() {
        AtomicInteger ordinal = new AtomicInteger(0);
        myStore.subscribe((x) -> ordinal.set(ordinal.incrementAndGet()));
        myStore.dispatch(Utilities.actionCreator(ACTION_SET_NAME, INITIAL_NAME));
        assertNotEquals(INITIAL_NAME, myStore.getState().getName());
        assertEquals(1, ordinal.get());
    }


}
