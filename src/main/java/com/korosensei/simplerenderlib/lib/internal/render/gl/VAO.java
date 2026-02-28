package com.korosensei.simplerenderlib.lib.internal.render.gl;

import org.lwjgl.opengl.GL30;

public abstract class VAO implements AutoCloseable {

    public final int id;
    private boolean initialized;
    private boolean deleted;

    public VAO() {
        this.id = GL30.glGenVertexArrays();
    }

    public void Bind() {
        ensureAlive();
        GL30.glBindVertexArray(id);
    }

    public void Delete() {
        if (deleted) {
            return;
        }
        GL30.glDeleteVertexArrays(id);
        deleted = true;
    }

    public void Unbind() {
        GL30.glBindVertexArray(0);
    }

    public abstract void initVertexAttrib(BufferObject vbo);

    public void init(BufferObject vbo) {
        if (initialized) {
            return;
        }
        vbo.Bind();
        Bind();
        initVertexAttrib(vbo);
        Unbind();
        vbo.unBind();
        initialized = true;
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
