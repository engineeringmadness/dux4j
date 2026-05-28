package org.flux.store.main;

import lombok.Getter;
import org.flux.store.api.*;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class ReflectionDuxSliceBuilder<T extends State> {

    private String storeName;
    private String basePackage;
    private List<Consumer<T>> subscribers = new ArrayList<>();
    private Middleware<T> middleware;
    private T initialState;
    private Boolean asyncFlag = false;
    private String backupPath;
    private Boolean autoBackup = false;

    public ReflectionDuxSliceBuilder<T> setStoreName(String storeName) {
        this.storeName = storeName;
        return this;
    }

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

    public ReflectionDuxSliceBuilder<T> enableAutoBackup(String backupPath) {
        this.autoBackup = true;
        this.backupPath = backupPath;
        return this;
    }

    public ReflectionDuxSliceBuilder<T> addSubscriber(Consumer<T> subscriber) {
        this.subscribers.add(subscriber);
        return this;
    }

    public ReflectionDuxSliceBuilder<T> enableAsyncNotifications() {
        this.asyncFlag = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Reducer<T>> discoverReducers() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(AutoStore.class);
        Map<String, Reducer<T>> reducers = new HashMap<>();
        for (Class<?> clazz : annotated) {
            AutoStore annotation = clazz.getAnnotation(AutoStore.class);
            if (annotation.value().equals(storeName) && ReducerBlock.class.isAssignableFrom(clazz)) {
                System.out.println("Found reducer for : " + this.storeName + " " + clazz.getName());
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
                this.middleware,
                this.asyncFlag,
                this.autoBackup,
                this.backupPath);
    }
}
