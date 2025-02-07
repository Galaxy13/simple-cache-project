package com.galaxy13.network.netty.auth;

public class Credentials {
    private final String login;
    private final String password;
    private String token;

    public Credentials(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean containsToken() {
        return token != null;
    }

    @Override
    public String toString() {
        return "Credentials [login=" + login + ", password=" + password + ", token=" + token + "]";
    }
}
