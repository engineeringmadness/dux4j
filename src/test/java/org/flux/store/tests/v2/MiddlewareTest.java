package org.flux.store.tests.v2;

import org.flux.store.api.v1.Action;
import org.flux.store.api.v2.Middleware;
import org.flux.store.api.v1.Reducer;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.utils.Utilities;
import org.flux.store.tests.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MiddlewareTest {

    public static final String INITIAL_EMAIL = "karan@hello.com";
    public static final String INITIAL_NAME = "Karan Gupta";
    public static final String ACTION_SET_NAME = "SET_NAME";
    public static final String SPECIAL_INPUT = "Tom Marvolo Riddle";
    public static final String TRANSFORMED_INPUT = "Lord Voldemort";

    private DuxStore<UserProfile> myStore;

    @BeforeEach
    public void init() {
        UserProfile initialState = new UserProfile(INITIAL_NAME, INITIAL_EMAIL);
        Reducer<UserProfile> reducer = (action, state) -> {
            switch (action.getType()) {
                case ACTION_SET_NAME:
                    String newName = action.getPayload().toString();
                    state.setName(newName);
                    break;
            }
            return state;
        };

        Middleware<UserProfile> middleware = (store, next, action) -> {
            System.out.println(action);
            if(action.getPayload().toString().equalsIgnoreCase(SPECIAL_INPUT)) {
              Action<String> modifiedAction = Utilities.actionCreator(action.getType(), TRANSFORMED_INPUT);
              next.accept(modifiedAction);
            } else {
                next.accept(action);
            }
        };
        myStore = new DuxStore<>(initialState, reducer, middleware);
    }

    @Test
    public void middlewareWorks() {
        Action<String> action = Utilities.actionCreator(ACTION_SET_NAME, SPECIAL_INPUT);
        myStore.dispatch(action);
        assertEquals(TRANSFORMED_INPUT, myStore.getState().getName());
    }

    @Test
    public void middlewareIgnoresIrrelevantInput() {
        String newName = "Manoj Gupta";
        Action<String> action = Utilities.actionCreator(ACTION_SET_NAME, newName);
        myStore.dispatch(action);
        assertEquals(newName, myStore.getState().getName());
    }

}
