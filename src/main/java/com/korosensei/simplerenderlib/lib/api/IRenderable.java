package com.korosensei.simplerenderlib.lib.api;

import org.joml.Matrix4f;

import com.korosensei.simplerenderlib.lib.internal.model.IMesh;

public interface IRenderable {

    IMesh getMesh();

    Matrix4f getWorldMatrix();

}
