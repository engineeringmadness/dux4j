package org.flux.store.tests.domain;

import lombok.*;
import org.flux.store.api.v1.State;

@Getter
@Setter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class CounterState implements State {
    private int count;

    public CounterState() {
        this.count = 0;
    }

    @Override
    public CounterState clone() {
        try {
            return (CounterState) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
