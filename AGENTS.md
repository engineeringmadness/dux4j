# Dux4J

## Project Summary

Dux4j is a Java port of the Redux JS library based on Flux pattern open sourced by Facebook in the early 2010s. 
It is a state management alternative to the traditional MVC architecture that is usually found in Java / Spring backend applications.

The library comes fully featured with all first class primitives that are present in a Redux style store API i.e. Actions, Reducers, Subscribers, Middleware, Dispatch, Store, Slice.
The store also contains the ability to go backwards and forwards in time using a snapshotting system.

## Important conventions

1. The data types are the core to the design aesthetic of the library. Located in @src/main/java/org/flux/store/api. The classes and interfaces defined here form the core foundation of the state management system.
2. The project has evolved in its API offering over the years hence you will find multiple places where the project is structred in v1, v2, v3... folders

### Versions overview

**v1** - This is the original API called Store API. It offers a basic Redux style store implementation where use provides initial state and a single reducer function.
```java
UserProfile initialState = new UserProfile(INITIAL_NAME, INITIAL_EMAIL);
myStore = new DuxStore<>(initialState, (action, state) -> {
    switch (action.getType()) {
        case ACTION_SET_EMAIL:
            String newEmail = action.getPayload().toString();
            state.setEmail(newEmail);
            break;
        case ACTION_SET_NAME:
            String newName = action.getPayload().toString();
            state.setName(newName);
            break;
        default:
            throw new RuntimeException("Action Type not supported by reducer");
    }
    return state;
});
```

**v2** - This is the 2nd iteration which is aimed at offering a cleaner API called Slice API. Its inspired by the Redux Slice feature that was offered by Redux toolkit.
It also comes with a Builder pattern to make the code look more syntactically similar to its JS equivalent.

```java
var slice = new DuxSliceBuilder<UserProfile>()
                .setInitialState(new UserProfile("Karan Gupta", "karan@hello.com"))
                .addReducer("setEmail", (action, state) -> {
                    state.setEmail(action.getPayload().toString());
                    return state;
                })
                .addReducer("setName", (action, state) -> {
                    state.setName(action.getPayload().toString());
                    return state;
                })
                .addSubscriber((state) -> System.out.println(state))
                .build();
```

This API is best suited for implementing session state or client state where each individual user / client of application has their own Slice object

**v3** - This is the 3rd iteration and offers a Spring bootesque API called Auto Reflection Store.
In this approach the user can define one (or more) reducers as standalone classes that implement the ReducerBlock interface.
These form independent logical bundles in your application. The reducers are then annotated with @AutoStore(value = <name of store>) to indicate to Dux4j
That these classes are containing reducer logic.

Using reflection on the package path, these reducers are picked up and combined to create a single store. 

```java
var slice = new ReflectionDuxSliceBuilder<UserProfile>()
        .setInitialState(new UserProfile("Karan Gupta", "karan@hello.com"))
        .setStoreName("MyStore")
        .setBasePackage("org.flux.store.tests.reflection")
        .build();
```

This API is best suited for application where a single common Redux store is used for the entirety of the application, perhaps to manage global state.

**Utilities** - Dux4J also comes with the common utilities that used to ship in the original Redux package such as `combineReducer` and `compose`

## Testing workflow

The test suite consists of functional, reliability and performance tests

Functional, Reliability tests are to be used for checking regression when refactoring or introducing new changes
> mvn test

Performance tests are run after ensuring there is no regression issues
> mvn test -Pperf

## Agent Docs

Additional library documentation is available in @library-core/agent-docs/