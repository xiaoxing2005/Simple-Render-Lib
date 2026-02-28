package com.korosensei.simplerenderlib.lib.api;

import org.joml.Matrix4f;

public interface IRenderable {

    IMesh getMesh();

    Matrix4f getWorldMatrix();

}
