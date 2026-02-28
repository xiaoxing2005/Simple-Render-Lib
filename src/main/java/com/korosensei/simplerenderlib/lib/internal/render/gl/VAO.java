package com.korosensei.simplerenderlib.lib.internal.render.gl;

import org.lwjgl.opengl.GL30;

import java.util.Objects;

public abstract class VAO implements AutoCloseable {

    public final int id;
    private final BufferObject VBO;
    private boolean initialized;
    private boolean deleted;

    public VAO(BufferObject VBO) {
        this.VBO = Objects.requireNonNull(VBO, "VBO must not be null");
        this.id = GL30.glGenVertexArrays();
    }

    public void Bind() {
        ensureAlive();
        GL30.glBindVertexArray(id);
    }

    public void bind() {
        Bind();
    }

    public void Delete() {
        if (deleted) {
            return;
        }
        GL30.glDeleteVertexArrays(id);
        deleted = true;
    }

    public void delete() {
        Delete();
    }

    public void Unbind() {
        GL30.glBindVertexArray(0);
    }

    public void unbind() {
        Unbind();
    }

    public abstract void BindVertexAttrib(BufferObject VBO);

    public VAO init() {
        if (initialized) {
            return this;
        }
        Bind();
        BindVertexAttrib(VBO);
        Unbind();
        initialized = true;
        return this;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void close() {
        Delete();
    }

    private void ensureAlive() {
        if (deleted) {
            throw new IllegalStateException("VAO " + id + " has been deleted");
        }
    }
}
