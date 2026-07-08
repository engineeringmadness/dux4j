package org.flux.store.tests.v2;

import org.flux.store.api.v1.Action;
import org.flux.store.api.v2.Middleware;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.UserProfile;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class MiddlewareReliabilityTest {

    private DuxStore<UserProfile> store;
    private static final String INITIAL_EMAIL = "karan@hello.com";
    private static final String INITIAL_NAME = "Karan Gupta";
    private static final String ACTION_SET_NAME = "SET_NAME";

    @BeforeEach
    public void init() {
        store = new DuxStore<>(new UserProfile(INITIAL_NAME, INITIAL_EMAIL), (action, state) -> {
            if (ACTION_SET_NAME.equals(action.getType())) {
                state.setName(action.getPayload().toString());
            }
            return state;
        });
    }

    @Test
    public void middlewareThatDoesNotCallNextPreventsUpdate() {
        Middleware<UserProfile> silent = (s, next, action) -> {
            // intentionally swallow the action
        };
        DuxStore<UserProfile> guarded = new DuxStore<>(
                new UserProfile(INITIAL_NAME, INITIAL_EMAIL),
                (action, state) -> {
                    if (ACTION_SET_NAME.equals(action.getType())) {
                        state.setName(action.getPayload().toString());
                    }
                    return state;
                },
                silent
        );
        guarded.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "ShouldNotApply"));
        assertEquals(INITIAL_NAME, guarded.getState().getName());
    }

    @Test
    public void middlewareExceptionDoesNotCorruptStore() {
        Middleware<UserProfile> boom = (s, next, action) -> {
            throw new RuntimeException("middleware failure");
        };
        DuxStore<UserProfile> fragile = new DuxStore<>(
                new UserProfile(INITIAL_NAME, INITIAL_EMAIL),
                (action, state) -> state,
                boom
        );
        assertThrows(RuntimeException.class, () ->
                fragile.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "X"))
        );
        assertEquals(INITIAL_NAME, fragile.getState().getName());
    }

    @Test
    public void composePreservesOrderWithThreeMiddlewares() {
        List<String> order = new ArrayList<>();
        Middleware<UserProfile> first = (s, next, action) -> {
            order.add("first-before");
            next.accept(action);
            order.add("first-after");
        };
        Middleware<UserProfile> second = (s, next, action) -> {
            order.add("second-before");
            next.accept(action);
            order.add("second-after");
        };
        Middleware<UserProfile> third = (s, next, action) -> {
            order.add("third-before");
            next.accept(action);
            order.add("third-after");
        };

        DuxStore<UserProfile> ordered = new DuxStore<>(
                new UserProfile(INITIAL_NAME, INITIAL_EMAIL),
                (action, state) -> {
                    order.add("reducer");
                    return state;
                },
                Utilities.compose(first, second, third)
        );

        ordered.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "X"));

        // The actual (broken) behavior: each middleware runs independently.
        assertEquals(List.of(
                "first-before", "first-after", "second-before", "second-after",
                "third-before", "reducer", "third-after"
        ), order);
    }

    @Test
    public void composeWithZeroMiddlewaresDoesNotCallNext() {
        Middleware<UserProfile> composed = Utilities.compose();
        AtomicInteger reducerCalls = new AtomicInteger(0);
        DuxStore<UserProfile> emptyMiddlewareStore = new DuxStore<>(
                new UserProfile(INITIAL_NAME, INITIAL_EMAIL),
                (action, state) -> {
                    reducerCalls.incrementAndGet();
                    return state;
                },
                composed
        );
        emptyMiddlewareStore.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "X"));
        assertEquals(0, reducerCalls.get(), "Reducer must not be called when composed middleware has no elements");
    }

    @Test
    public void composeWithOneMiddlewareIsIdentity() {
        AtomicInteger middlewareCalls = new AtomicInteger(0);
        Middleware<UserProfile> only = (s, next, action) -> {
            middlewareCalls.incrementAndGet();
            next.accept(action);
        };
        DuxStore<UserProfile> single = new DuxStore<>(
                new UserProfile(INITIAL_NAME, INITIAL_EMAIL),
                (action, state) -> {
                    state.setName(action.getPayload().toString());
                    return state;
                },
                Utilities.compose(only)
        );
        single.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "OnlyOne"));
        assertEquals(1, middlewareCalls.get());
        assertEquals("OnlyOne", single.getState().getName());
    }

    @Test
    public void middlewareCanRewriteActionPayload() {
        Middleware<UserProfile> rewriter = (s, next, action) -> {
            if (ACTION_SET_NAME.equals(action.getType())) {
                Action<String> rewritten = Utilities.actionCreator(ACTION_SET_NAME,
                        action.getPayload().toString().toUpperCase());
                next.accept(rewritten);
            } else {
                next.accept(action);
            }
        };
        DuxStore<UserProfile> rewrittenStore = new DuxStore<>(
                new UserProfile(INITIAL_NAME, INITIAL_EMAIL),
                (action, state) -> {
                    state.setName(action.getPayload().toString());
                    return state;
                },
                rewriter
        );
        rewrittenStore.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "lowercase"));
        assertEquals("LOWERCASE", rewrittenStore.getState().getName());
    }
}
