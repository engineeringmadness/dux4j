package org.flux.store.tests.v1;

import org.flux.store.api.v2.Middleware;
import org.flux.store.api.v1.Reducer;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.CounterState;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency tests for {@link DuxStore}.
 *
 * <p>These tests verify that the store can accept actions from multiple threads
 * simultaneously and still process them as if they arrived on a single sequential
 * queue. They are intentionally written against the current (non-thread-safe)
 * implementation so that they fail until proper serialization is added to
 * {@code DuxStore.dispatch}.</p>
 */
public class ConcurrencyTest {

    public static final String ACTION_INCREMENT = "INCREMENT";
    public static final String ACTION_ADD = "ADD";

    private DuxStore<CounterState> store;

    @BeforeEach
    public void init() {
        Reducer<CounterState> reducer = (action, state) -> {
            switch (action.getType()) {
                case ACTION_INCREMENT:
                    state.setCount(state.getCount() + 1);
                    break;
                case ACTION_ADD:
                    int delta = (Integer) action.getPayload();
                    state.setCount(state.getCount() + delta);
                    break;
                default:
                    throw new RuntimeException("Action Type not supported by reducer");
            }
            return state;
        };
        store = new DuxStore<>(new CounterState(), reducer);
    }

    /**
     * Many threads dispatching the same INCREMENT action concurrently should
     * produce a final count equal to the total number of dispatches. Any
     * interleaving inside {@code dispatchInternal} that reads a stale state,
     * applies the reduction, and writes the result back will lose updates and
     * cause this test to fail.
     */
    @Test
    public void concurrentIncrementsProduceCorrectTotal() throws InterruptedException {
        int threads = 10;
        int incrementsPerThread = 500;
        int totalIncrements = threads * incrementsPerThread;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        store.dispatch(Utilities.actionCreator(ACTION_INCREMENT, null));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "All dispatch threads should complete");

        assertEquals(totalIncrements, store.getState().getCount(),
                "Final count must equal the total number of dispatched increments");
    }

    /**
     * Every dispatched action should be recorded in the store's action history.
     * If two dispatches interleave while the store updates its history index,
     * actions may be added to the underlying list but not visible through
     * {@code getFullActionHistory}.
     */
    @Test
    public void concurrentDispatchesPreserveActionHistory() throws InterruptedException {
        int threads = 10;
        int actionsPerThread = 50;
        int totalActions = threads * actionsPerThread;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < actionsPerThread; j++) {
                        store.dispatch(Utilities.actionCreator(ACTION_ADD, 1));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "All dispatch threads should complete");

        List<String> history = store.getActionHistory();
        long dispatchedActions = history.stream()
                .filter(ACTION_ADD::equals)
                .count();
        assertEquals(totalActions, dispatchedActions,
                "Action history must contain every dispatched action");
    }

    /**
     * Subscribed listeners must eventually observe every state change produced by
     * concurrent dispatches. Because notifications are currently asynchronous,
     * the test waits for a number of notifications equal to the expected number
     * of state changes before asserting the final state.
     */
    @Test
    public void concurrentDispatchesNotifyListenersConsistently() throws InterruptedException {
        int threads = 10;
        int incrementsPerThread = 100;
        int totalIncrements = threads * incrementsPerThread;

        CountDownLatch notificationLatch = new CountDownLatch(totalIncrements);
        List<Integer> observedCounts = Collections.synchronizedList(new ArrayList<>());

        store.subscribe(state -> {
            observedCounts.add(state.getCount());
            notificationLatch.countDown();
        });

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        store.dispatch(Utilities.actionCreator(ACTION_INCREMENT, null));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "All dispatch threads should complete");
        assertTrue(notificationLatch.await(30, TimeUnit.SECONDS),
                "Listener should be notified for every state change");

        assertEquals(totalIncrements, store.getState().getCount(),
                "Final state observed by listener must match the total increments");
    }

    /**
     * The middleware pipeline must remain correct under concurrent load. Even when
     * middleware simply forwards the action, the final state must reflect every
     * dispatched update; lost updates caused by unsynchronized access to
     * {@code dispatchInternal} will make this test fail.
     */
    @Test
    public void concurrentMiddlewarePipelineProducesCorrectTotal() throws InterruptedException {
        int threads = 10;
        int actionsPerThread = 50;
        int totalActions = threads * actionsPerThread;

        AtomicInteger middlewareInvocationCount = new AtomicInteger(0);
        List<String> observedActionTypes = Collections.synchronizedList(new ArrayList<>());

        Middleware<CounterState> middleware = (store, next, action) -> {
            observedActionTypes.add(action.getType());
            middlewareInvocationCount.incrementAndGet();
            next.accept(action);
        };

        DuxStore<CounterState> storeWithMiddleware = new DuxStore<>(
                new CounterState(),
                (action, state) -> {
                    if (ACTION_ADD.equals(action.getType())) {
                        state.setCount(state.getCount() + (Integer) action.getPayload());
                    }
                    return state;
                },
                middleware
        );

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < actionsPerThread; j++) {
                        storeWithMiddleware.dispatch(Utilities.actionCreator(ACTION_ADD, 1));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "All dispatch threads should complete");

        assertEquals(totalActions, middlewareInvocationCount.get(),
                "Middleware must be invoked once for every dispatched action");
        assertEquals(totalActions, storeWithMiddleware.getState().getCount(),
                "Final count must equal the total number of dispatched actions after middleware");
    }
}
