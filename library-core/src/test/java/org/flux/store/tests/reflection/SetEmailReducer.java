package org.flux.store.tests.reflection;

import org.flux.store.api.v1.Action;
import org.flux.store.api.v3.ReducerBlock;
import org.flux.store.api.v3.AutoStore;
import org.flux.store.tests.domain.UserProfile;

@AutoStore
public class SetEmailReducer implements ReducerBlock<UserProfile> {
    @Override
    public String getType() {
        return "setEmail";
    }

    @Override
    public UserProfile reduce(Action action, UserProfile state) {
        state.setEmail(action.getPayload().toString());
        return state;
    }
}
