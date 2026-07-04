package org.flux.store.tests.v1;

import org.flux.store.api.v1.Action;
import org.flux.store.api.v1.Reducer;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.UserProfile;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TimeTravelReliabilityTest {

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
        }
        return state;
    }

    @Test
    public void goBackAtStartIsNoOp() {
        UserProfile before = store.getState();
        store.goBack();
        assertSame(before, store.getState());
        List<String> history = store.getActionHistory();
        assertEquals(1, history.size());
        assertEquals(Utilities.INITIAL_ACTION, history.get(0));
    }

    @Test
    public void goForwardAtEndDoesNotDoubleApply() {
        store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "Alice"));
        store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "Bob"));

        UserProfile before = store.getState();
        int historySize = store.getActionHistory().size();

        store.goForward();

        assertEquals(before.getName(), store.getState().getName());
        assertEquals(historySize, store.getActionHistory().size(),
                "History should not grow when going forward at the end");
    }

    @Test
    public void snapshotThresholdBoundaryAtTen() {
        // Dispatch 10 actions so the store records a snapshot at index 10.
        for (int i = 0; i < 10; i++) {
            store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "name-" + i));
        }
        // Going back to index 9 should invalidate the snapshot and force reconstruction.
        store.goBack();
        assertEquals("name-8", store.getState().getName());
    }

    @Test
    public void snapshotThresholdBoundaryAtTwenty() {
        for (int i = 0; i < 20; i++) {
            store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "name-" + i));
        }
        store.goBack();
        assertEquals("name-18", store.getState().getName());
    }

    @Test
    public void mixedBackAndForwardPreservesHistory() {
        store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "A"));
        store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "B"));
        store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "C"));

        store.goBack();
        store.goBack();
        store.goForward();

        assertEquals("B", store.getState().getName());
        // History size reflects current index, not original total after navigation.
        // After 3 dispatches (index=3), 2 goBack (index=1), 1 goForward (index=2),
        // getActionHistory returns subList(0, index+1) = INITIAL + A + B = 3 items.
        assertEquals(3, store.getActionHistory().size());
    }

    @Test
    public void timeTravelReplaysThroughMiddleware() {
        AtomicInteger middlewareCalls = new AtomicInteger(0);
        org.flux.store.api.v2.Middleware<UserProfile> middleware = (s, next, action) -> {
            middlewareCalls.incrementAndGet();
            next.accept(action);
        };
        DuxStore<UserProfile> storeWithMiddleware = new DuxStore<>(
                new UserProfile(INITIAL_NAME, INITIAL_EMAIL),
                this::reduce,
                middleware
        );
        for (int i = 0; i < 12; i++) {
            storeWithMiddleware.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "n" + i));
        }
        int callsBeforeReplay = middlewareCalls.get();
        // Cross the snapshot boundary during replay.
        storeWithMiddleware.goBack();
        storeWithMiddleware.goBack();
        assertTrue(middlewareCalls.get() > callsBeforeReplay,
                "Middleware should be invoked while replaying history");
    }

    @Test
    public void replaceReducerAffectsHistoricalReconstruction() {
        store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "Original"));
        store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "Original2"));

        Reducer<UserProfile> loudReducer = (action, state) -> {
            if (ACTION_SET_NAME.equals(action.getType())) {
                state.setName(action.getPayload().toString().toUpperCase());
            }
            return state;
        };
        store.replaceReducer(loudReducer);
        store.goBack();

        assertEquals("ORIGINAL", store.getState().getName());
    }
}
