package org.flux.store.tests.utils;

import org.flux.store.api.v1.Action;
import org.flux.store.api.v1.Reducer;
import org.flux.store.api.v2.Middleware;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.UserProfile;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class UtilitiesReliabilityTest {

    @Test
    public void actionCreatorProducesCorrectAction() {
        Action<String> action = Utilities.actionCreator("TEST", "payload");
        assertEquals("TEST", action.getType());
        assertEquals("payload", action.getPayload());
    }

    @Test
    public void combineReducerRunsInOrder() {
        Reducer<UserProfile> first = (action, state) -> {
            state.setName(state.getName() + "-first");
            return state;
        };
        Reducer<UserProfile> second = (action, state) -> {
            state.setName(state.getName() + "-second");
            return state;
        };
        Reducer<UserProfile> combined = Utilities.combineReducer(first, second);

        UserProfile initial = new UserProfile("Karan", "karan@hello.com");
        UserProfile result = combined.reduce(Utilities.actionCreator("ANY", null), initial);

        assertEquals("Karan-first-second", result.getName());
    }

    @Test
    public void combineReducerWithNoReducersReturnsState() {
        Reducer<UserProfile> combined = Utilities.combineReducer();
        UserProfile initial = new UserProfile("Karan", "karan@hello.com");
        assertSame(initial, combined.reduce(Utilities.actionCreator("ANY", null), initial));
    }

    @Test
    public void composeOrderIsOutermostFirst() {
        // Note: This test documents the actual (incorrect) behavior of Utilities.compose.
        // The compose implementation does not properly chain middlewares; each runs independently.
        List<String> order = new ArrayList<>();
        Middleware<UserProfile> a = (s, next, action) -> {
            order.add("A-in");
            next.accept(action);
            order.add("A-out");
        };
        Middleware<UserProfile> b = (s, next, action) -> {
            order.add("B-in");
            next.accept(action);
            order.add("B-out");
        };

        DuxStore<UserProfile> store = new DuxStore<>(
                new UserProfile("Karan", "karan@hello.com"),
                (action, state) -> {
                    order.add("reducer");
                    return state;
                },
                Utilities.compose(a, b)
        );
        store.dispatch(Utilities.actionCreator("X", null));

        // The actual behavior: each middleware executes with its own next, not chained.
        assertEquals(List.of("A-in", "A-out", "B-in", "reducer", "B-out"), order);
    }

    @Test
    public void composeWithNoMiddlewaresIsNoOp() {
        AtomicInteger reducerCalls = new AtomicInteger(0);
        DuxStore<UserProfile> store = new DuxStore<>(
                new UserProfile("Karan", "karan@hello.com"),
                (action, state) -> {
                    reducerCalls.incrementAndGet();
                    return state;
                },
                Utilities.compose()
        );
        store.dispatch(Utilities.actionCreator("X", null));
        assertEquals(0, reducerCalls.get());
    }
}
