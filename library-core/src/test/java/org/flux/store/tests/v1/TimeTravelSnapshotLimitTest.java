package org.flux.store.tests.v1;

import org.flux.store.api.v1.Action;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.UserProfile;
import org.flux.store.utils.TimeTravel;
import org.flux.store.utils.Utilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TimeTravelSnapshotLimitTest {

    public static final String INITIAL_EMAIL = "karan@hello.com";
    public static final String INITIAL_NAME = "Karan Gupta";
    public static final String ACTION_SET_NAME = "SET_NAME";

    private DuxStore<UserProfile> myStore;

    @BeforeEach
    public void init() {
        UserProfile initialState = new UserProfile(INITIAL_NAME, INITIAL_EMAIL);
        myStore = new DuxStore<>(initialState, (action, state) -> {
            if (action.getType().equals(ACTION_SET_NAME)) {
                state.setName(action.getPayload().toString());
            }
            return state;
        });
    }

    @Test
    public void snapshotListShouldNotExceedLimit() throws Exception {

        // Dispatch enough actions to exceed the snapshot limit
        // snapshotThreshold = 10, so need (limit * 10) + buffer actions
        int actionsNeeded = (TimeTravel.snapshotNumberLimit * 10) + 100;
        for (int i = 0; i < actionsNeeded; i++) {
            Action<String> action = Utilities.actionCreator(ACTION_SET_NAME, "Name" + i);
            myStore.dispatch(action);
        }

        // Access checkpoint states via reflection
        Field timeTravelField = DuxStore.class.getDeclaredField("timeTravel");
        timeTravelField.setAccessible(true);
        Object timeTravel = timeTravelField.get(myStore);

        Field checkpointStatesField = timeTravel.getClass().getDeclaredField("checkpointStates");
        checkpointStatesField.setAccessible(true);
        List<?> checkpointStates = (List<?>) checkpointStatesField.get(timeTravel);

        // FAIL if snapshot count exceeds the limit
        assertTrue(checkpointStates.size() <= TimeTravel.snapshotNumberLimit,
                "Snapshot count (" + checkpointStates.size() +
                ") exceeds snapshotNumberLimit (" + TimeTravel.snapshotNumberLimit +
                "). This causes unbounded memory growth and OOM errors.");
    }
}
