package org.flux.store.api.exceptions;

public class AsyncNotificationException extends Exception{
    public AsyncNotificationException(String message) {super(message);}

    public AsyncNotificationException(Throwable ex) {super(ex);}

    public AsyncNotificationException(String message, Throwable ex) {super(message, ex);}
}
