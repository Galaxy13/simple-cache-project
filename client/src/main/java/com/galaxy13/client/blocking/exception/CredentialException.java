package com.galaxy13.client.blocking.exception;

import com.galaxy13.network.netty.auth.Credentials;

public class CredentialException extends RuntimeException {
    public CredentialException(Credentials credentials) {
        super("Bad credentials provided: " + credentials);
    }
}
