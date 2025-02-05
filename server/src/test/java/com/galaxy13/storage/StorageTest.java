package com.galaxy13.storage;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class StorageTest {
    @Test
    void testWekRefStorage() {
        Storage<String, Value> storage = new WeakRefStorage<>(100);
        storage.put("key1", new Value("value1"));
        Optional<Value> value1 = storage.get("key1");

        assertThat(value1).isPresent();
        assertThat(value1.get().value()).isEqualTo("value1");

        Optional<Value> value2 = storage.remove("key1");
        assertThat(value2).isPresent();
        assertThat(value2.get().value()).isEqualTo("value1");

        Optional<Value> noValue = storage.remove("key1");
        assertThat(noValue).isNotPresent();
    }

    @Test
    void testLRUStorage() {
        Storage<String, Value> storage = new LRUStorage<>(5);
        for (int i = 0; i < 5; i++) {
            storage.put("key" + i, new Value("value" + i));
        }

        for (int i = 0; i < 5; i++) {
            Optional<Value> value = storage.get("key" + i);
            assertThat(value).isPresent();
            assertThat(value.get().value()).isEqualTo("value" + i);
        }
        storage.get("key1");
        storage.put("key6", new Value("value6"));
        assertThat(storage.get("key0")).isNotPresent();
        assertThat(storage.get("key1")).isPresent();
        assertThat(storage.get("key1").get().value()).isEqualTo("value1");
        assertThat(storage.get("key6")).isPresent();

        storage.get("key2");
        storage.get("key3");
        storage.get("key4");
        storage.get("key5");
        storage.put("key7", new Value("value7"));

        assertThat(storage.get("key6")).isPresent();
        assertThat(storage.get("key1")).isNotPresent();

        Optional<Value> value = storage.remove("key6");
        assertThat(storage.get("key6")).isNotPresent();
        assertThat(value).isPresent();
        assertThat(value.get().value()).isEqualTo("value6");
    }
}
