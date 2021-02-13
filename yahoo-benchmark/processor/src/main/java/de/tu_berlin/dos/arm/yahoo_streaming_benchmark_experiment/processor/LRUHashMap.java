package de.tu_berlin.dos.arm.yahoo_streaming_benchmark_experiment.processor;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUHashMap<K, V> extends LinkedHashMap<K, V> {
    private int cacheSize;

    public LRUHashMap(int cacheSize) {
        super(16, 0.75f, true);
        this.cacheSize = cacheSize;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= cacheSize;
    }
}
