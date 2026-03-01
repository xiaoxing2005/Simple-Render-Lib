package com.korosensei.simplerenderlib.lib.manager;

public class RenderManager {

    public static void init() {
        ShaderManager.init();
        ModelManager.getInstance()
            .init();
    }

    public static void update() {
        ShaderManager.INSTANCE.updateUBO();
    }

}
