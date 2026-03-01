package com.korosensei.simplerenderlib.lib.manager;

import static com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject.createUBO;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;

public class ShaderManager {

    public static ShaderManager INSTANCE;
    private final ArrayList<ShaderProgram> shaderList;
    private final BufferObject UBO;
    private final FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);

    private ShaderManager() {
        shaderList = new ArrayList<>();
        UBO = createUBO(GL15.GL_DYNAMIC_DRAW);
        UBO.Bind();
        GL15.glBufferData(GL31.GL_UNIFORM_BUFFER, 128, GL15.GL_DYNAMIC_DRAW);
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, 0, UBO.ID);
        UBO.unBind();
    }

    public static void init() {
        INSTANCE = new ShaderManager();
    }

    public void registerShader(ShaderProgram shader) {
        shaderList.add(shader);

        // 关键修复：确保显式将着色器中的 Uniform Block 绑定到 0 号绑定点
        // 有些驱动会忽略 GLSL 里的 layout(binding = 0)
        int blockIndex = GL31.glGetUniformBlockIndex(shader.getProgramID(), "Matrices");
        if (blockIndex != GL31.GL_INVALID_INDEX) {
            GL31.glUniformBlockBinding(shader.getProgramID(), blockIndex, 0);
        }
    }

    public ArrayList<ShaderProgram> getShaderList() {
        return shaderList;
    }

    public void updateUBO() {
        projectionMatrixBuffer.clear();
        viewMatrixBuffer.clear();

        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrixBuffer);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, viewMatrixBuffer);

        projectionMatrixBuffer.rewind();
        viewMatrixBuffer.rewind();

        UBO.Bind();
        // std140 mat4 在内存中占据 64 bytes
        GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, 0, projectionMatrixBuffer);
        GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, 64, viewMatrixBuffer);
        UBO.unBind();
    }
}
