package com.korosensei.simplerenderlib.lib.internal.render.gl.structs;

import java.nio.IntBuffer;

public class DrawArraysIndirectCommand {

    public static final int INTS = 4;
    public static final int BYTES = INTS * Integer.BYTES;

    public final int count;
    public final int instanceCount;
    public final int first;
    public final int baseInstance;

    public DrawArraysIndirectCommand(int count, int instanceCount, int first, int baseInstance) {
        if (count < 0 || instanceCount < 0 || first < 0 || baseInstance < 0) {
            throw new IllegalArgumentException("Indirect command values must be non-negative");
        }
        this.count = count;
        this.instanceCount = instanceCount;
        this.first = first;
        this.baseInstance = baseInstance;
    }

    public int[] toArray() {
        return new int[]{count, instanceCount, first, baseInstance};
    }

    public void writeTo(IntBuffer target) {
        target.put(count).put(instanceCount).put(first).put(baseInstance);
    }

    public static int[] flatten(DrawArraysIndirectCommand... commands) {
        if (commands == null || commands.length == 0) {
            return new int[0];
        }
        int[] result = new int[commands.length * INTS];
        int idx = 0;
        for (DrawArraysIndirectCommand command : commands) {
            if (command == null) {
                idx += INTS;
                continue;
            }
            result[idx++] = command.count;
            result[idx++] = command.instanceCount;
            result[idx++] = command.first;
            result[idx++] = command.baseInstance;
        }
        return result;
    }
}
