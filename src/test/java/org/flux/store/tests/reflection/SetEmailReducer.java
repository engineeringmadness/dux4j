package org.flux.store.tests.reflection;

import org.flux.store.api.Action;
import org.flux.store.api.ReducerBlock;
import org.flux.store.api.AutoStore;
import org.flux.store.tests.domain.UserProfile;

@AutoStore("MyStore")
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
