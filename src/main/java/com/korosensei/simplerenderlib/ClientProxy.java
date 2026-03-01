package com.korosensei.simplerenderlib;

import com.korosensei.simplerenderlib.clinet.Event.ModelLoaderEvent;
import com.korosensei.simplerenderlib.clinet.Event.ShaderRegisterEvent;
import com.korosensei.simplerenderlib.lib.manager.ModelManager;
import com.korosensei.simplerenderlib.lib.manager.ShaderManager;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import net.minecraft.util.ResourceLocation;

import static com.korosensei.simplerenderlib.SimpleRenderLib.EVENT_BUS;

public class ClientProxy extends CommonProxy {
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation("simplerenderlib", "model/PowerChair.obj");
    private static final String MODEL_ID = MODEL_LOCATION.toString();

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.
    @Override
    public void init(FMLInitializationEvent event) {
       // RenderManager.init();

        ShaderRegisterEvent shaderRegisterEvent = new ShaderRegisterEvent(ShaderManager.INSTANCE);
        EVENT_BUS.post(shaderRegisterEvent);
        ModelLoaderEvent modelLoaderEvent = new ModelLoaderEvent(ModelManager.getInstance());
        EVENT_BUS.post(modelLoaderEvent);
//        Model model = ObjModelLoader.loadModel(MODEL_LOCATION);
//        modelLoaderEvent.modelManager.registerModel(MODEL_ID, model);
//        ModelManager.getInstance().uploadToGPU();

        ClientRegistry.bindTileEntitySpecialRenderer(TilePowerChair.class, new TestTESR());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);;
    }
}
