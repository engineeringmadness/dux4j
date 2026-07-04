package org.flux.store.tests.perf;

import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.UserProfile;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeTravelPerformanceTest {

    private static final String ACTION_SET_NAME = "SET_NAME";
    private static final int WARMUP_ACTIONS = 5_000;
    private static final int DISPATCH_COUNT = 5_000;
    private static final int TRAVEL_STEPS = 500;
    private static final long MAX_ALLOWED_TIME_NANOS = TimeUnit.MILLISECONDS.toNanos(80);

    private static final BiFunction<org.flux.store.api.v1.Action, UserProfile, UserProfile> REDUCER = (action, state) -> {
        if (ACTION_SET_NAME.equals(action.getType())) {
            state.setName(action.getPayload().toString());
        }
        return state;
    };

    private DuxStore<UserProfile> createStore() {
        return new DuxStore<>(new UserProfile("Karan", "karan@hello.com"), REDUCER::apply);
    }

    @Test
    public void timeTravelWithLargeHistory() {
        // Warm up the JVM so the measurement reflects steady-state performance.
        DuxStore<UserProfile> warmup = createStore();
        for (int i = 0; i < WARMUP_ACTIONS; i++) {
            warmup.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "warm-" + i));
        }
        for (int i = 0; i < TRAVEL_STEPS; i++) {
            warmup.goBack();
        }
        for (int i = 0; i < TRAVEL_STEPS; i++) {
            warmup.goForward();
        }

        DuxStore<UserProfile> store = createStore();

        long dispatchStart = System.nanoTime();
        for (int i = 0; i < DISPATCH_COUNT; i++) {
            store.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "name-" + i));
        }
        long dispatchElapsed = System.nanoTime() - dispatchStart;

        long travelStart = System.nanoTime();
        for (int i = 0; i < TRAVEL_STEPS; i++) {
            store.goBack();
        }
        for (int i = 0; i < TRAVEL_STEPS; i++) {
            store.goForward();
        }
        long travelElapsed = System.nanoTime() - travelStart;
        long totalElapsed = dispatchElapsed + travelElapsed;

        System.out.printf("Time-travel perf: dispatch %d in %.3f s, %d back/forward in %.3f s%n",
                DISPATCH_COUNT, dispatchElapsed / 1_000_000_000.0,
                TRAVEL_STEPS, travelElapsed / 1_000_000_000.0);

        assertEquals("name-" + (DISPATCH_COUNT - 1), store.getState().getName(),
                "Store should end at the latest state after forward travel");
        assertTrue(totalElapsed <= MAX_ALLOWED_TIME_NANOS,
                String.format("Total time %.3f s exceeds %.3f s",
                        totalElapsed / 1_000_000_000.0, MAX_ALLOWED_TIME_NANOS / 1_000_000_000.0));
    }
}
