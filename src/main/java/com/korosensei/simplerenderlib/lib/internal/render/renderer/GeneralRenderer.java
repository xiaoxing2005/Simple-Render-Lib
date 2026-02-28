package com.korosensei.simplerenderlib.lib.internal.render.renderer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;
import com.korosensei.simplerenderlib.lib.internal.render.gl.VAO;
import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.VertexStructure;

/**
 * 通用渲染器类
 * 实现了线程安全的顶点缓存、模型矩阵变换堆栈，以及安全延迟的初始化。
 */
public class GeneralRenderer extends AbstractRenderer {

    // 静态 Shader 常量（延迟初始化）
    private static ShaderProgram shaderPosition;
    private static ShaderProgram shaderPositionUV;
    private static ShaderProgram shaderPositionColor;
    private static ShaderProgram shaderPositionLightmap;
    private static ShaderProgram shaderPositionUVLightmap;
    private static ShaderProgram shaderPositionColorLightmap;

    private boolean isInitialized = false;
    private VertexStructure structure;
    private int primitiveMode;

    // 线程安全的顶点缓存集合
    private final List<float[]> vertexCache = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean isDirty = false;
    private int vertexCount = 0;

    // 模型矩阵堆栈（使用 JOML 库，提供高效的矩阵运算和堆栈保存/恢复）
    private final Matrix4fStack matrixStack;
    // 预分配缓冲区，避免每帧分配产生的内存抖动和 GC 压力
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);

    /**
     * 无参构造函数
     * 初始化在此被延迟，使用 {@link #init(VertexStructure, int,int)} 进行真正的初始化配置
     */
    public GeneralRenderer() {
        super();
        this.matrixStack = new Matrix4fStack(16);
        // 初始化为一个不做任何变换的单位矩阵
        this.matrixStack.identity();
    }

    @Override
    protected void initBuffers(int vboDrawType) {
        // 重写父类的 initBuffers 并保持为空。
        // 目的是阻止在构造函数中过早地调用底层 OpenGL API 创建缓冲区。
    }

    @Override
    protected VAO createVAO() {
        // 直接返回配置好的顶点结构中的 VAO
        return structure != null ? structure.createVAO() : null;
    }

    @Override
    protected BufferObject createVBO(int vboDrawType) {
        return null; // VBO is handled inside init
    }

    /**
     * 真正的初始化方法
     * 只有在此方法执行后，渲染器才允许执行渲染
     *
     * @param structure     顶点结构定义 (如 VertexStructure.POSITION_UV 等)
     * @param primitiveMode GL图元模式 (如 GL11.GL_TRIANGLES 等)
     */
    public void init(VertexStructure structure, int primitiveMode, int DrawType) {
        if (isInitialized) {
            return;
        }
        this.structure = structure;
        this.primitiveMode = primitiveMode;

        // 初始化 VBO
        this.vbo = BufferObject.createVBO(DrawType);
        // 初始化 VAO
        this.vao = createVAO();
        if (this.vao != null && this.vbo != null) {
            this.vao.init(this.vbo);
        }

        this.isInitialized = true;
    }

    /**
     * 完全重置并重新初始化渲染器
     * 会销毁现有的缓冲区对象，然后重新调用初始化逻辑
     */
    public void reInitialize(VertexStructure structure, int primitiveMode, int DrawType) {
        reset();

        if (this.vbo != null && !this.vbo.isDeleted()) {
            this.vbo.close();
            this.vbo = null;
        }
        // Notice: We don't close the VAO here because it might be a shared instance from VertexStructure.

        this.isInitialized = false;
        init(structure, primitiveMode, DrawType);
    }

    /**
     * 重置状态：清空所有顶点缓存、矩阵堆栈以及相关的计数器和脏标记。
     */
    public void reset() {
        clearVertices();
        matrixStack.clear();
        // 确保清空后栈顶仍为单位矩阵
        matrixStack.identity();
        this.vertexCount = 0;
        this.isDirty = false;
        // Optionally empty the buffers on GPU if initialized
        if (isInitialized) {
            if (this.vbo != null && !this.vbo.isDeleted()) {
                this.vbo.uploadData(new float[0]);
            }
        }
    }

    // ==========================================
    // 线程安全的顶点缓存与增删操作
    // ==========================================

    private void validateVertexData(float[] vertexData) {
        if (structure == null) {
            throw new IllegalStateException("Renderer must be initialized before adding vertices.");
        }
        int expectedLength = structure.getVertexStride() / Float.BYTES;
        if (vertexData == null || vertexData.length != expectedLength) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid vertex data length. Expected %d floats, but got %d.",
                    expectedLength,
                    vertexData == null ? 0 : vertexData.length));
        }
    }

    public void addVertex(float[] vertexData) {
        validateVertexData(vertexData);
        vertexCache.add(vertexData);
        isDirty = true;
    }

    public void addVertices(float[][] vertices) {
        if (vertices == null) return;
        for (float[] vertexData : vertices) {
            validateVertexData(vertexData);
        }
        synchronized (vertexCache) {
            Collections.addAll(vertexCache, vertices);
        }
        isDirty = true;
    }

    public void removeVertex(float[] vertexData) {
        vertexCache.remove(vertexData);
        isDirty = true;
    }

    public void clearVertices() {
        vertexCache.clear();
        isDirty = true;
    }

    // ==========================================
    // 模型矩阵堆栈与变换操作
    // ==========================================

    /**
     * 保存当前矩阵堆栈（压栈）
     */
    public void pushMatrix() {
        matrixStack.pushMatrix();
    }

    /**
     * 恢复上一个矩阵堆栈（出栈）
     */
    public void popMatrix() {
        matrixStack.popMatrix();
    }

    /**
     * 施加平移变换 (修改当前栈顶矩阵)
     */
    public void translate(float x, float y, float z) {
        matrixStack.translate(x, y, z);
    }

    /**
     * 施加旋转变换 (修改当前栈顶矩阵)
     * 
     * @param angle 旋转角度 (弧度)
     */
    public void rotate(float angle, float x, float y, float z) {
        matrixStack.rotate(angle, x, y, z);
    }

    /**
     * 施加缩放变换 (修改当前栈顶矩阵)
     */
    public void scale(float x, float y, float z) {
        matrixStack.scale(x, y, z);
    }

    /**
     * 追加一个自定义的矩阵到堆栈 (修改当前栈顶矩阵)
     */
    public void multMatrix(Matrix4f matrix) {
        matrixStack.mul(matrix);
    }

    /**
     * 清空当前所有矩阵变换
     */
    public void clearMatrices() {
        matrixStack.clear();
        matrixStack.identity();
    }

    /**
     * 在渲染前，将当前栈顶的矩阵上传到 GPU 的 Shader 中进行运算
     *
     * @param uniformLocation Shader中模型矩阵的 Uniform 变量位置
     */
    public void uploadMatrix(int uniformLocation) {
        if (uniformLocation >= 0) {
            matrixBuffer.clear();
            matrixStack.get(matrixBuffer); // 写入 Buffer
            GL20.glUniformMatrix4(uniformLocation, false, matrixBuffer);
        }
    }

    // ==========================================
    // 渲染相关逻辑
    // ==========================================

    @Override
    public void render() {
        if (!isInitialized) {
            throw new IllegalStateException("GeneralRenderer is not initialized yet. Call init() first.");
        }

        // 检查顶点集合是否有变更，如果有则重新填充 VBO
        if (isDirty) {
            updateVertices();
        }

        ShaderProgram shader = getCurrentShader();
        if (shader != null) {
            shader.use();

            // 显式绑定采样器对应的纹理单元，防止采样器默认指向 0 导致冲突
            int baseTexLoc = GL20.glGetUniformLocation(shader.getProgramID(), "baseTexture");
            if (baseTexLoc >= 0) GL20.glUniform1i(baseTexLoc, 0);
            int lightTexLoc = GL20.glGetUniformLocation(shader.getProgramID(), "lightmapTexture");
            if (lightTexLoc >= 0) GL20.glUniform1i(lightTexLoc, 1);
            
            // 获取并上传投影矩阵
            projBuffer.clear();
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projBuffer);
            int projLoc = GL20.glGetUniformLocation(shader.getProgramID(), "projectionMatrix");
            if (projLoc >= 0) {
                GL20.glUniformMatrix4(projLoc, false, projBuffer);
            }
            
            // 获取并上传视图矩阵
            viewBuffer.clear();
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, viewBuffer);
            int viewLoc = GL20.glGetUniformLocation(shader.getProgramID(), "viewMatrix");
            if (viewLoc >= 0) {
                GL20.glUniformMatrix4(viewLoc, false, viewBuffer);
            }

            // 上传渲染器内部维护的模型矩阵
            int modelLoc = GL20.glGetUniformLocation(shader.getProgramID(), "modelMatrix");
            uploadMatrix(modelLoc);
        }

        super.render();
        
        if (shader != null) {
            ShaderProgram.resetShader();
            // 关键修复：确保将活动的纹理单元恢复为 0，防止干扰后续（如原版粒子）的渲染
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
        }
    }

    @Override
    protected boolean bufferCheck() {
        return isInitialized && vao != null && vao.isInitialized() && !vao.isDeleted();
    }

    @Override
    protected void bufferBind() {
        vao.Bind();
    }

    @Override
    protected void bufferUnbind() {
        vao.Unbind();
    }

    /**
     * 将缓存中的顶点数据同步并重新上传至 VBO，然后恢复标记
     */
    private void updateVertices() {
        synchronized (vertexCache) {
            if (!isDirty) {
                return;
            }

            int totalFloats = 0;
            for (float[] v : vertexCache) {
                totalFloats += v.length;
            }

            if (totalFloats == 0) {
                this.vertexCount = 0;
            } else {
                float[] allData = new float[totalFloats];
                int offset = 0;
                for (float[] v : vertexCache) {
                    System.arraycopy(v, 0, allData, offset, v.length);
                    offset += v.length;
                }

                int floatsPerVertex = structure.getVertexStride() / Float.BYTES;
                this.vertexCount = totalFloats / floatsPerVertex;

                uploadVertexData(allData);
            }

            isDirty = false;
        }
    }

    @Override
    protected void draw() {
        if (vertexCount > 0) {
            GL11.glDrawArrays(primitiveMode, 0, vertexCount);
        }
    }

    /**
     * 获取当前渲染器 VertexStructure 对应的 ShaderProgram
     * 如果对应的 Shader 尚未初始化，则会进行延迟初始化。
     *
     * @return 对应的 ShaderProgram，如果结构不匹配则返回 null
     */
    public ShaderProgram getCurrentShader() {
        if (structure == null) {
            return null;
        }
        
        if (structure == VertexStructure.POSITION) {
            if (shaderPosition == null || !shaderPosition.isValid()) {
                shaderPosition = new ShaderProgram("simplerenderlib:shader/generic/position.vert", "simplerenderlib:shader/generic/position.frag");
            }
            return shaderPosition;
        } else if (structure == VertexStructure.POSITION_UV) {
            if (shaderPositionUV == null || !shaderPositionUV.isValid()) {
                shaderPositionUV = new ShaderProgram("simplerenderlib:shader/generic/position_uv.vert", "simplerenderlib:shader/generic/position_uv.frag");
            }
            return shaderPositionUV;
        } else if (structure == VertexStructure.POSITION_COLOR) {
            if (shaderPositionColor == null || !shaderPositionColor.isValid()) {
                shaderPositionColor = new ShaderProgram("simplerenderlib:shader/generic/position_color.vert", "simplerenderlib:shader/generic/position_color.frag");
            }
            return shaderPositionColor;
        } else if (structure == VertexStructure.POSITION_LIGHTMAP) {
            if (shaderPositionLightmap == null || !shaderPositionLightmap.isValid()) {
                shaderPositionLightmap = new ShaderProgram("simplerenderlib:shader/generic_lightmap/position_lightmap.vert", "simplerenderlib:shader/generic_lightmap/position_lightmap.frag");
            }
            return shaderPositionLightmap;
        } else if (structure == VertexStructure.POSITION_UV_LIGHTMAP) {
            if (shaderPositionUVLightmap == null || !shaderPositionUVLightmap.isValid()) {
                shaderPositionUVLightmap = new ShaderProgram("simplerenderlib:shader/generic_lightmap/position_uv_lightmap.vert", "simplerenderlib:shader/generic_lightmap/position_uv_lightmap.frag");
            }
            return shaderPositionUVLightmap;
        } else if (structure == VertexStructure.POSITION_COLOR_LIGHTMAP) {
            if (shaderPositionColorLightmap == null || !shaderPositionColorLightmap.isValid()) {
                shaderPositionColorLightmap = new ShaderProgram("simplerenderlib:shader/generic_lightmap/position_color_lightmap.vert", "simplerenderlib:shader/generic_lightmap/position_color_lightmap.frag");
            }
            return shaderPositionColorLightmap;
        }
        
        return null;
    }

    @Override
    public void close() {
        super.close();
    }
}
