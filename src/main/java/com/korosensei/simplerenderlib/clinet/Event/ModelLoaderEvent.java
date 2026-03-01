package com.korosensei.simplerenderlib.clinet.Event;

import com.korosensei.simplerenderlib.lib.manager.ModelManager;

import cpw.mods.fml.common.eventhandler.Event;

public class ModelLoaderEvent extends Event {

    public final ModelManager modelManager;

    public ModelLoaderEvent(ModelManager modelManager) {
        this.modelManager = modelManager;
    }
}
