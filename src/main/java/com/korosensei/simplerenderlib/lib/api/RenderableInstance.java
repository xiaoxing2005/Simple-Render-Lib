package com.korosensei.simplerenderlib.lib.api;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.korosensei.simplerenderlib.lib.internal.model.IMesh;

public class RenderableInstance implements IRenderable {

    private final IMesh mesh;
    private Matrix4f worldMatrix;

    public RenderableInstance(IMesh mesh) {
        this.mesh = mesh;
        this.worldMatrix = new Matrix4f().identity();
    }

    public RenderableInstance(IMesh mesh, Matrix4f worldMatrix) {
        this.mesh = mesh;
        this.worldMatrix = worldMatrix;
    }

    public RenderableInstance(IMesh mesh, Vector3f translate) {
        this.mesh = mesh;
        this.worldMatrix = new Matrix4f().identity()
            .translate(translate);
    }

    @Override
    public IMesh getMesh() {
        return this.mesh;
    }

    @Override
    public Matrix4f getWorldMatrix() {
        return this.worldMatrix;
    }

    public void setWorldMatrix(Matrix4f worldMatrix) {
        this.worldMatrix = worldMatrix;
    }
}
