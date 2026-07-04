package org.flux.store.tests.reflection;

import org.flux.store.api.v1.Action;
import org.flux.store.api.v3.AutoStore;
import org.flux.store.api.v3.ReducerBlock;
import org.flux.store.tests.domain.UserProfile;

@AutoStore("OtherStore")
public class OtherStoreReducer implements ReducerBlock<UserProfile> {

    @Override
    public String getType() {
        return "setName";
    }

    @Override
    public UserProfile reduce(Action action, UserProfile state) {
        state.setName("other:" + action.getPayload().toString());
        return state;
    }
}
