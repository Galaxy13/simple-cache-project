package com.galaxy13.network.message;

public enum MessageCode {
    OK("200"),
    SUBSCRIPTION_RESPONSE("201"),
    SUBSCRIPTION_SUCCESS("202"),
    NOT_PRESENT("301"),
    UNSUPPORTED_OPERATION("302"),
    FORMAT_EXCEPTION("303");

    private final String label;

    MessageCode(String label) {
        this.label = label;
    }

    public String code(){
        return label;
    }
}
