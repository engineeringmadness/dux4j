package org.flux.store.api.v1;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class Action<T> {

    private final String type;
    private final T payload;

    public Action(String type, T payload) {
        this.type = type;
        this.payload = payload;
    }
}
