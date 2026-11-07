package dev.admutium.techmod.cable;

import java.util.function.Supplier;

/** Small instance-owned cache; cleared by data/neighbor changes, never retained globally. */
public final class CableShapeCache<T> {
    private T value;
    public synchronized T get(Supplier<T> factory) {
        if (value==null) value=factory.get();
        return value;
    }
    public synchronized void invalidate() { value=null; }
}
