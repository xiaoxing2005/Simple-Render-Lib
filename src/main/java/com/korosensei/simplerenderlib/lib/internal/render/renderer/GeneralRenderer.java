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
import com.korosensei.simplerenderlib.lib.manager.ShaderManager;

/**
 * 通用渲染器类
 * 实现了线程安全的顶点缓存、模型矩阵变换堆栈，以及安全延迟的初始化。
 */
public class GeneralRenderer extends AbstractRenderer {

    // 静态 Shader 常量（延迟初始化），只保留单一的超级着色器
    private static ShaderProgram shaderUniversal;

    private boolean isInitialized = false;
    private VertexStructure structure;
    private int primitiveMode;

    // 线程安全的顶点缓存集合 (缓存转换后的统一13-float顶点)
    private final List<float[]> vertexCache = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean isDirty = false;
    private int vertexCount = 0;

    // 当前顶点状态
    private float currentR = -1.0f, currentG = -1.0f, currentB = -1.0f, currentA = -1.0f;
    private float currentU = -1.0f, currentV = -1.0f;
    private float currentNX = -1.0f, currentNY = -1.0f, currentNZ = -1.0f;
    private int currentBrightness = -1;

    // 全局光照 Uniform，默认 240 (满亮度)
    private int uniformBrightness = 240;

    // 模型矩阵堆栈
    private final Matrix4fStack matrixStack;

    // 预分配缓冲区，避免每帧分配产生的内存抖动和 GC 压力
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer viewBuffer = BufferUtils.createFloatBuffer(16);

    public GeneralRenderer() {
        super();
        this.matrixStack = new Matrix4fStack(16);
        this.matrixStack.identity();
    }

    @Override
    protected void initBuffers(int vboDrawType) {
        // 重写父类的 initBuffers 并保持为空。
    }

    @Override
    protected VAO createVAO() {
        // 始终使用 UNIVERSAL 结构与着色器对接
        return VertexStructure.UNIVERSAL.createVAO();
    }

    @Override
    protected BufferObject createVBO(int vboDrawType) {
        return null;
    }

    public void init(VertexStructure structure, int primitiveMode, int DrawType) {
        if (isInitialized) {
            return;
        }
        this.structure = structure;
        this.primitiveMode = primitiveMode;

        this.vbo = BufferObject.createVBO(DrawType);
        this.vao = createVAO();
        if (this.vao != null && this.vbo != null) {
            this.vao.init(this.vbo);
        }

        this.isInitialized = true;
    }

    public void reInitialize(VertexStructure structure, int primitiveMode, int DrawType) {
        reset();
        if (this.vbo != null && !this.vbo.isDeleted()) {
            this.vbo.close();
            this.vbo = null;
        }
        this.isInitialized = false;
        init(structure, primitiveMode, DrawType);
    }

    public void reset() {
        clearVertices();
        matrixStack.clear();
        matrixStack.identity();
        this.vertexCount = 0;
        this.isDirty = false;
        resetState();
        if (isInitialized) {
            if (this.vbo != null && !this.vbo.isDeleted()) {
                this.vbo.uploadData(new float[0]);
            }
        }
    }

    public void resetState() {
        currentR = -1.0f;
        currentG = -1.0f;
        currentB = -1.0f;
        currentA = -1.0f;
        currentU = -1.0f;
        currentV = -1.0f;
        currentNX = -1.0f;
        currentNY = -1.0f;
        currentNZ = -1.0f;
        currentBrightness = -1;
    }

    public void setColor(float r, float g, float b, float a) {
        this.currentR = r;
        this.currentG = g;
        this.currentB = b;
        this.currentA = a;
    }

    public void setUV(float u, float v) {
        this.currentU = u;
        this.currentV = v;
    }

    public void setNormal(float nx, float ny, float nz) {
        this.currentNX = nx;
        this.currentNY = ny;
        this.currentNZ = nz;
    }

    public void setBrightness(int brightness) {
        this.currentBrightness = brightness;
    }

    /**
     * 设置全局统一的光照亮度（供着色器在顶点未指定亮度时作为后备使用）
     *
     * @param brightness 打包后的 Minecraft 光照值或 0-240 的简写满亮度值
     */
    public void setUniformBrightness(int brightness) {
        this.uniformBrightness = brightness;
    }

    public void addVertex(float x, float y, float z) {
        float[] padded = new float[13];
        padded[0] = x;
        padded[1] = y;
        padded[2] = z;

        padded[3] = currentR;
        padded[4] = currentG;
        padded[5] = currentB;
        padded[6] = currentA;

        padded[7] = currentU;
        padded[8] = currentV;

        padded[9] = currentNX;
        padded[10] = currentNY;
        padded[11] = currentNZ;

        padded[12] = Float.intBitsToFloat(currentBrightness);

        vertexCache.add(padded);
        isDirty = true;
    }

    // --- 一次性提交便捷方法 ---

    public void addVertexWithUV(float x, float y, float z, float u, float v) {
        setUV(u, v);
        addVertex(x, y, z);
    }

    public void addVertexWithColor(float x, float y, float z, float r, float g, float b, float a) {
        setColor(r, g, b, a);
        addVertex(x, y, z);
    }

    public void addVertexWithLightmap(float x, float y, float z, int brightness) {
        setBrightness(brightness);
        addVertex(x, y, z);
    }

    public void addVertexWithUVLightmap(float x, float y, float z, float u, float v, int brightness) {
        setUV(u, v);
        setBrightness(brightness);
        addVertex(x, y, z);
    }

    public void addVertexWithColorLightmap(float x, float y, float z, float r, float g, float b, float a,
        int brightness) {
        setColor(r, g, b, a);
        setBrightness(brightness);
        addVertex(x, y, z);
    }

    public void addVertexWithUVNormal(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        setUV(u, v);
        setNormal(nx, ny, nz);
        addVertex(x, y, z);
    }

    public void addVertexWithUVNormalLightmap(float x, float y, float z, float u, float v, float nx, float ny, float nz,
        int brightness) {
        setUV(u, v);
        setNormal(nx, ny, nz);
        setBrightness(brightness);
        addVertex(x, y, z);
    }

    private void validateVertexData(float[] vertexData) {
        if (structure == null) {
            throw new IllegalStateException("Renderer must be initialized before adding vertices.");
        }
        int expectedLength = structure.getVertexStride() / Float.BYTES;
        if (vertexData == null || vertexData.length != expectedLength) {
            throw new IllegalArgumentException(
                "Invalid vertex data length. Expected " + expectedLength
                    + " but got "
                    + (vertexData == null ? 0 : vertexData.length));
        }
    }

    /**
     * 将任意合法长度的顶点数据转换为通用 (UNIVERSAL) 的 13 浮点数格式
     * 缺少的属性使用 -1 填充以通知 Shader 禁用
     */
    private float[] padVertexData(float[] input) {
        float[] padded = new float[13];
        // 默认填充 -1.0f
        padded[3] = currentR;
        padded[4] = currentG;
        padded[5] = currentB;
        padded[6] = currentA;

        padded[7] = currentU;
        padded[8] = currentV;

        padded[9] = currentNX;
        padded[10] = currentNY;
        padded[11] = currentNZ;
        padded[12] = currentBrightness;// Brightness

        // Position 必定存在 (0-2)
        padded[0] = input[0];
        padded[1] = input[1];
        padded[2] = input[2];

        switch (structure) {
            case POSITION -> {
                // Nothing to map
            }
            case POSITION_UV -> {
                padded[7] = input[3];
                padded[8] = input[4];
            }
            case POSITION_COLOR -> {
                padded[3] = input[3];
                padded[4] = input[4];
                padded[5] = input[5];
                padded[6] = input[6];
            }
            case POSITION_LIGHTMAP -> {
                padded[12] = input[3];
            }
            case POSITION_UV_LIGHTMAP -> {
                padded[7] = input[3];
                padded[8] = input[4];
                padded[12] = input[5];
            }
            case POSITION_COLOR_LIGHTMAP -> {
                padded[3] = input[3];
                padded[4] = input[4];
                padded[5] = input[5];
                padded[6] = input[6];
                padded[12] = input[7];
            }
            case POSITION_UV_NORMAL -> {
                padded[7] = input[3];
                padded[8] = input[4];
                padded[9] = input[5];
                padded[10] = input[6];
                padded[11] = input[7];
            }
            case POSITION_UV_NORMAL_LIGHTMAP -> {
                padded[7] = input[3];
                padded[8] = input[4];
                padded[9] = input[5];
                padded[10] = input[6];
                padded[11] = input[7];
                padded[12] = input[8];
            }
        }

        return padded;
    }

    public void addVertex(float[] vertexData) {
        validateVertexData(vertexData);
        vertexCache.add(padVertexData(vertexData));
        isDirty = true;
    }

    public void addVertices(float[][] vertices) {
        if (vertices == null) return;
        List<float[]> paddedList = new ArrayList<>(vertices.length);
        for (float[] vertexData : vertices) {
            validateVertexData(vertexData);
            paddedList.add(padVertexData(vertexData));
        }
        synchronized (vertexCache) {
            vertexCache.addAll(paddedList);
        }
        isDirty = true;
    }

    public void removeVertex(float[] vertexData) {
        // 由于存入的是 Padding 过的数组，直接 remove 对象会失效，
        // 需要的时候应当 clear 重新填充。
        isDirty = true;
    }

    public void clearVertices() {
        vertexCache.clear();
        isDirty = true;
    }

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

    public void uploadMatrix(int uniformLocation) {
        if (uniformLocation >= 0) {
            matrixBuffer.clear();
            matrixStack.get(matrixBuffer);
            GL20.glUniformMatrix4(uniformLocation, false, matrixBuffer);
        }
    }

    @Override
    public void render() {
        if (!isInitialized) {
            throw new IllegalStateException("GeneralRenderer is not initialized yet.");
        }

        if (isDirty) {
            updateVertices();
        }

        ShaderProgram shader = getCurrentShader();
        if (shader != null) {
            shader.use();

            // 采样器绑定
            int baseTexLoc = GL20.glGetUniformLocation(shader.getProgramID(), "baseTexture");
            if (baseTexLoc >= 0) GL20.glUniform1i(baseTexLoc, 0);
            int lightTexLoc = GL20.glGetUniformLocation(shader.getProgramID(), "lightmapTexture");
            if (lightTexLoc >= 0) GL20.glUniform1i(lightTexLoc, 1);

            // 投影矩阵
            projBuffer.clear();
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projBuffer);
            int projLoc = GL20.glGetUniformLocation(shader.getProgramID(), "projectionMatrix");
            if (projLoc >= 0) GL20.glUniformMatrix4(projLoc, false, projBuffer);

            // 视图矩阵
            viewBuffer.clear();
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, viewBuffer);
            int viewLoc = GL20.glGetUniformLocation(shader.getProgramID(), "viewMatrix");
            if (viewLoc >= 0) GL20.glUniformMatrix4(viewLoc, false, viewBuffer);

            // 模型矩阵
            int modelLoc = GL20.glGetUniformLocation(shader.getProgramID(), "modelMatrix");
            uploadMatrix(modelLoc);

            // 全局亮度 Uniform
            int brightLoc = GL20.glGetUniformLocation(shader.getProgramID(), "uBrightness");
            if (brightLoc >= 0) GL20.glUniform1i(brightLoc, uniformBrightness);
        }

        super.render();

        if (shader != null) {
            ShaderProgram.resetShader();
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

    private void updateVertices() {
        synchronized (vertexCache) {
            if (!isDirty) return;
            int totalFloats = 0;
            for (float[] v : vertexCache) totalFloats += v.length;

            if (totalFloats == 0) {
                this.vertexCount = 0;
            } else {
                float[] allData = new float[totalFloats];
                int offset = 0;
                for (float[] v : vertexCache) {
                    System.arraycopy(v, 0, allData, offset, v.length);
                    offset += v.length;
                }

                // 由于已经转化为了 UNIVERSAL 结构，固定为 13
                int floatsPerVertex = 13;
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

    public ShaderProgram getCurrentShader() {
        if (shaderUniversal == null || !shaderUniversal.isValid()) {
            shaderUniversal = new ShaderProgram(
                "simplerenderlib:shader/universal.vert",
                "simplerenderlib:shader/universal.frag");
            ShaderManager.INSTANCE.registerShader(shaderUniversal);
        }
        return shaderUniversal;
    }

    @Override
    public void close() {
        super.close();
    }
}
