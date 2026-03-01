package com.korosensei.simplerenderlib.lib.manager;

import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;

import java.util.ArrayList;

public class RenderManager {

    public static void init() {
        ShaderManager.init();
        ModelManager.getInstance().init();
    }

    public static void update() {
        ShaderManager.INSTANCE.updateUBO();
    }

}
