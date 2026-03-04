package com.korosensei.simplerenderlib;

import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;
import com.korosensei.simplerenderlib.lib.manager.RenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.korosensei.simplerenderlib.lib.internal.model.Model;
import com.korosensei.simplerenderlib.lib.internal.model.loader.ObjModelLoader;
import com.korosensei.simplerenderlib.lib.internal.render.renderer.ModelRenderer;
import com.korosensei.simplerenderlib.lib.manager.ModelManager;
import com.korosensei.simplerenderlib.lib.manager.ShaderManager;
import org.lwjgl.opengl.GL32;

public class TestTESR extends TileEntitySpecialRenderer {

    private boolean initialized = false;

    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(
        "simplerenderlib",
        "model/test.obj");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(
        "simplerenderlib",
        "model/test.png");
    private final ShaderProgram shader = new ShaderProgram(
        "simplerenderlib:shader/SpecialShader.vert",
        "simplerenderlib:shader/SpecialShader.frag");
    private static final String MODEL_ID = MODEL_LOCATION.toString();

    public TestTESR() {
        // No need to listen to RenderWorldEvent.Pre for UBO updates
    }

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!initialized) {
            initRenderData();
        }

        int currentLight = tile.getWorldObj()
            .getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord + 1, tile.zCoord, 0);

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);

        ModelRenderer renderer = ModelRenderer.getInstance();
        RenderManager.INSTANCE.updateDepth();
        renderer.setSpecialShader(shader);
        //RenderManager.INSTANCE.updateMVPMatrices();
        // 1. 开启全局渲染环境（绑定 Shader 和 VBO）
        renderer.begin();

        // 2. 配置当前模型的变换矩阵
        renderer.pushMatrix();
        renderer.translate((float) x + 0.5f, (float) y, (float) z + 0.5f);
//        renderer.scale(0.0625f, 0.0625f, 0.0625f);
        // 3. 配置纹理和光照状态
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TEXTURE_LOCATION);
        renderer.setUniformBrightness(currentLight);

        // 4. 执行极速渲染，只传一个 ID 即可！
        renderer.renderModel(MODEL_ID);

        renderer.popMatrix();

        // 5. 结束并清理环境
        renderer.end();

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void initRenderData() {
        try {
            // 获取 ModelManager 并确保初始化了 VBO
            RenderManager.initialize();
            ModelManager manager = ModelManager.getInstance();
            // 解析模型
            Model model = ObjModelLoader.loadModel(MODEL_LOCATION);

            // 注册到巨型 VBO 缓冲区中
            manager.registerModel(MODEL_ID, model);

            // 一次性上传到显存（实际开发中，这个动作可以在游戏启动阶段统一调用一次）
            manager.uploadToGPU();

        } catch (Exception e) {
            e.printStackTrace();
        }

        initialized = true;
    }
}
