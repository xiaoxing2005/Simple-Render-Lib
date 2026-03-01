package com.korosensei.simplerenderlib.clinet.Event;

import com.korosensei.simplerenderlib.lib.manager.ShaderManager;

import cpw.mods.fml.common.eventhandler.Event;

public class ShaderRegisterEvent extends Event {

    public ShaderManager shaderManager;

    public ShaderRegisterEvent(ShaderManager shaderManager) {
        this.shaderManager = shaderManager;
    }

}
