package org.flux.store.tests.v1;

import org.flux.store.api.v1.Action;
import org.flux.store.api.v1.Reducer;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.UserProfile;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class StoreReliabilityTest {

    public static final String INITIAL_EMAIL = "karan@hello.com";
    public static final String INITIAL_NAME = "Karan Gupta";
    public static final String ACTION_SET_EMAIL = "SET_EMAIL";
    public static final String ACTION_SET_NAME = "SET_NAME";

    private DuxStore<UserProfile> store;

    @BeforeEach
    public void init() {
        UserProfile initialState = new UserProfile(INITIAL_NAME, INITIAL_EMAIL);
        store = new DuxStore<>(initialState, this::reduce);
    }

    private UserProfile reduce(Action action, UserProfile state) {
        switch (action.getType()) {
            case ACTION_SET_EMAIL:
                state.setEmail(action.getPayload().toString());
                break;
            case ACTION_SET_NAME:
                state.setName(action.getPayload().toString());
                break;
            default:
                throw new RuntimeException("Action Type not supported by reducer");
        }
        return state;
    }

    @Test
    public void dispatchingNoChangeActionDoesNotNotifyListeners() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        store.subscribe(state -> count.incrementAndGet());

        store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, INITIAL_NAME));

        // Give any rogue async notification a chance to fire.
        Thread.sleep(100);
        assertEquals(0, count.get(), "Listener should not be called when state does not change");
    }

    @Test
    public void unknownActionPropagatesReducerException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                store.dispatch(Utilities.actionCreator("UNKNOWN_ACTION", "x"))
        );
        assertEquals("Action Type not supported by reducer", ex.getMessage());
    }

    @Test
    public void replaceReducerActuallyChangesBehavior() {
        Reducer<UserProfile> newReducer = (action, state) -> {
            if (ACTION_SET_NAME.equals(action.getType())) {
                state.setName(action.getPayload().toString() + "-replaced");
            }
            return state;
        };
        store.replaceReducer(newReducer);
        store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "Alice"));
        assertEquals("Alice-replaced", store.getState().getName());
    }

    @Test
    public void getStateReturnsLiveReference() {
        UserProfile live = store.getState();
        live.setName("Mutated");
        assertEquals("Mutated", store.getState().getName());
    }

    @Test
    public void multipleSubscribersAreAllNotified() throws InterruptedException {
        int subscriberCount = 5;
        CountDownLatch latch = new CountDownLatch(subscriberCount);
        for (int i = 0; i < subscriberCount; i++) {
            store.subscribe(state -> latch.countDown());
        }
        store.dispatch(Utilities.actionCreator(ACTION_SET_EMAIL, "a@b.com"));
        assertTrue(latch.await(2, TimeUnit.SECONDS), "All subscribers should be notified");
    }

    @Test
    public void throwingSubscriberDoesNotPreventOtherNotifications() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        store.subscribe(state -> { throw new RuntimeException("boom"); });
        store.subscribe(state -> latch.countDown());

        assertDoesNotThrow(() -> store.dispatch(Utilities.actionCreator(ACTION_SET_EMAIL, "safe@example.com")));
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Non-throwing subscriber should still be notified");
    }

    @Test
    public void actionWithNullPayloadThrowsInReducer() {
        assertThrows(NullPointerException.class, () ->
                store.dispatch(new Action<String>(ACTION_SET_NAME, null))
        );
    }
}
