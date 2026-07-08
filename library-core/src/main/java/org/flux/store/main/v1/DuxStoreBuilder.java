package org.flux.store.main.v1;

import lombok.Getter;
import org.flux.store.api.v2.Middleware;
import org.flux.store.api.v1.Reducer;
import org.flux.store.api.v1.State;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class DuxStoreBuilder<T extends State> {

    private T initialState;
    private Reducer<T> reducer;
    public List<Consumer<T>> listeners = new ArrayList<>();
    private Middleware<T> middleware;
    private String backupPath;
    private Boolean autoBackup = false;
    private Boolean asyncFlag = false;

    public DuxStoreBuilder<T> setInitialState(T initialState) {
        this.initialState = initialState;
        return this;
    }

    public DuxStoreBuilder<T> setReducer(Reducer<T> reducer) {
        this.reducer = reducer;
        return this;
    }

    public DuxStoreBuilder<T> addListener(Consumer<T> listener) {
        this.listeners.add(listener);
        return this;
    }

    public DuxStoreBuilder<T> setMiddleware(Middleware<T> middleware) {
        this.middleware = middleware;
        return this;
    }

    public DuxStoreBuilder<T> enableAutoBackup(String backupPath) {
        this.autoBackup = true;
        this.backupPath = backupPath;
        return this;
    }

    public DuxStoreBuilder<T> enableAsyncNotifications() {
        this.asyncFlag = true;
        return this;
    }

    public DuxStore<T> build() {
        return new DuxStore<>(this);
    }
}
