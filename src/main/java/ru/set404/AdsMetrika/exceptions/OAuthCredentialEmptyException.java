package ru.set404.AdsMetrika.exceptions;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;

public class OAuthCredentialEmptyException extends RuntimeException{
    String message;

    public OAuthCredentialEmptyException(String message) {
        super(message);
        this.message = message;
    }
}
