package org.flux.store.tests.perf;

import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.CounterState;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("perf")
public class NotificationLatencyTest {

    private static final String ACTION_INCREMENT = "INCREMENT";
    private static final int ITERATIONS = 200;
    private static final long MAX_ALLOWED_LATENCY_NANOS = TimeUnit.MILLISECONDS.toNanos(15);
    private static final long AVERAGE_ALLOWED_LATENCY_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    @Test
    public void asyncNotificationLatency() throws InterruptedException {
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        DuxStore<CounterState> store = new DuxStore<>(new CounterState(), (action, state) -> {
            if (ACTION_INCREMENT.equals(action.getType())) {
                state.setCount(state.getCount() + 1);
            }
            return state;
        });

        CountDownLatch[] latches = new CountDownLatch[ITERATIONS];
        long[] dispatchTimes = new long[ITERATIONS];

        store.subscribe(state -> {
            int count = state.getCount();
            if (count > 0 && count <= ITERATIONS) {
                long latency = System.nanoTime() - dispatchTimes[count - 1];
                latencies.add(latency);
                latches[count - 1].countDown();
            }
        });

        for (int i = 0; i < ITERATIONS; i++) {
            latches[i] = new CountDownLatch(1);
            dispatchTimes[i] = System.nanoTime();
            store.dispatch(Utilities.actionCreator(ACTION_INCREMENT, null));
            latches[i].await(1, TimeUnit.SECONDS);
        }

        assertTrue(latencies.size() >= ITERATIONS,
                "Expected at least " + ITERATIONS + " latency samples, got " + latencies.size());

        long max = latencies.stream().mapToLong(Long::longValue).max().orElse(0);
        double average = latencies.stream().mapToLong(Long::longValue).average().orElse(0);

        System.out.printf("Notification latency: avg=%.3f ms, max=%.3f ms%n",
                average / 1_000_000.0, max / 1_000_000.0);

        assertTrue(max <= MAX_ALLOWED_LATENCY_NANOS,
                String.format("Max latency %.3f ms exceeds %.3f ms",
                        max / 1_000_000.0, MAX_ALLOWED_LATENCY_NANOS / 1_000_000.0));
        assertTrue(average <= AVERAGE_ALLOWED_LATENCY_NANOS,
                String.format("Average latency %.3f ms exceeds %.3f ms",
                        average / 1_000_000.0, AVERAGE_ALLOWED_LATENCY_NANOS / 1_000_000.0));
    }
}
