package com.mcmodloader.core.artifact;

import java.util.concurrent.atomic.AtomicInteger;

public final class MinecraftNetworkRequestCounter {
    private final AtomicInteger requestCount = new AtomicInteger();

    public int incrementAndGet() {
        return requestCount.incrementAndGet();
    }

    public int requestCount() {
        return requestCount.get();
    }
}
