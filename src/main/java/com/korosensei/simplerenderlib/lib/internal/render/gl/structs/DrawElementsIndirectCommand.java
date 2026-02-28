package com.korosensei.simplerenderlib.lib.internal.render.gl.structs;

import java.nio.IntBuffer;

public class DrawElementsIndirectCommand {

    public static final int INTS = 5;
    public static final int BYTES = INTS * Integer.BYTES;

    public final int count;
    public final int instanceCount;
    public final int firstIndex;
    public final int baseVertex;
    public final int baseInstance;

    public DrawElementsIndirectCommand(int count, int instanceCount, int firstIndex, int baseVertex, int baseInstance) {
        if (count < 0 || instanceCount < 0 || firstIndex < 0 || baseVertex < 0 || baseInstance < 0) {
            throw new IllegalArgumentException("Indirect command values must be non-negative");
        }
        this.count = count;
        this.instanceCount = instanceCount;
        this.firstIndex = firstIndex;
        this.baseVertex = baseVertex;
        this.baseInstance = baseInstance;
    }

    public int[] toArray() {
        return new int[] { count, instanceCount, firstIndex, baseVertex, baseInstance };
    }

    public void writeTo(IntBuffer target) {
        target.put(count)
            .put(instanceCount)
            .put(firstIndex)
            .put(baseVertex)
            .put(baseInstance);
    }

    public static int[] flatten(DrawElementsIndirectCommand... commands) {
        if (commands == null || commands.length == 0) {
            return new int[0];
        }
        int[] result = new int[commands.length * INTS];
        int idx = 0;
        for (DrawElementsIndirectCommand command : commands) {
            if (command == null) {
                idx += INTS;
                continue;
            }
            result[idx++] = command.count;
            result[idx++] = command.instanceCount;
            result[idx++] = command.firstIndex;
            result[idx++] = command.baseVertex;
            result[idx++] = command.baseInstance;
        }
        return result;
    }
}
