DuxStore also has an amazing functionality which is that it uses the unique redux architecture to provide point in time restore functionality.
Consider a scenario where you are debugging a complex application and it would be great it you could go backwards or forwards in the sequence of events by simply executing a command. This is exactly what is offered by Dux.

This functionality is made possible by storing all the actions received by the store and using those to reconstruct the store state at moment in time.

```java
myStore.goBack();

myStore.goForward();
```