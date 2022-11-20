package ru.set404.AdsMetrika.exceptions;

public class OAuthCredentialEmptyException extends RuntimeException{
    String message;

    public OAuthCredentialEmptyException(String message) {
        super(message);
        this.message = message;
    }
}
