package com.galaxy13.controller;

import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.storage.CacheController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {
    private final CacheController cacheController;

    public ExampleController(CacheController cacheController) {
        this.cacheController = cacheController;
    }

    @GetMapping("/data")
    public int getValue() {
        String value = cacheController.get("test");
        if (value != null) {
            return Integer.parseInt(value);
        }
        return 0;
    }
}
