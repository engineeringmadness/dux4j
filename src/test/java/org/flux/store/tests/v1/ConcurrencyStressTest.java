package org.flux.store.tests.v1;

import org.flux.store.api.v2.Middleware;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.main.v2.DuxSliceBuilder;
import org.flux.store.api.v2.Slice;
import org.flux.store.tests.domain.CounterState;
import org.flux.store.tests.domain.UserProfile;
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

public class ConcurrencyStressTest {

    public static final String ACTION_INCREMENT = "INCREMENT";
    public static final String ACTION_ADD = "ADD";

    private DuxStore<CounterState> store;

    @BeforeEach
    public void init() {
        store = new DuxStore<>(new CounterState(), (action, state) -> {
            switch (action.getType()) {
                case ACTION_INCREMENT:
                    state.setCount(state.getCount() + 1);
                    break;
                case ACTION_ADD:
                    state.setCount(state.getCount() + (Integer) action.getPayload());
                    break;
            }
            return state;
        });
    }

    @Test
    public void highVolumeConcurrentIncrementsAreCorrect() throws InterruptedException {
        int threads = 20;
        int incrementsPerThread = 1000;
        int total = threads * incrementsPerThread;

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        store.dispatch(Utilities.actionCreator(ACTION_INCREMENT, null));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        assertTrue(done.await(60, TimeUnit.SECONDS));
        assertEquals(total, store.getState().getCount());
    }

    @Test
    public void concurrentDispatchAndTimeTravelDoesNotCorruptState() throws InterruptedException {
        int dispatchThreads = 8;
        int actionsPerThread = 200;

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(dispatchThreads + 1);

        Runnable dispatchWork = () -> {
            try {
                start.await();
                for (int j = 0; j < actionsPerThread; j++) {
                    store.dispatch(Utilities.actionCreator(ACTION_ADD, 1));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        };

        for (int i = 0; i < dispatchThreads; i++) {
            new Thread(dispatchWork).start();
        }

        new Thread(() -> {
            try {
                start.await();
                for (int j = 0; j < 50; j++) {
                    store.goBack();
                    store.goForward();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        }).start();

        start.countDown();
        assertTrue(done.await(60, TimeUnit.SECONDS));

        int finalCount = store.getState().getCount();
        assertTrue(finalCount >= 0, "Final count must not be negative after concurrent dispatch/time-travel");
    }

    @Test
    public void concurrentSliceDispatchesAreCorrect() throws InterruptedException {
        Slice<CounterState> slice = new DuxSliceBuilder<CounterState>()
                .setInitialState(new CounterState())
                .addReducer(ACTION_INCREMENT, (action, state) -> {
                    state.setCount(state.getCount() + 1);
                    return state;
                })
                .build();

        int threads = 10;
        int incrementsPerThread = 500;
        int total = threads * incrementsPerThread;

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        slice.getAction(ACTION_INCREMENT).accept(null);
                    }
                } catch (Exception e) {
                    // test failure will be caught by latch timeout / assertion
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        assertTrue(done.await(60, TimeUnit.SECONDS));
        assertEquals(total, slice.getState().getCount());
    }

    @Test
    public void getStateDuringConcurrentDispatchDoesNotThrow() throws InterruptedException {
        int threads = 10;
        int actionsPerThread = 500;

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads + 1);
        AtomicInteger reads = new AtomicInteger(0);

        Runnable writer = () -> {
            try {
                start.await();
                for (int j = 0; j < actionsPerThread; j++) {
                    store.dispatch(Utilities.actionCreator(ACTION_INCREMENT, null));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        };

        Runnable reader = () -> {
            try {
                start.await();
                for (int j = 0; j < actionsPerThread; j++) {
                    CounterState state = store.getState();
                    assertNotNull(state);
                    reads.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        };

        for (int i = 0; i < threads; i++) {
            new Thread(writer).start();
        }
        new Thread(reader).start();

        start.countDown();
        assertTrue(done.await(60, TimeUnit.SECONDS));
        assertEquals(actionsPerThread, reads.get());
    }

    @Test
    public void throwingListenerDuringConcurrentDispatchDoesNotBreakOthers() throws InterruptedException {
        int threads = 5;
        int actionsPerThread = 100;
        int total = threads * actionsPerThread;

        CountDownLatch latch = new CountDownLatch(total);
        store.subscribe(state -> { throw new RuntimeException("listener boom"); });
        store.subscribe(state -> latch.countDown());

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    for (int j = 0; j < actionsPerThread; j++) {
                        store.dispatch(Utilities.actionCreator(ACTION_INCREMENT, null));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        assertTrue(done.await(60, TimeUnit.SECONDS));
        assertTrue(latch.await(60, TimeUnit.SECONDS),
                "Non-throwing listener should receive every notification despite concurrent throwing listeners");
    }
}
