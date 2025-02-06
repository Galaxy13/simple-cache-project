package com.galaxy13.controller;

import com.galaxy13.mapping.FirstValue;
import com.galaxy13.storage.CacheComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController {
    private final CacheComponent cacheComponent;

    public ExampleController(CacheComponent cacheComponent) {
        this.cacheComponent = cacheComponent;
    }

    @GetMapping("/data")
    public int getValue() {
        String value = cacheComponent.get("test");
        if (value != null) {
            return Integer.parseInt(value);
        }
        return 0;
    }

    @PostMapping("/firstValue")
    public int putValue(@RequestBody FirstValue firstValue) {
        cacheComponent.put("test", firstValue.getValue());
        return 1;
    }
}
