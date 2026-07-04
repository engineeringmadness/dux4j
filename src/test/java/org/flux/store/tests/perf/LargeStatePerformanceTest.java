package org.flux.store.tests.perf;

import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.CatalogItem;
import org.flux.store.tests.domain.CatalogState;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("perf")
public class LargeStatePerformanceTest {

    private static final String ACTION_ADD_ITEM = "ADD_ITEM";
    private static final String ACTION_UPDATE_PRICE = "UPDATE_PRICE";
    private static final int ITEM_COUNT = 100;
    private static final int DISPATCH_COUNT = 5_000;
    private static final long MAX_ALLOWED_TIME_NANOS = TimeUnit.MILLISECONDS.toNanos(500);

    @Test
    public void largeStateDispatchThroughput() {
        List<CatalogItem> items = new ArrayList<>();
        for (int i = 0; i < ITEM_COUNT; i++) {
            items.add(new CatalogItem("id-" + i, "item-" + i, i * 1.0));
        }
        CatalogState initial = new CatalogState(items);

        DuxStore<CatalogState> store = new DuxStore<>(initial, (action, state) -> {
            switch (action.getType()) {
                case ACTION_ADD_ITEM:
                    state.getItems().add((CatalogItem) action.getPayload());
                    break;
                case ACTION_UPDATE_PRICE:
                    int index = (Integer) action.getPayload();
                    CatalogItem item = state.getItems().get(index % state.getItems().size());
                    item.setPrice(item.getPrice() + 1);
                    break;
            }
            return state;
        });

        long start = System.nanoTime();
        for (int i = 0; i < DISPATCH_COUNT; i++) {
            if (i % 10 == 0) {
                store.dispatch(Utilities.actionCreator(ACTION_ADD_ITEM,
                        new CatalogItem("new-" + i, "new-item-" + i, i)));
            } else {
                store.dispatch(Utilities.actionCreator(ACTION_UPDATE_PRICE, i));
            }
        }
        long elapsed = System.nanoTime() - start;

        System.out.printf("Large state: %d dispatches on state with %d items in %.3f s%n",
                DISPATCH_COUNT, store.getState().getItems().size(), elapsed / 1_000_000_000.0);

        assertEquals(ITEM_COUNT + DISPATCH_COUNT / 10, store.getState().getItems().size());
        assertTrue(elapsed <= MAX_ALLOWED_TIME_NANOS,
                String.format("Large-state dispatch took %.3f s, exceeding %.3f s",
                        elapsed / 1_000_000_000.0, MAX_ALLOWED_TIME_NANOS / 1_000_000_000.0));
    }
}
