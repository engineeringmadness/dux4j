package org.flux.store.api.exceptions;

public class TimeTravelExceededException extends RuntimeException {

    public TimeTravelExceededException(String message) {super(message);}

    public TimeTravelExceededException(Throwable ex) {super(ex);}

    public TimeTravelExceededException(String message, Throwable ex) {super(message, ex);}
}
