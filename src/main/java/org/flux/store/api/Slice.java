package org.flux.store.api;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public interface Slice<T extends State> {
    T getState();
    Consumer getAction(String type) throws InvalidActionException;
    void goBack();
    void goForward();
}
