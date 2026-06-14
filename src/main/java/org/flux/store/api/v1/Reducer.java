package org.flux.store.api.v1;

@FunctionalInterface
public interface Reducer<T extends State> {

    T reduce(Action action, T state);
}
