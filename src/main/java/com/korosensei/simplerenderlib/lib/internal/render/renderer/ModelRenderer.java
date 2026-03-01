package com.korosensei.simplerenderlib.lib.internal.render.renderer;

import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;
import com.korosensei.simplerenderlib.lib.manager.ModelManager;
import com.korosensei.simplerenderlib.lib.manager.ShaderManager;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

/**
 * 专为 ModelManager 巨型 VBO 架构设计的高性能静态渲染器。
 * 不再持有任何自身的顶点缓存和 VAO，纯粹负责：
 * 1. 矩阵计算与上传
 * 2. 状态切换与 Shader 挂载
 * 3. 触发巨型 VBO 上的局部绘制指令 (Draw Call)
 */
public class ModelRenderer {

    private static final ModelRenderer INSTANCE = new ModelRenderer();

    // 唯一的超级着色器
    private ShaderProgram shaderUniversal;

    // 模型矩阵堆栈
    private final Matrix4fStack matrixStack;

    // 预分配的缓冲区，避免 GC 压力
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);

    // 全局光照 Uniform，默认满亮度
    private int uniformBrightness = 240;

    private ModelRenderer() {
        this.matrixStack = new Matrix4fStack(16);
        this.matrixStack.identity();
    }

    public static ModelRenderer getInstance() {
        return INSTANCE;
    }

    /**
     * 获取通用的超级着色器，支持延迟初始化
     */
    public ShaderProgram getShader() {
        if (shaderUniversal == null || !shaderUniversal.isValid()) {
            shaderUniversal = new ShaderProgram("simplerenderlib:shader/universal.vert", "simplerenderlib:shader/universal.frag");
            //ShaderManager.INSTANCE.registerShader(shaderUniversal);
        }
        return shaderUniversal;
    }

    // ==========================================
    // 矩阵操作接口
    // ==========================================

    public void pushMatrix() {
        matrixStack.pushMatrix();
    }

    public void popMatrix() {
        matrixStack.popMatrix();
    }

    public void translate(float x, float y, float z) {
        matrixStack.translate(x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        matrixStack.rotate(angle, x, y, z);
    }

    public void scale(float x, float y, float z) {
        matrixStack.scale(x, y, z);
    }

    public void multMatrix(Matrix4f matrix) {
        matrixStack.mul(matrix);
    }

    public void clearMatrices() {
        matrixStack.clear();
        matrixStack.identity();
    }

    // ==========================================
    // 状态配置
    // ==========================================

    /**
     * 设置全局统一的光照亮度（对于没有自带光照数据的模型生效）
     */
    public void setUniformBrightness(int brightness) {
        this.uniformBrightness = brightness;
    }

    /**
     * 准备渲染环境：绑定超级 Shader、同步 OpenGL 的投影和视图矩阵，并绑定巨型 VAO。
     * 在开始连续渲染多个模型前，只需调用一次此方法。
     */
    public void begin() {
        ShaderProgram shader = getShader();
        if (shader == null) return;

        shader.use();

        // 每次渲染实体前，更新一次 UBO 以确保获取到正确的投影和视图矩阵
        ShaderManager.INSTANCE.updateUBO();

        // 配置纹理单元
        int baseTexLoc = GL20.glGetUniformLocation(shader.getProgramID(), "baseTexture");
        if (baseTexLoc >= 0) GL20.glUniform1i(baseTexLoc, 0);
        int lightTexLoc = GL20.glGetUniformLocation(shader.getProgramID(), "lightmapTexture");
        if (lightTexLoc >= 0) GL20.glUniform1i(lightTexLoc, 1);

        // 绑定 ModelManager 提供的巨型 VAO 和 EBO
        ModelManager.getInstance().bind();
    }

    /**
     * 渲染一个已经在 ModelManager 中注册的模型的特定分组。
     * 如果传入的分组名为 null，则渲染该模型的全部内容（如果模型不支持分组则回退到 glDrawArrays）。
     *
     * @param modelIdentifier 模型的唯一标识符
     * @param groupName       要渲染的分组名称（传入 null 以渲染全部）
     */
    public void renderModel(String modelIdentifier, String groupName) {
        ModelManager.ModelAllocation alloc = ModelManager.getInstance().getAllocation(modelIdentifier);
        if (alloc == null) {
            return;
        }

        ShaderProgram shader = getShader();
        if (shader == null) return;

        // 1. 上传当前栈顶的模型矩阵
        int modelLoc = GL20.glGetUniformLocation(shader.getProgramID(), "modelMatrix");
        if (modelLoc >= 0) {
            matrixBuffer.clear();
            matrixStack.get(matrixBuffer);
            GL20.glUniformMatrix4(modelLoc, false, matrixBuffer);
        }

        // 2. 上传全局亮度
        int brightLoc = GL20.glGetUniformLocation(shader.getProgramID(), "uBrightness");
        if (brightLoc >= 0) {
            GL20.glUniform1i(brightLoc, uniformBrightness);
        }

        // 3. 执行极速局部绘制
        if (groupName != null && alloc.groups.containsKey(groupName)) {
            ModelManager.ModelGroupAllocation groupAlloc = alloc.groups.get(groupName);
            // 使用 glDrawElements 渲染 EBO 中指定的索引片段，索引为 int (GL_UNSIGNED_INT)，每个占 4 字节
            GL11.glDrawElements(GL11.GL_TRIANGLES, groupAlloc.indexCount, GL11.GL_UNSIGNED_INT, groupAlloc.firstIndex * 4L);
        } else {
            // 如果没有指定分组，或者模型本身没有索引数据，则回退到绘制整个 VBO 片段
            GL11.glDrawArrays(GL11.GL_TRIANGLES, alloc.first, alloc.count);
        }
    }

    /**
     * 渲染一个已经在 ModelManager 中注册的模型的全部内容。
     */
    public void renderModel(String modelIdentifier) {
        renderModel(modelIdentifier, null);
    }

    /**
     * 结束渲染环境：解绑 VAO 并重置 Shader 状态。
     */
    public void end() {
        ModelManager.getInstance().unbind();
        ShaderProgram.resetShader();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }
}
