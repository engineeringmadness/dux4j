package org.flux.store.api.v1;

import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ReflectionDiffBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public interface State extends Diffable<State>, Cloneable, Serializable {

    @Override
    default DiffResult diff(State p) {
        return new ReflectionDiffBuilder<>(this, p, ToStringStyle.SIMPLE_STYLE)
                .build();
    }

    State clone();
}

