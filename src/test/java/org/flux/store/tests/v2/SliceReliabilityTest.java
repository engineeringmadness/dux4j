package org.flux.store.tests.v2;

import org.flux.store.api.exceptions.InvalidActionException;
import org.flux.store.api.v2.Middleware;
import org.flux.store.api.v2.Slice;
import org.flux.store.main.v2.DuxSliceBuilder;
import org.flux.store.tests.domain.UserProfile;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class SliceReliabilityTest {

    private Slice<UserProfile> slice;
    private final AtomicInteger notificationCount = new AtomicInteger(0);

    @BeforeEach
    public void init() {
        notificationCount.set(0);
        slice = new DuxSliceBuilder<UserProfile>()
                .setInitialState(new UserProfile("Karan", "karan@hello.com"))
                .addReducer("setName", (action, state) -> {
                    state.setName(action.getPayload().toString());
                    return state;
                })
                .addReducer("setEmail", (action, state) -> {
                    state.setEmail(action.getPayload().toString());
                    return state;
                })
                .addSubscriber(state -> notificationCount.incrementAndGet())
                .build();
    }

    @Test
    public void canTimeTravelBackAndForwardThroughSlice() throws InvalidActionException {
        Consumer setName = slice.getAction("setName");
        setName.accept("Alice");
        setName.accept("Bob");
        setName.accept("Carol");

        slice.goBack();
        slice.goBack();
        assertEquals("Alice", slice.getState().getName());

        slice.goForward();
        assertEquals("Bob", slice.getState().getName());
    }

    @Test
    public void sliceWithMiddlewareTransformsAction() throws InvalidActionException {
        Middleware<UserProfile> middleware = (store, next, action) -> {
            if ("setName".equals(action.getType()) && "bad".equals(action.getPayload())) {
                next.accept(Utilities.actionCreator(action.getType(), "good"));
            } else {
                next.accept(action);
            }
        };
        Slice<UserProfile> sliceWithMiddleware = new DuxSliceBuilder<UserProfile>()
                .setInitialState(new UserProfile("Karan", "karan@hello.com"))
                .addReducer("setName", (action, state) -> {
                    state.setName(action.getPayload().toString());
                    return state;
                })
                .setMiddleware(middleware)
                .build();

        sliceWithMiddleware.getAction("setName").accept("bad");
        assertEquals("good", sliceWithMiddleware.getState().getName());
    }

    @Test
    public void invalidActionStillThrows() {
        assertThrows(InvalidActionException.class, () -> slice.getAction("missingAction"));
    }

    @Test
    public void consumerCanBeReused() throws InvalidActionException {
        Consumer setName = slice.getAction("setName");
        setName.accept("A");
        setName.accept("B");
        assertEquals("B", slice.getState().getName());
    }

    @Test
    public void noChangeDispatchDoesNotNotifySliceSubscriber() throws InterruptedException, InvalidActionException {
        slice.getAction("setName").accept("Karan");
        Thread.sleep(100);
        assertEquals(0, notificationCount.get());
    }
}
