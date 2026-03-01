package com.korosensei.simplerenderlib.clinet.Event;

import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;
import com.korosensei.simplerenderlib.lib.manager.ShaderManager;
import cpw.mods.fml.common.eventhandler.Event;

import java.util.ArrayList;

public class ShaderRegisterEvent extends Event {

    public ShaderManager shaderManager;
    public ShaderRegisterEvent(ShaderManager shaderManager) {
        this.shaderManager = shaderManager;
    }
    public void registerShader(ShaderManager shaderManager) {

    }

}
