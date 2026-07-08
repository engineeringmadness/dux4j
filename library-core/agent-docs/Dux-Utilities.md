Inspired by Redux we also offer some helpful utilities as part of the library to help with some of the more complex use cases

1. **ActionCreator**- This function allows you to outsource the creation of a an action object and gives you a generator function that does it for you. This allows you to dispatch the payload directly without having to do the intermediate step of creating an action object.

```java
myStore.dispatch(Utilities.actionCreator(ACTION_SET_NAME, "Hello there"));
```

2. **CombineReduce**- DuxStore only supports one reducer to be attached which is a limitation. This means that for complex applications we could have reducers with a massive ugly switch case statement which has an impact on code readability and maintainability. To solve this this utility function can be used as it takes multiple reducer functions as arguments and returns a single reducer which combines the functionality of all the input reducers. 

    In below example we have a reducer for updating the author and another reducer for updating the title of a book - 
```java
 Reducer<CombinedState> authorReducer = (action, state) -> {
    switch (action.getType()) {
        case ACTION_SET_AUTHOR:
            Author author = (Author) action.getPayload();
            state.setAuthor(author);
            break;
    }
    return state;
};

Reducer<CombinedState> bookReducer = (action, state) -> {
    switch (action.getType()) {
        case ACTION_SET_BOOK:
            Book book = (Book) action.getPayload();
            state.setBook(book);
            break;
    }
    return state;
};

Reducer<CombinedState> reducer = Utilities.combineReducer(authorReducer, bookReducer);

```

3. **Compose**- DuxStore also only supports attaching of one middleware function to the store. To overcome this limitation we provide a function similar to combineReducer which takes in multiple middleware functions and returns a single Middleware containing all the functionality of the input middlewares.

```java
Middleware<UserProfile> middleware1 = (store, next, action) -> {
    System.out.println("Old State: " + store.getState());
    next.accept(action);
};

Middleware<UserProfile> middleware2 = (store, next, action) -> {
    next.accept(action);
    System.out.println("New State: " + store.getState());
};

Middleware<UserProfile> combined = Utilities.compose(middleware1, middleware2);
```