package org.flux.store.api;

public interface ReducerBlock<T extends State> extends Reducer<T> {
    String getType();
}