package com.korosensei.simplerenderlib.lib.manager;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject.createUBO;

public class RenderManager {
    public static RenderManager INSTANCE;
    private final BufferObject UBO;
    private final FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer sizeBuffer = BufferUtils.createFloatBuffer(3);
    private int depthTextureID = GL11.glGenTextures();
    private int width = 0;
    private int height = 0;

    private RenderManager() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        UBO = createUBO(GL15.GL_DYNAMIC_DRAW);
        UBO.Bind();
        GL15.glBufferData(GL31.GL_UNIFORM_BUFFER, 140, GL15.GL_DYNAMIC_DRAW);
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, 0, UBO.ID);
        UBO.unBind();

    }

    public static void initialize() {
        INSTANCE = new RenderManager();
        ShaderManager.init();
        ModelManager.getInstance()
            .init();
    }

    public void updateMVPMatrices() {
        projectionMatrixBuffer.clear();
        viewMatrixBuffer.clear();
        sizeBuffer.clear();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrixBuffer);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, viewMatrixBuffer);
        sizeBuffer.put(new float[]{Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight,Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16 * 2});
        sizeBuffer.rewind();

        projectionMatrixBuffer.rewind();
        viewMatrixBuffer.rewind();
        UBO.Bind();
        // std140 mat4 在内存中占据 64 bytes
        GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, 0, projectionMatrixBuffer);
        GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, 64, viewMatrixBuffer);
        GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, 128, sizeBuffer);
        UBO.unBind();
    }
    @SubscribeEvent
    public void update(RenderWorldLastEvent event) {
        updateMVPMatrices();
       // if (OpenGlHelper.isFramebufferEnabled()) Minecraft.getMinecraft().getFramebuffer().unbindFramebuffer();
    }

    public void updateDepth() {
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTextureID);
        if (Minecraft.getMinecraft().displayWidth != width || Minecraft.getMinecraft().displayHeight != height) {
            width = Minecraft.getMinecraft().displayWidth;
            height = Minecraft.getMinecraft().displayHeight;
            GL11.glDeleteTextures(depthTextureID);
            depthTextureID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTextureID);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT32F, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }

}
