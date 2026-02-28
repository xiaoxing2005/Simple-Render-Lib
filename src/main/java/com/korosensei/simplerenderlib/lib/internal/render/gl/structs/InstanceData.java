package com.korosensei.simplerenderlib.lib.internal.render.gl.structs;

import java.nio.FloatBuffer;
import java.util.Arrays;

public final class InstanceData {

    private static final float[] EMPTY = new float[0];
    private final float[] values;

    public InstanceData(float... values) {
        if (values == null || values.length == 0) {
            this.values = EMPTY;
            return;
        }
        this.values = Arrays.copyOf(values, values.length);
    }

    public static InstanceData of(float... values) {
        return new InstanceData(values);
    }

    public int size() {
        return values.length;
    }

    public int bytes() {
        return values.length * Float.BYTES;
    }

    public float get(int index) {
        return values[index];
    }

    public void writeTo(FloatBuffer target) {
        target.put(values);
    }

    public float[] toArray() {
        return Arrays.copyOf(values, values.length);
    }

    public static float[] flatten(InstanceData... instances) {
        if (instances == null || instances.length == 0) {
            return new float[0];
        }

        int total = totalFloats(instances);
        float[] result = new float[total];
        int idx = 0;
        for (InstanceData instance : instances) {
            if (instance == null || instance.values.length == 0) {
                continue;
            }
            System.arraycopy(instance.values, 0, result, idx, instance.values.length);
            idx += instance.values.length;
        }
        return result;
    }

    public static int totalFloats(InstanceData... instances) {
        if (instances == null || instances.length == 0) {
            return 0;
        }
        int total = 0;
        for (InstanceData instance : instances) {
            if (instance != null) {
                total += instance.values.length;
            }
        }
        return total;
    }
}
