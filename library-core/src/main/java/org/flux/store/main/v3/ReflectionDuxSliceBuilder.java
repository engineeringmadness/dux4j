package org.flux.store.main.v3;

import lombok.Getter;
import org.flux.store.api.v1.State;
import org.flux.store.api.v2.Middleware;
import org.flux.store.api.v1.Reducer;
import org.flux.store.api.v3.AutoStore;
import org.flux.store.api.v3.ReducerBlock;
import org.flux.store.main.v2.DuxSlice;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class ReflectionDuxSliceBuilder<T extends State> {

    private String basePackage;
    private List<Consumer<T>> subscribers = new ArrayList<>();
    private Middleware<T> middleware;
    private T initialState;

    public ReflectionDuxSliceBuilder<T> setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }

    public ReflectionDuxSliceBuilder<T> setInitialState(T initialState) {
        this.initialState = initialState;
        return this;
    }

    public ReflectionDuxSliceBuilder<T> setMiddleware(Middleware<T> middleware) {
        this.middleware = middleware;
        return this;
    }

    public ReflectionDuxSliceBuilder<T> addSubscriber(Consumer<T> subscriber) {
        this.subscribers.add(subscriber);
        return this;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Reducer<T>> discoverReducers() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(AutoStore.class);
        Map<String, Reducer<T>> reducers = new HashMap<>();
        for (Class<?> clazz : annotated) {
            AutoStore annotation = clazz.getAnnotation(AutoStore.class);
            if (annotation != null && ReducerBlock.class.isAssignableFrom(clazz)) {
                ReducerBlock<T> instance = getInstance(clazz);
                reducers.put(instance.getType(), instance);
            }
        }
        return reducers;
    }

    private ReducerBlock<T> getInstance(Class<?> clazz) {
        try {
            ReducerBlock<T> instance = (ReducerBlock<T>) clazz.getDeclaredConstructor().newInstance();
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate ReducerBlock: " + clazz.getName(), e);
        }
    }

    public DuxSlice<T> build() {
        Map<String, Reducer<T>> reducers = discoverReducers();
        return DuxSlice.createSlice(
                this.initialState,
                reducers,
                this.subscribers,
                this.middleware);
    }
}
