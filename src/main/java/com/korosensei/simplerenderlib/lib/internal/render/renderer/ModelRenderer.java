package com.korosensei.simplerenderlib.lib.internal.render.renderer;

import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;
import com.korosensei.simplerenderlib.lib.manager.ModelManager;
import com.korosensei.simplerenderlib.lib.manager.ShaderManager;

/**
 * 专为 ModelManager 巨型 VBO 架构设计的高性能静态渲染器。
 * 继承自 AbstractRenderer，复用了渲染的生命周期流程。
 */
public class ModelRenderer extends AbstractRenderer {

    private static final ModelRenderer INSTANCE = new ModelRenderer();

    // 唯一的超级着色器
    private ShaderProgram shaderUniversal;
    private ShaderProgram SpecialShader;

    // 模型矩阵堆栈
    private final Matrix4fStack matrixStack;

    // 预分配的缓冲区，避免 GC 压力
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    // 全局光照 Uniform，默认满亮度
    private int uniformBrightness = 240;

    // 当前要渲染的模型ID和组名
    private String currentModelIdentifier;
    private String currentGroupName;

    private ModelRenderer() {
        // 由于使用了全局 VBO/VAO，我们向父类传递 0 或者不初始化自身的缓冲区
        super(0);
        this.matrixStack = new Matrix4fStack(16);
        this.matrixStack.identity();
    }

    public static ModelRenderer getInstance() {
        return INSTANCE;
    }

    @Override
    protected void initBuffers(int vboDrawType) {
        // 覆盖父类的初始化，因为 ModelRenderer 依赖 ModelManager 的巨型 VBO
        // 不需要创建自己的 VBO 和 VAO
    }

    @Override
    protected boolean bufferCheck() {
        return ModelManager.getInstance().getAllocation(currentModelIdentifier) != null;
    }

    @Override
    protected void bufferBind() {
        ShaderProgram shader = getShader();
        if (shader == null) return;

        shader.use();

        // 投影和视图矩阵已由 RenderManager 的 UBO (binding = 0) 提供
        // ShaderManager.INSTANCE.updateUBO(); 已经废弃

        // 配置纹理单元
        int baseTexLoc = GL20.glGetUniformLocation(shader.getProgramID(), "baseTexture");
        if (baseTexLoc >= 0) GL20.glUniform1i(baseTexLoc, 0);
        int lightTexLoc = GL20.glGetUniformLocation(shader.getProgramID(), "lightmapTexture");
        if (lightTexLoc >= 0) GL20.glUniform1i(lightTexLoc, 1);

        // 绑定 ModelManager 提供的巨型 VAO 和 EBO
        ModelManager.getInstance().bind();
    }

    @Override
    protected void bufferUnbind() {
        ModelManager.getInstance().unbind();
        ShaderProgram.resetShader();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        // 清理当前绘制目标
        currentModelIdentifier = null;
        currentGroupName = null;
    }

    @Override
    protected void draw() {
        ModelManager.ModelAllocation alloc = ModelManager.getInstance().getAllocation(currentModelIdentifier);
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
        if (currentGroupName != null && alloc.groups.containsKey(currentGroupName)) {
            ModelManager.ModelGroupAllocation groupAlloc = alloc.groups.get(currentGroupName);
            GL11.glDrawElements(
                GL11.GL_TRIANGLES,
                groupAlloc.indexCount,
                GL11.GL_UNSIGNED_INT,
                groupAlloc.firstIndex * 4L);
        } else {
            GL11.glDrawArrays(GL11.GL_TRIANGLES, alloc.first, alloc.count);
        }
    }

    /**
     * 获取通用的超级着色器，支持延迟初始化
     */
    public ShaderProgram getShader() {
        if (SpecialShader != null && SpecialShader.isValid()) {
            return SpecialShader;
        }
        if (shaderUniversal == null || !shaderUniversal.isValid()) {
            shaderUniversal = new ShaderProgram(
                "simplerenderlib:shader/universal.vert",
                "simplerenderlib:shader/universal.frag");
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

    public void setSpecialShader(ShaderProgram shader) {
        this.SpecialShader = shader;
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
     * 兼容旧版 begin()
     */
    public void begin() {
        // 交由 AbstractRenderer 的 render() 内的 bufferBind() 自动处理
    }

    /**
     * 渲染一个已经在 ModelManager 中注册的模型的特定分组。
     * 如果传入的分组名为 null，则渲染该模型的全部内容（如果模型不支持分组则回退到 glDrawArrays）。
     *
     * @param modelIdentifier 模型的唯一标识符
     * @param groupName       要渲染的分组名称（传入 null 以渲染全部）
     */
    public void renderModel(String modelIdentifier, String groupName) {
        this.currentModelIdentifier = modelIdentifier;
        this.currentGroupName = groupName;
        // 触发 AbstractRenderer 的生命周期循环 (bufferCheck -> bufferBind -> draw -> bufferUnbind)
        super.render();
    }

    /**
     * 渲染一个已经在 ModelManager 中注册的模型的全部内容。
     */
    public void renderModel(String modelIdentifier) {
        renderModel(modelIdentifier, null);
    }

    /**
     * 兼容旧版 end()
     */
    public void end() {
        // 交由 AbstractRenderer 的 render() 内的 bufferUnbind() 自动处理
    }
}
