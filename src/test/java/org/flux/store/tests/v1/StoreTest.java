package org.flux.store.tests.v1;

import org.flux.store.api.v1.Action;
import org.flux.store.api.v1.Reducer;
import org.flux.store.utils.Utilities;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.tests.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class  StoreTest {

    public static final String INITIAL_EMAIL = "karan@hello.com";
    public static final String INITIAL_NAME = "Karan Gupta";
    public static final String ACTION_SET_EMAIL = "SET_EMAIL";
    public static final String ACTION_SET_NAME = "SET_NAME";

    private DuxStore<UserProfile> myStore;

    @BeforeEach
    public void init() {
        UserProfile initialState = new UserProfile(INITIAL_NAME, INITIAL_EMAIL);
        myStore = new DuxStore<>(initialState, (action, state) -> {
            switch (action.getType()) {
                case ACTION_SET_EMAIL:
                    String newEmail = action.getPayload().toString();
                    state.setEmail(newEmail);
                    break;
                case ACTION_SET_NAME:
                    String newName = action.getPayload().toString();
                    state.setName(newName);
                    break;
                default:
                    throw new RuntimeException("Action Type not supported by reducer");
            }
            return state;
        });
    }

    @Test
    public void canChangeEmail() {
        String newEmail = "karan@gmail.com";
        Action<String> action = Utilities.actionCreator(ACTION_SET_EMAIL, newEmail);
        myStore.dispatch(action);
        assertEquals(newEmail, myStore.getState().getEmail());
    }

    @Test
    public void canDetectEmailChange() {
        String newEmail = "karan@gmail.com";
        Action<String> action = Utilities.actionCreator(ACTION_SET_EMAIL, newEmail);
        myStore.subscribe(x -> {
            System.out.println("State has changed...");
            assertNotNull(x);
            System.out.println(x);
        });
        System.out.println(myStore.getState());
        myStore.dispatch(action);
    }

    @Test
    public void reducerCanBeReplaced() {
        String newName = "Lena Gupta";
        Reducer<UserProfile> newReducer = (action, state) -> {
          return state;
        };
        myStore.replaceReducer(newReducer);
        Action<String> action2 = Utilities.actionCreator(ACTION_SET_NAME, newName);
        myStore.dispatch(action2);
        assertEquals(INITIAL_NAME, myStore.getState().getName());
    }
}
