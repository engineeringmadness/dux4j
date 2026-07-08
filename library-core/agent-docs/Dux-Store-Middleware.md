Middlewares are functions which act as interceptor functions which can be placed before the dispatcher. These functions can run side effects, business logic or enhance the incoming actions also.
 
DuxStore only supports attaching of single middleware function to the store but for complex use cases you can use the compose function provided in the Utilities class which can combine multiple middleware definitions into a single one. This can be powerful tool as we can essentially construct a "Processing Pipeline" before the actions reach the store.

_Footgun Alert_ - It is essential that you call the next function inside the middleware for all branches of code otherwise the actions will not actually reach the dispatcher and hence the store will never be updated.

```java

public static final String ACTION_SET_NAME = "SET_NAME";
public static final String SPECIAL_INPUT = "Tom Marvolo Riddle";
public static final String TRANSFORMED_INPUT = "Lord Voldemort";

Reducer<UserProfile> reducer = (action, state) -> {
    switch (action.getType()) {
        case ACTION_SET_NAME:
            String newName = action.getPayload().toString();
            state.setName(newName);
            break;
    }
    return state;
};

Middleware<UserProfile> middleware = (store, next, action) -> {
    System.out.println(action);
    if(action.getPayload().toString().equalsIgnoreCase(SPECIAL_INPUT)) {
        Action<String> modifiedAction = Utilities.actionCreator(action.getType(), TRANSFORMED_INPUT);
        next.accept(modifiedAction);
    } else {
        next.accept(action);
    }
};

myStore = new DuxStore<>(initialState, reducer, middleware);
``` 


