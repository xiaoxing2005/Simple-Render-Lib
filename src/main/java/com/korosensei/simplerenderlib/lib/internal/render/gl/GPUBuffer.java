package com.korosensei.simplerenderlib.lib.internal.render.gl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public abstract class GPUBuffer implements AutoCloseable {

    public final int ID;
    public final int drawType;
    private final int target;
    private boolean deleted;
    private FloatBuffer floatStage;
    private IntBuffer intStage;

    protected GPUBuffer(int target, int drawType) {
        this.target = target;
        this.drawType = drawType;
        this.ID = GL15.glGenBuffers();
    }

    public final int bufferType() {
        return target;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void Bind() {
        ensureAlive();
        GL15.glBindBuffer(target, ID);
    }

    public void bind() {
        Bind();
    }

    public void unBind() {
        GL15.glBindBuffer(target, 0);
    }

    public void unbind() {
        unBind();
    }

    public void bindBase(int bindingPoint) {
        ensureAlive();
        GL30.glBindBufferBase(target, bindingPoint, ID);
    }

    protected final void upload(FloatBuffer data) {
        ensureAlive();
        Bind();
        GL15.glBufferData(target, data, drawType);
        unBind();
    }

    protected final void upload(IntBuffer data) {
        ensureAlive();
        Bind();
        GL15.glBufferData(target, data, drawType);
        unBind();
    }

    protected final void allocate(long bytes) {
        ensureAlive();
        Bind();
        GL15.glBufferData(target, bytes, drawType);
        unBind();
    }

    protected final FloatBuffer stageFloats(int count) {
        if (count <= 0) {
            return null;
        }
        if (floatStage == null || floatStage.capacity() < count) {
            floatStage = BufferUtils.createFloatBuffer(nextPow2(count));
        }
        floatStage.clear();
        return floatStage;
    }

    protected final IntBuffer stageInts(int count) {
        if (count <= 0) {
            return null;
        }
        if (intStage == null || intStage.capacity() < count) {
            intStage = BufferUtils.createIntBuffer(nextPow2(count));
        }
        intStage.clear();
        return intStage;
    }

    protected final void clearFloatStage() {
        if (floatStage != null) {
            floatStage.clear();
        }
    }

    protected final void clearIntStage() {
        if (intStage != null) {
            intStage.clear();
        }
    }

    public void Delete() {
        if (deleted) {
            return;
        }
        GL15.glDeleteBuffers(ID);
        deleted = true;
    }

    public void delete() {
        Delete();
    }

    @Override
    public void close() {
        Delete();
    }

    protected final void ensureAlive() {
        if (deleted) {
            throw new IllegalStateException("Buffer " + ID + " has been deleted");
        }
    }

    private static int nextPow2(int value) {
        int v = value <= 1 ? 1 : value - 1;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        return v + 1;
    }
}
