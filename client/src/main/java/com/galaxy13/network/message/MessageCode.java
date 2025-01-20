package com.galaxy13.network.message;

public enum MessageCode {
    OK("200"),
    SUBSCRIPTION_RESPONSE("201"),
    SUBSCRIPTION_SUCCESS("202"),
    NOT_PRESENTED("301"),
    UNSUPPORTED_OPERATION("302");

    private final String code;

    MessageCode(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static MessageCode fromString(String code) {
        for (MessageCode msgCode : MessageCode.values()) {
            if (msgCode.code.equals(code)) {
                return msgCode;
            }
        }
        return null;
    }
}
