package org.flux.store.main.v2;

import org.flux.store.api.exceptions.InvalidActionException;
import org.flux.store.api.v1.State;
import org.flux.store.api.v2.Middleware;
import org.flux.store.api.v1.Reducer;
import org.flux.store.api.v2.Slice;
import org.flux.store.main.v1.DuxStore;
import org.flux.store.utils.Utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DuxSlice<T extends State> implements Slice<T> {

    private DuxStore<T> store;
    private List<String> actions;

    private DuxSlice(DuxStore<T> store, List<String> actions) {
        this.store = store;
        this.actions = actions;
    }

    public static <T extends State> DuxSlice<T> createSlice(T initialState, Map<String, Reducer<T>> reducers, List<Consumer<T>> subscribers, Middleware<T> middleware) {
        Reducer<T> reducer = (action, state) -> {
            for (String key: reducers.keySet()) {
                if(action.getType().equalsIgnoreCase(key)) {
                    Reducer<T> current = reducers.get(key);
                    state = current.reduce(action,state);
                }
            }
            return state;
        };
        DuxStore<T> myStore;
        if(middleware != null) {
            myStore = new DuxStore<>(initialState, reducer, middleware);
        } else {
            myStore = new DuxStore<>(initialState, reducer);
        }
        for (Consumer<T> subscriber: subscribers) {
            myStore.subscribe(subscriber);
        }
        DuxSlice<T> slice = new DuxSlice<>(myStore, new ArrayList<>(reducers.keySet()));
        return slice;
    }

    protected static <T extends State> DuxSlice<T> createSlice(DuxSliceBuilder<T> builder) {
        return DuxSlice.createSlice(
                builder.getInitialState(),
                builder.getReducers(),
                builder.getSubscribers(),
                builder.getMiddleware());
    }

    public Consumer getAction(String type) throws InvalidActionException {
        if(!actions.contains(type))
            throw new InvalidActionException("Action type does not exist on slice");
        return payload -> store.dispatch(Utilities.actionCreator(type, payload));
    }

    @Override
    public void goBack() {
        this.store.goBack();
    }

    @Override
    public void goForward() {
        this.store.goForward();
    }

    public T getState() {
        return store.getState();
    }
}
