package com.galaxy13.network.message.response;


import com.galaxy13.network.message.MessageCode;

import java.util.Map;

@SuppressWarnings({"unused"})
public interface Response<K, V> {
    MessageCode messageCode();

    V getParameter(K name);

    Map<K, V> getParameters();
}
