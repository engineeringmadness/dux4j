
# Welcome to the dux4j wiki!

Welcome to Dux, A unidirectional state management system written for Java. The implementation is inspired by the Flux Architecture by Facebook and Dan Abramov's library Redux.

## Get Started

### How to install it

Add the library as a maven dependency
```xml
<dependency>
    <groupId>dev.engineeringmadness</groupId>
    <artifactId>dux4j</artifactId>
    <version>2.0.0</version>
</dependency>
```

### How to use it

> Note: The Slice API is a easier and modern approach to building a Store and is recommended as the default. You are still free to use the original DuxStore / DuxStoreBuilder API

1. Create class representing your Application State
```java
@Getter
@Setter
@AllArgsConstructor
public class UserProfile implements State {
    private String name;
    private String email;

    @Override
    public UserProfile clone() {
     //...
    }
}

```
2. Create a new Store slice by passing in initial state and a Reducer function
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
                .build();

```
3. Dispatching Actions
```java
Consumer setEmail = slice.getAction("setEmail");
setEmail.accept(newEmail);
```


## Key Terminologies

**Actions**- just a POJO which specifies an event that has occurred and needs to be handled by the store. It has two mandatory fields - Type which is a string which gives metadata info on what kind of event has occured and Payload which has the data required to update the store

**State** - State is also just a POJO which represents the entire application state / a slice of state depending on how you use the DuxStore.

**Store** - Centralized location where we keep all application state. The store does not allow users to directly modify its internal state but only accepts different types of actions and updates its internal state accordingly. in DuxJava the implementation of Store is called DuxStore.

**Dispatcher** - Dispatcher is responsible for sending the actions to the Store. In Flux Architecture the Dispatcher is separate from the store but similar to Redux in DuxJava also the dispatcher is part of the store itself.

**Reducer** - A reducer is a pure function which takes an action and the current state and returns a new state which has updated itself based on the type of action supplied. DuxStore supports only a single reducer but the library comes with the classic combineReducer function which can be used to club together multiple reducers.

**Pure / Idempotent Function** - Given the same inputs, it will return the same output, regardless of the number of times a function is called.

**Side Effects** - Any operation that is not directly related to the final output of the function is called a Side Effect. E.g. of side effects would be interactions with external systems like calling a REST API, querying a database or writing to a file.

**Middleware** - Middleware is a simple function which has access to the store, dispatcher and incoming actions. It intercepts the actions, performs any business logic that may include enhancing the actions received and then forwards then over to the dispatcher.

**Thunk** - Thunk is a special type of action which is not a POJO. Instead it is a function. It gets access to the store and dispatcher and can be used to run async side effects and update the store as a result. This helps the store to manage side effects without corrupting the reducer as the reducer has to be a pure function