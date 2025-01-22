package com.galaxy13.network.message.code;

public enum MessageCode {
    OK("200"),
    SUBSCRIPTION_RESPONSE("201"),
    SUBSCRIPTION_SUCCESS("202"),
    NOT_PRESENT("301"),
    UNSUPPORTED_OPERATION("302"),
    FORMAT_EXCEPTION("303"),
    SUBSCRIPTION_ERROR("304"),;

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
