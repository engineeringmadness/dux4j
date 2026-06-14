package org.flux.store.api.v1;

import java.util.function.Consumer;

public interface Store<T extends State> {

    void subscribe(Consumer<T> fn);
    void dispatch(Action action);
    T getState();
    void replaceReducer(Reducer<T> newReducer);
    void goBack();
    void goForward();
}
