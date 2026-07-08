package org.flux.store.api.v2;

import org.flux.store.api.v1.State;
import org.flux.store.api.v1.Action;
import org.flux.store.api.v1.Store;

import java.util.function.Consumer;

@FunctionalInterface
public interface Middleware<T extends State> {

    void run(Store<T> store, Consumer<Action> next, Action action);
}