package com.galaxy13.network.message.response;


import com.galaxy13.network.message.MessageCode;

public interface Response<V> {
    MessageCode messageCode();

    String key();

    V value();
}
