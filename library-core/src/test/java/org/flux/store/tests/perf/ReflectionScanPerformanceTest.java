package org.flux.store.tests.perf;

import org.flux.store.api.v2.Slice;
import org.flux.store.main.v3.ReflectionDuxSliceBuilder;
import org.flux.store.tests.domain.UserProfile;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("perf")
public class ReflectionScanPerformanceTest {

    private static final int BUILDS = 10;
    private static final long MAX_ALLOWED_TIME_NANOS = TimeUnit.MILLISECONDS.toNanos(500);

    @Test
    public void reflectionSliceBuildPerformance() {
        long start = System.nanoTime();
        for (int i = 0; i < BUILDS; i++) {
            Slice<UserProfile> slice = new ReflectionDuxSliceBuilder<UserProfile>()
                    .setInitialState(new UserProfile("Karan", "karan@hello.com"))
                    .setStoreName("CleanStore")
                    .setBasePackage("org.flux.store.tests.reflection.clean")
                    .build();
            assertTrue(slice.getState().getName().contains("Karan"));
        }
        long elapsed = System.nanoTime() - start;
        double avgMillis = (elapsed / (double) BUILDS) / 1_000_000.0;

        System.out.printf("Reflection scan: %d builds in %.3f s (avg %.3f ms)%n",
                BUILDS, elapsed / 1_000_000_000.0, avgMillis);

        assertTrue(elapsed <= MAX_ALLOWED_TIME_NANOS,
                String.format("Reflection scan took %.3f s, exceeding %.3f s",
                        elapsed / 1_000_000_000.0, MAX_ALLOWED_TIME_NANOS / 1_000_000_000.0));
    }
}
