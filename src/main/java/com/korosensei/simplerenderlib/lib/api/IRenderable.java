package com.korosensei.simplerenderlib.lib.api;

import com.korosensei.simplerenderlib.lib.internal.model.IMesh;
import org.joml.Matrix4f;



public interface IRenderable {

    IMesh getMesh();

    Matrix4f getWorldMatrix();

}
