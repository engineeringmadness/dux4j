To make the creation and usage of DuxStore easier a simpler API has been added called as DuxSlice. This is a wrapper on top of DuxStore which offers an easier way to declare the dux store. Using DuxSlice API we can specify a bunch of mini-reducers which can act on various attributes of state which eliminates the need for having one large reducer with tons of if else blocks or switch case statements.

```java
public interface Slice<T extends State> {
    T getState();
    Consumer getAction(String type) throws InvalidActionException;
}
```

DuxSlice can be created using the builder class provided with the library.

```java

Slice<UserProfile> slice = new DuxSliceBuilder<UserProfile>()
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
                .enableAsyncNotifications()
                .build();
```

The builder offers a variety of methodsto configure various aspects of the store used underneath the slice.
| Parameter      | Value |
| ----------- | ----------- |
| setInitialState(State state)  | set initial state of slice   |
| addReducer(String type, Reducer reducer)  | It takes a reducer which only works on a specific type of action  |
| enableAsyncNotifications() | Subscribers are notified asynchronously so if subscriber code is computation intensive it doesn't block the main thread |
| addSubscriber(Consumer subscriber) | Add a subscriber callback to slice |
| setMiddleware(Middleware middleware) | Set a middleware to slice. If you want to use multiple you can use the compose utility |


The DuxSlice also provides type safety in the sense that you will be unable to dispatch an action which does not have a reducer registered which can process it. The actions are still dispatched using types but only types which were registered at the time of creation of slice will work with the store and any other values will throw exceptions

```
Consumer setName = slice.getAction("setName");
setName.accept(newName);

Consumer setEmail = slice.getAction("updateEmail"); // throws InvalidActionException as slice does not have any reducer corresponding to that action type
```


The Slice / Store APIs also provide a mechanism for auto backup of store state to a file on disk in the form of JSON.
This backup can be enabled and its triggered when application shuts down via a JVM Runtime hook.
This helps achieve a level of inbuilt persistence on part of the Store at the same time leaving proper persistence in the hand of the application layer.


```java
 this.slice = new DuxSliceBuilder<UserProfile>()
                .setInitialState(new UserProfile("Karan Gupta", "karan@hello.com"))
                .addReducer("setEmail", (action, state) -> {
                    state.setEmail(action.getPayload().toString());
                    return state;
                })
                .enableAutoBackup("path to backup file location")
                .build();
```