package org.flux.store.tests.perf;

import org.flux.store.api.v2.Middleware;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.CounterState;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("perf")
public class MiddlewareOverheadTest {

    private static final String ACTION_INCREMENT = "INCREMENT";
    private static final int ACTION_COUNT = 50_000;
    private static final long MAX_ALLOWED_TIME_NANOS = TimeUnit.SECONDS.toNanos(1);

    private DuxStore<CounterState> createStore(Middleware<CounterState> middleware) {
        return new DuxStore<>(new CounterState(), (action, state) -> {
            if (ACTION_INCREMENT.equals(action.getType())) {
                state.setCount(state.getCount() + 1);
            }
            return state;
        }, middleware);
    }

    private long measureDispatches(DuxStore<CounterState> store, int count) {
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            store.dispatch(Utilities.actionCreator(ACTION_INCREMENT, null));
        }
        return System.nanoTime() - start;
    }

    @Test
    public void noMiddlewareBaseline() {
        DuxStore<CounterState> store = createStore(null);
        long elapsed = measureDispatches(store, ACTION_COUNT);
        System.out.printf("No middleware: %d dispatches in %.3f s%n",
                ACTION_COUNT, elapsed / 1_000_000_000.0);
        assertTrue(elapsed <= MAX_ALLOWED_TIME_NANOS);
    }

    @Test
    public void singleMiddlewareOverhead() {
        Middleware<CounterState> middleware = (s, next, action) -> next.accept(action);
        DuxStore<CounterState> store = createStore(middleware);
        long elapsed = measureDispatches(store, ACTION_COUNT);
        System.out.printf("Single middleware: %d dispatches in %.3f s%n",
                ACTION_COUNT, elapsed / 1_000_000_000.0);
        assertTrue(elapsed <= MAX_ALLOWED_TIME_NANOS);
    }

    @Test
    public void composedMiddlewareOverhead() {
        AtomicInteger counter = new AtomicInteger(0);
        Middleware<CounterState> m1 = (s, next, action) -> { counter.incrementAndGet(); next.accept(action); };
        Middleware<CounterState> m2 = (s, next, action) -> { counter.incrementAndGet(); next.accept(action); };
        Middleware<CounterState> m3 = (s, next, action) -> { counter.incrementAndGet(); next.accept(action); };

        DuxStore<CounterState> store = createStore(Utilities.compose(m1, m2, m3));
        long elapsed = measureDispatches(store, ACTION_COUNT);
        System.out.printf("Composed middleware (3x): %d dispatches in %.3f s%n",
                ACTION_COUNT, elapsed / 1_000_000_000.0);
        assertTrue(elapsed <= MAX_ALLOWED_TIME_NANOS);
        assertEquals(ACTION_COUNT * 3, counter.get());
    }

    private void assertEquals(int expected, int actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
