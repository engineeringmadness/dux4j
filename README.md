# Dux4j

![workflow](https://github.com/compscikaran/dux4j/actions/workflows/ci-build.yml/badge.svg)

Redux like unidirectional state management implementation for Java.

## How the idea came about

The idea came about when I had finished Facebook's Talk on Flux Architecture and seen few of Dan Abramov's interviews, and I thought to myself why this kind of pattern does not exist in java.
So I set out to implement the same in the language I use everyday.

## [Get Started](https://github.com/compscikaran/dux4j/wiki)

## What is Dux4j

Key Features -
1. Simple Redux like Unidirectional application store
2. Supports all the familiar patterns such as reducers, subscribers, actions, action creators, thunks, middlewares
3. Time travel debugging which allows to go to any previous or forward state in the store's history
4. Allow backup and restore of application state and syncing application state to persistant storage

## Using Redux Slices (v3)

The v3 API uses reflection to auto-discover reducers from a given package, eliminating boilerplate. Here's a complete example:

### 1. Define your state model

```java
import org.flux.store.api.v1.State;
import lombok.*;

@Getter @Setter @AllArgsConstructor @ToString
public class UserProfile implements State {
    private String name;
    private String email;

    @Override
    public UserProfile clone() {
        try { return (UserProfile) super.clone(); }
        catch (CloneNotSupportedException e) { return null; }
    }
}
```

### 2. Define reducer classes annotated with `@AutoStore`

Each reducer handles one action type and is discovered automatically at build time.

```java
import org.flux.store.api.v1.Action;
import org.flux.store.api.v3.AutoStore;
import org.flux.store.api.v3.ReducerBlock;

@AutoStore("MyStore")
public class SetNameReducer implements ReducerBlock<UserProfile> {
    @Override
    public String getType() { return "setName"; }

    @Override
    public UserProfile reduce(Action action, UserProfile state) {
        state.setName(action.getPayload().toString());
        return state;
    }
}
```

```java
@AutoStore("MyStore")
public class SetEmailReducer implements ReducerBlock<UserProfile> {
    @Override
    public String getType() { return "setEmail"; }

    @Override
    public UserProfile reduce(Action action, UserProfile state) {
        state.setEmail(action.getPayload().toString());
        return state;
    }
}
```

### 3. Build and use the slice

```java
import org.flux.store.api.v2.Slice;
import org.flux.store.main.v3.ReflectionDuxSliceBuilder;

Slice<UserProfile> slice = new ReflectionDuxSliceBuilder<UserProfile>()
        .setInitialState(new UserProfile("Karan Gupta", "karan@hello.com"))
        .setStoreName("MyStore")
        .setBasePackage("com.example.app.reducers")
        .build();

// Dispatch actions by name — reducers are located via reflection
slice.getAction("setName").accept("Manoj Gupta");
slice.getAction("setEmail").accept("manoj@hello.com");

System.out.println(slice.getState().getName());  // Manoj Gupta
System.out.println(slice.getState().getEmail()); // manoj@hello.com
```

The builder scans all classes in `basePackage` for `@AutoStore("MyStore")` reducers, maps them by their `getType()` value, and wires them into the slice automatically.