package com.korosensei.simplerenderlib.lib.internal.render.renderer;

import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.VertexStructure;
import org.lwjgl.opengl.GL15;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.VAO;

/**
 * 基础渲染器抽象类
 * 负责最基本的缓冲区（VAO, VBO）的创建与生命周期管理，并提供基础的渲染流程。
 */
public abstract class AbstractRenderer implements AutoCloseable {

    protected VAO vao;
    protected BufferObject vbo;

    public AbstractRenderer() {
        initBuffers(GL15.GL_STATIC_DRAW);
    }

    public AbstractRenderer(int vboDrawType) {
        initBuffers(vboDrawType);
    }

    /**
     * 初始化基础的 VAO 和 VBO 缓冲区
     */
    protected void initBuffers(int vboDrawType) {
        // 创建顶点缓冲区，默认使用 GL_DYNAMIC_DRAW 以便后续更新数据
        vbo = createVBO(vboDrawType);
        // 调用子类实现的 VAO 创建方法，其中应包含顶点属性的配置
        vao = createVAO();
        // 初始化 VAO 并绑定 VBO
        vao.init(vbo);
    }

    /**
     * 抽象方法：由子类提供一个具体的 VAO 实例。
     * 子类需要在该 VAO 的 initVertexAttrib() 中配置顶点属性指针。
     *
     * @return 初始化的 VAO 实例
     */
    protected VAO createVAO() {
        return VertexStructure.UNIVERSAL.createVAO();
    }

    protected BufferObject createVBO(int vboDrawType) {
        return BufferObject.createVBO(vboDrawType);
    }

    /**
     * 预留的方法：为顶点缓冲区 (VBO) 填充数据
     *
     * @param vertexData 顶点数据
     */
    public void uploadVertexData(float[] vertexData) {
        if (vbo != null && vertexData != null) {
            vbo.uploadData(vertexData);
        }
    }

    /**
     * 最基本的渲染方法
     * 绑定 VAO，执行具体的绘制逻辑，最后解绑 VAO。
     */
    public void render() {
        if (bufferCheck()) {
            bufferBind();
            draw();
            bufferUnbind();
        }
    }

    protected abstract boolean bufferCheck();

    protected abstract void bufferBind();

    protected abstract void bufferUnbind();

    /**
     * 核心的抽象绘制方法
     * 子类必须实现此方法以执行具体的 OpenGL 绘制调用，例如 glDrawArrays 或 glDrawElements 等。
     */
    protected abstract void draw();

    /**
     * 释放相关资源
     */
    @Override
    public void close() {
        if (vao != null && vao.isDeleted()) {
            vao.close();
        }
        if (vbo != null && !vbo.isDeleted()) {
            vbo.close();
        }
    }
}
