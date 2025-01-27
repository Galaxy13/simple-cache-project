package com.galaxy13.network.netty.decoder;

import com.galaxy13.network.message.Operation;

public interface CacheMessage {
    Operation getOperation();

    String getParameter(String key);
}
