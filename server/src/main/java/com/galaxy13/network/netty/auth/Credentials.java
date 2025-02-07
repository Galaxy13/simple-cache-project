package com.galaxy13.network.netty.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Credentials {
    private static final Logger logger = LoggerFactory.getLogger(Credentials.class);

    private final String login;
    private final String password;

    private final boolean omitCredentials;

    private final Set<String> tokens;

    public Credentials(String login, String password) {
        this.login = login;
        this.password = password;
        tokens = Collections.newSetFromMap(new ConcurrentHashMap<>());
        if (login == null || password == null) {
            logger.warn("No login or password provided. Credentials will be ignored.");
            this.omitCredentials = true;
        } else {
            this.omitCredentials = false;
        }
    }

    public boolean checkCredentials(String login, String password) {
        if (omitCredentials) {
            return true;
        }
        return this.login.equals(login) && this.password.equals(password);
    }

    public void addToken(String token) {
        if (omitCredentials) {
            logger.warn("Trying to add token when credentials are not set.");
            return;
        }
        tokens.add(token);
    }

    public boolean containsToken(String token) {
        if (omitCredentials) {
            logger.warn("Token check ignored. Cause: credentials are not set.");
            return true;
        }
        return tokens.contains(token);
    }
}
