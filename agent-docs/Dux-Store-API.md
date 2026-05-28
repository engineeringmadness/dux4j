## Store Interface

Below are the interfaces that define the structure used to interact with dux4j

![Screenshot from 2023-05-20 14-12-01](https://github.com/compscikaran/dux4j/assets/15171039/bfd4417d-2def-432a-b34b-7016c2469547)


_dispatch_ is the main method on the store which allows users to interact with it by sending actions to it. Actions can be either POJO (Action object) or in the form of a functional interface Thunk.

_getState_ gives you a view into the current state of the store

_subscribe_ lets us attach callbacks to the store which get triggered whenever the internal state of the store has changed

_replaceReducer_ As the name suggests it lets you replace the reducer function implementation of an existing store

## DuxStore

Firstly we need to define the model representing our application state. our Application State class has to implement the State interface provided by DuxJava

```java
import org.flux.store.api.State;

public class UserProfile implements State {
    private String name;
    private String email;

    @Override
    public UserProfile clone() {
        // clone method implementation
    }

   // getters and setters
}

```
Once we have our application state model setup then we need to define our reducer function

```java
Reducer<UserProfile> reducer = (action, state) -> {
    switch (action.getType()) {
        case "SET_EMAIL":
            String newEmail = action.getPayload().toString();
            state.setEmail(newEmail);
            break;
        case "SET_NAME":
            String newName = action.getPayload().toString();
            state.setName(newName);
            break;
   }
   return state;
};
```
The reducer has few interesting characteristics. Firstly similar to Redux, we implement it using a switch case statement on the action type attribute. The reducer must return the new state otherwise the store will not function properly. 

The reducer takes in action and state. Based on action type we are modifying the state object !? 🤔 (I thought reducers are pure functions...). The DuxStore internally creates a clone of the current state and passes that in to the reducer instead of directly passing in the current state itself.

Next we need to come up with an initial state for our state to be used as a starting point
```java
UserProfile initialState = new UserProfile("", "");
```
Now that we have our key components we can create our state
```java
DuxStore<UserProfile> myStore = new DuxStore<>(initialState, reducer);
```

## Dispatching Actions

To dispatch actions the DuxStore conviniently provides a dispatch method which accepts an Action Object. E.g. if we want to update the email attribute in our store -
```java
String newEmail = "hello@gmail.com";
Action<String> action = new Action<>("SET_EMAIL", newEmail);

myStore.dispatch(action);
```

## Adding Subscribers

We can optionally attach callbacks which get triggered whenever the state of our store changes. It receives the updated state as an argument. E.g. lets attach a simple logging statement as a subscriber to our store
```java
myStore.subscribe(x -> {
    System.out.println("State has changed..." + x);
});
```

By default the subscribers are notified of state changes in async manner which are implemented using the CompletableFuture API.