package org.flux.store.tests.perf;

import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.CounterState;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DispatchThroughputTest {

    private static final String ACTION_INCREMENT = "INCREMENT";
    private static final int WARMUP_ACTIONS = 10_000;
    private static final int MEASURED_ACTIONS = 100_000;
    private static final long MINIMUM_THROUGHPUT_OPS_PER_SECOND = 290_000;

    private DuxStore<CounterState> store;

    @BeforeEach
    public void init() {
        store = new DuxStore<>(new CounterState(), (action, state) -> {
            if (ACTION_INCREMENT.equals(action.getType())) {
                state.setCount(state.getCount() + 1);
            }
            return state;
        });
    }

    @Test
    public void dispatchThroughput() {
        // Warm up the JVM and internal structures.
        for (int i = 0; i < WARMUP_ACTIONS; i++) {
            store.dispatch(Utilities.actionCreator(ACTION_INCREMENT, null));
        }
        store = new DuxStore<>(new CounterState(), (action, state) -> {
            if (ACTION_INCREMENT.equals(action.getType())) {
                state.setCount(state.getCount() + 1);
            }
            return state;
        });

        long start = System.nanoTime();
        for (int i = 0; i < MEASURED_ACTIONS; i++) {
            store.dispatch(Utilities.actionCreator(ACTION_INCREMENT, null));
        }
        long elapsedNanos = System.nanoTime() - start;
        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
        double throughput = MEASURED_ACTIONS / elapsedSeconds;

        System.out.printf("Dispatched %d actions in %.3f s (%.0f ops/s)%n",
                MEASURED_ACTIONS, elapsedSeconds, throughput);

        assertTrue(throughput >= MINIMUM_THROUGHPUT_OPS_PER_SECOND,
                String.format("Throughput %.0f ops/s is below minimum %.0f ops/s",
                        throughput, (double) MINIMUM_THROUGHPUT_OPS_PER_SECOND));
    }
}
