package com.korosensei.simplerenderlib.lib.internal.render.gl.structs;

import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.VAO;

public enum VertexStructure {

    POSITION(3 * Float.BYTES),
    POSITION_UV(5 * Float.BYTES),
    POSITION_COLOR (7 * Float.BYTES),
    POSITION_COLOR_LIGHTMAP(8 * Float.BYTES),
    POSITION_LIGHTMAP(4 * Float.BYTES),
    POSITION_UV_LIGHTMAP(6 * Float.BYTES),
    POSITION_UV_NORMAL( 8 * Float.BYTES),
    POSITION_UV_NORMAL_LIGHTMAP(9 * Float.BYTES),
    UNIVERSAL(12 * Float.BYTES + Integer.BYTES);

    private final int vertexStride;

    VertexStructure(int vertexStride) {
        this.vertexStride = vertexStride;
    }

    public VAO createVAO() {
        return new VAO() {
            @Override
            public void initVertexAttrib(BufferObject vbo) {
                // 0: Position (3 floats)
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12 * Float.BYTES + Integer.BYTES, 0);
                // 1: Color (4 floats)
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(
                    1,
                    4,
                    GL11.GL_FLOAT,
                    false,
                    12 * Float.BYTES + Integer.BYTES,
                    3 * Float.BYTES);
                // 2: UV (2 floats)
                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(
                    2,
                    2,
                    GL11.GL_FLOAT,
                    false,
                    12 * Float.BYTES + Integer.BYTES,
                    7 * Float.BYTES);
                // 3: Normal (3 floats)
                GL20.glEnableVertexAttribArray(3);
                GL20.glVertexAttribPointer(
                    3,
                    3,
                    GL11.GL_FLOAT,
                    false,
                    12 * Float.BYTES + Integer.BYTES,
                    9 * Float.BYTES);
                // 4: Brightness (1 int)
                GL20.glEnableVertexAttribArray(4);
                GL30.glVertexAttribIPointer(4, 1, GL11.GL_INT, 12 * Float.BYTES + Integer.BYTES, 12 * Float.BYTES);
            }
        };
    }

    public int getVertexStride() {
        return vertexStride;
    }
}
