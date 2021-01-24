package de.tu_berlin.cit.yahoo_benchmark.flink;

public class Window {
    public String timestamp;
    public Long seenCount;

    @Override
    public boolean equals(Object other) {
        if(other instanceof Window) {
            return timestamp.equals(((Window)other).timestamp);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = result * prime + timestamp.hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "{ time: " + timestamp + ", seen: " + seenCount + " }";
    }
}
