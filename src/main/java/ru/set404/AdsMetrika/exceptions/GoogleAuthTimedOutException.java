package ru.set404.AdsMetrika.exceptions;

public class GoogleAuthTimedOutException extends RuntimeException{
    String message;

    public GoogleAuthTimedOutException(String message) {
        super(message);
        this.message = message;
    }
}
