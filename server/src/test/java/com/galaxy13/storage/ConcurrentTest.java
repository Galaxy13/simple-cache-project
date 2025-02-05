package com.galaxy13.storage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

class ConcurrentTest {
    private static Storage<String, Value> storage;
    private static ExecutorService executor;
    @BeforeAll
    static void init() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @BeforeEach
    void setUp() {
        storage = new LRUStorage<>(5);
    }

    @AfterAll
    static void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void putAndGet() throws InterruptedException, ExecutionException {
        Future<?> putFuture = executor.submit(() -> {
            for (int i = 0; i < 100_000; i++) {
                storage.put("key" + i, new Value("value" + i));
            }
        });

        Future<?> getFuture = executor.submit(() -> {
            for (int i = 0; i < 100_000; i++) {
                storage.get("key" + i);
            }
        });
        putFuture.get();
        getFuture.get();

        assertThat(getFuture).isDone();
        assertThat(putFuture).isDone();

        assertThat(storage.size()).isEqualTo(5);
        assertThat(storage.get("key99999")).isPresent();
    }

    @Test
    void putAndRemove() throws InterruptedException, ExecutionException {
        Future<?> putFuture = executor.submit(() -> {
            for (int i = 0; i < 100_000; i++) {
                storage.put("key" + i, new Value("value" + i));
            }
        });

        Future<?> getFuture = executor.submit(() -> {
            for (int i = 0; i < 100_000; i++) {
                storage.remove("key" + i);
            }
        });

        putFuture.get();
        getFuture.get();

        assertThat(getFuture).isDone();
        assertThat(putFuture).isDone();
    }
}
