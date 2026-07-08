package org.flux.store.api.v2;

import org.flux.store.api.exceptions.InvalidActionException;
import org.flux.store.api.v1.State;

import java.util.function.Consumer;

public interface Slice<T extends State> {
    T getState();
    Consumer getAction(String type) throws InvalidActionException;
    void goBack();
    void goForward();
}
