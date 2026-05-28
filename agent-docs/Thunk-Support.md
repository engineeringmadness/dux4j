Thunks are actions which perform async logic before dispatching the payload to the store. It takes in as arguments the store's dispatch and getState methods. Its not compulsory that the logic be async or synchronous and Thunks are free to call the argument methods whenever.

Reducers **must not contain side effects** but real applications require logic that has side effects. Some of that may live inside the Controller, but some may need to live outside the Controller layer. Thunks give us a place to put those side effects.

In the below example we use OkHttp library to call a REST API and use the response to construct our action payload which is then dispatched to the store.
```java
// Thunk Definition

Thunk<UserProfile> updateUserFromAPI = (dispatch, getState) -> {
        Request request = new Request.Builder()
           .url("https://jsonplaceholder.typicode.com/users/1")
           .build();
         Call call = client.newCall(request);
         call.enqueue(new Callback() {
             
             public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                User user = gson.fromJson(response.body().string(), User.class);
                dispatch.accept(Utilities.actionCreator("SET_USER", new UserProfile(user.getName(), user.getEmail()));
              }

              public void onFailure(Call call, IOException e) {
                  fail();
              }
            });
        };


// Dispatching a Thunk is exactly the same as dispatching an Action

myStore.dispatch(updateUserFromAPI);

```

