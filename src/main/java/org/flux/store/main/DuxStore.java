package org.flux.store.main;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.DiffResult;
import org.flux.store.api.*;
import org.flux.store.utils.AsyncProcessor;
import org.flux.store.utils.TimeTravel;
import org.flux.store.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
public class DuxStore<T extends State> implements Store<T> {

    private static final Logger log = LoggerFactory.getLogger(DuxStore.class);

    private Reducer<T> reducer;
    private TimeTravel<T> timeTravel = new TimeTravel<>();
    private T state;
    public List<Consumer<T>> listeners = new ArrayList<>();
    private Middleware<T> middleware;
    private Gson gson = new Gson();

    public DuxStore(T initialState, Reducer<T> reducer) {
        this.state = initialState;
        this.reducer = reducer;
        this.timeTravel.recordChange(new Action<>(Utilities.INITIAL_ACTION, initialState), initialState);
    }

    public DuxStore(DuxStoreBuilder<T> builder) {
        this.state = builder.getInitialState();
        this.reducer = builder.getReducer();
        this.listeners = builder.listeners;
        this.middleware = builder.getMiddleware();
        this.timeTravel.recordChange(new Action<>(Utilities.INITIAL_ACTION, builder.getInitialState()), builder.getInitialState());
    }

    public DuxStore(T initialState, Reducer<T> reducer, Middleware<T> middleware) {
        this(initialState, reducer);
        this.middleware = middleware;
    }

    @Override
    public void subscribe(Consumer<T> fn) {
        this.listeners.add(fn);
    }

    @Override
    public void dispatch(Action action) {
        if(middleware != null) {
            middleware.run(this, this::dispatchInternal, action);
        } else {
            dispatchInternal(action);
        }
    }

    private void dispatchInternal(Action action) {
        T newState = reducer.reduce(action, (T) state.clone());
        DiffResult diffResult = newState.diff(state);
        boolean isChanged = diffResult.getNumberOfDiffs() > 0;
        if(isChanged) {
            this.state = newState;
            this.timeTravel.recordChange(action, newState);
            this.notifyListeners();
        }
    }

    private void dispatchNoNotify(Action action) {
        if(action.getType().equalsIgnoreCase(Utilities.INITIAL_ACTION)) {
            // Ignore action
        } else if(action.getType().equalsIgnoreCase(Utilities.RESTORE_ACTION)) {
            this.state = (T) action.getPayload();
        } else {
            T newState = reducer.reduce(action, (T) state.clone());
            this.state = newState;
        }
    }

    private void dispatchTimeTravel(Action action) {
        if(middleware != null) {
            middleware.run(this, this::dispatchNoNotify, action);
        } else {
            dispatchNoNotify(action);
        }
    }

    @Override
    public T getState() {
        return this.state;
    }

    @Override
    public void replaceReducer(Reducer<T> newReducer) {
        this.reducer = newReducer;
    }

    private void notifyListeners() {
        this.listeners.forEach(l -> AsyncProcessor.submitNotify(l, this.state));
    }

    public void goBack() {
        boolean recreateSnapshot = this.timeTravel.goBack();
        this.state = this.timeTravel.getSnapshot();
        if(recreateSnapshot) {
            this.state = this.timeTravel.getInitialState();
            List<Action> actionsForSnapshot = this.timeTravel.getActionHistory();
            for (Action action: actionsForSnapshot) {
                dispatchTimeTravel(action);
            }
            this.timeTravel.setSnapshot(this.state);
        }
        List<Action> actionsToRecreateState = this.timeTravel.getActionToRecreate();
        for (Action action: actionsToRecreateState) {
            dispatchTimeTravel(action);
        }
        this.notifyListeners();
    }

    public void goForward() {
        this.timeTravel.goForward();
        Action latestAction = this.timeTravel.getLatestAction();
        T newState = reducer.reduce(latestAction, (T) state.clone());
        this.state = newState;
        this.notifyListeners();
    }

    public List<String> getActionHistory() {
        return this.timeTravel.getFullActionHistory();
    }
}
