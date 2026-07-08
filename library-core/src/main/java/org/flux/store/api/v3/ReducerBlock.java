package org.flux.store.api.v3;

import org.flux.store.api.v1.Reducer;
import org.flux.store.api.v1.State;

public interface ReducerBlock<T extends State> extends Reducer<T> {
    String getType();
}