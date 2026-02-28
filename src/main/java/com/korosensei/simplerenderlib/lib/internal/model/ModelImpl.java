package com.korosensei.simplerenderlib.lib.internal.model;

import net.minecraft.util.ResourceLocation;

import org.joml.Matrix4f;

import com.korosensei.simplerenderlib.lib.api.IMesh;

public class ModelImpl implements IMesh {

    private float[] mVertices;
    private float[] mNormals;
    private float[] mTexCoords;
    private Matrix4f modelMatrix;
    private ResourceLocation texture;

    public ModelImpl(ResourceLocation texture, float[] vertices, float[] mNormals, float[] mTexCoords) {
        this.texture = texture;
        this.mNormals = mNormals;
        this.mTexCoords = mTexCoords;
        this.modelMatrix = new Matrix4f().identity();
    }

    @Override
    public float[] getVertices() {
        return mVertices;
    }

    @Override
    public float[] getNormals() {
        return mNormals;
    }

    @Override
    public float[] getTexCoords() {
        return mTexCoords;
    }

    @Override
    public float[] getFaces() {
        return mNormals;
    }

}
