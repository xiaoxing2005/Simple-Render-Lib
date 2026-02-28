package com.korosensei.simplerenderlib.lib.internal.render.gl.structs;

import java.util.Arrays;

public final class FloatCollector {
    private float[] data;
    private int size;

    public FloatCollector(int initialCapacity) {
        this.data = new float[Math.max(16, initialCapacity)];
    }

    public void addAll(float[] values) {
        ensureCapacity(size + values.length);
        System.arraycopy(values, 0, data, size, values.length);
        size += values.length;
    }

    public void replaceWith(float[] values) {
        clear();
        addAll(values);
    }

    public void clear() {
        size = 0;
    }

    public int size() {
        return size;
    }

    public float[] toArray() {
        return Arrays.copyOf(data, size);
    }

    public int copyTo(float[] target, int offset) {
        System.arraycopy(data, 0, target, offset, size);
        return offset + size;
    }

    private void ensureCapacity(int minCapacity) {
        if (data.length >= minCapacity) {
            return;
        }
        int newCap = data.length;
        while (newCap < minCapacity) {
            newCap <<= 1;
        }
        data = Arrays.copyOf(data, newCap);
    }
}
