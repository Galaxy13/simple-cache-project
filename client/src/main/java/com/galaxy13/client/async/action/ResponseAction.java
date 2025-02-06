package com.galaxy13.client.async.action;

import com.galaxy13.network.message.Response;

import java.io.IOException;

public interface ResponseAction {
    void action(Response response);
}
