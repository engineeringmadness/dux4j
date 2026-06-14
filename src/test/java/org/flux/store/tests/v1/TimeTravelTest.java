package org.flux.store.tests.v1;

import com.github.javafaker.Faker;
import org.flux.store.api.v1.Action;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.utils.Utilities;
import org.flux.store.tests.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TimeTravelTest {

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
    public void canTravelBack() {
        String newEmail = "karan@gmail.com";
        String newEmail2 = "karan@hotmail.com";
        Action<String> action = Utilities.actionCreator(ACTION_SET_EMAIL, newEmail);
        Action<String> action2 = Utilities.actionCreator(ACTION_SET_EMAIL, newEmail2);
        System.out.println(myStore.getState());
        myStore.dispatch(action);
        myStore.dispatch(action2);
        myStore.goBack();
        myStore.goBack();
        System.out.println(myStore.getState());
        assertEquals(INITIAL_EMAIL, myStore.getState().getEmail());
    }

    @Test
    public void canTravelForward() {
        String newEmail = "karan@gmail.com";
        String newEmail2 = "karan@hotmail.com";
        Action<String> action = Utilities.actionCreator(ACTION_SET_EMAIL, newEmail);
        Action<String> action2 = Utilities.actionCreator(ACTION_SET_EMAIL, newEmail2);
        System.out.println(myStore.getState());
        myStore.dispatch(action);
        myStore.dispatch(action2);
        myStore.goBack();
        myStore.goBack();
        myStore.goForward();
        System.out.println(myStore.getState());
        assertEquals(newEmail, myStore.getState().getEmail());
    }

    @Test
    public void historyIsAccurate() {
        String newEmail = "manoj@gmail.com";
        String newName = "Manoj Gupta";
        Action<String> action = Utilities.actionCreator(ACTION_SET_EMAIL, newEmail);
        myStore.dispatch(action);
        Action<String> action2 = Utilities.actionCreator(ACTION_SET_NAME, newName);
        myStore.dispatch(action2);
        assertEquals(newEmail, myStore.getState().getEmail());
        assertEquals(newName, myStore.getState().getName());
        List<String> history = myStore.getActionHistory();
        assertTrue(history.size() == 3);
        assertEquals(ACTION_SET_EMAIL, history.get(1));
        assertEquals(ACTION_SET_NAME, history.get(2));
    }

    @Test
    public void canTravelHalfway() {
        String newEmail = "karan@gmail.com";
        String newEmail2 = "karan@hotmail.com";
        String newName = "Sandeep Rajwar";
        String newName2 = "Hardik Pathak";
        Action<String> action = Utilities.actionCreator(ACTION_SET_EMAIL, newEmail);
        Action<String> action3 = Utilities.actionCreator(ACTION_SET_EMAIL, newEmail2);
        Action<String> action2 = Utilities.actionCreator(ACTION_SET_NAME, newName);
        Action<String> action4 = Utilities.actionCreator(ACTION_SET_NAME, newName2);
        System.out.println(myStore.getState());
        Arrays.asList(action, action2, action3, action4).forEach(x -> myStore.dispatch(x));
        myStore.goBack();
        myStore.goBack();
        myStore.goBack();
        myStore.goBack();
        myStore.goForward();
        myStore.goForward();
        System.out.println(myStore.getState());
        assertEquals(newEmail, myStore.getState().getEmail());
        assertEquals(newName, myStore.getState().getName());
    }

    @ParameterizedTest
    @CsvSource({"45,35", "7,3", "45, 20", "2,1", "100,1", "30,20", "20,19", "40,38"})
    public void canTravelBackBeyondCheckpoint(String actionsValue, String pointValue) {
        Integer numActions = Integer.valueOf(actionsValue);
        Integer point = Integer.valueOf(pointValue);
        String prevName = "";
        Faker faker = new Faker();
        for (int i = 0; i < numActions; i++) {
            if(i == point) {
                prevName = myStore.getState().getName();
            }
            String newName = faker.name().fullName();
            myStore.dispatch(Utilities.actionCreator(ACTION_SET_NAME, newName));
            System.out.println((i + 1) + ". " + myStore.getState());
        }
        for(int i = 0; i < (numActions - point); i++) {
            myStore.goBack();
        }
        System.out.println(myStore.getState());
        assertEquals(prevName, myStore.getState().getName());
    }

}
