package com.korosensei.simplerenderlib.lib.internal.render.gl.structs;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.VAO;
import java.util.function.Supplier;

public class VertexStructure {

    public static final VertexStructure POSITION = new VertexStructure(3 * Float.BYTES, () -> new VAO() {

        @Override
        public void initVertexAttrib(BufferObject vbo) {
            // position attribute
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        }
    });
    public static final VertexStructure POSITION_UV = new VertexStructure(5 * Float.BYTES, () -> new VAO() {

        @Override
        public void initVertexAttrib(BufferObject vbo) {
            // position attribute
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
            // uv attribute
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        }
    });
    public static final VertexStructure POSITION_COLOR = new VertexStructure(7 * Float.BYTES, () -> new VAO() {

        @Override
        public void initVertexAttrib(BufferObject vbo) {
            // position attribute
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 * Float.BYTES, 0);
            // color attribute
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES);
        }
    });
    public static final VertexStructure POSITION_LIGHTMAP = new VertexStructure(
        3 * Float.BYTES + Integer.BYTES,
        () -> new VAO() {

            @Override
            public void initVertexAttrib(BufferObject vbo) {
                // position attribute
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES + Integer.BYTES, 0);
                // brightness attribute (int)
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 1, GL11.GL_INT, false, 3 * Float.BYTES + Integer.BYTES, 3 * Float.BYTES);
            }
        });
    public static final VertexStructure POSITION_UV_LIGHTMAP = new VertexStructure(
        5 * Float.BYTES + Integer.BYTES,
        () -> new VAO() {

            @Override
            public void initVertexAttrib(BufferObject vbo) {
                // position attribute
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES + Integer.BYTES, 0);
                // uv attribute
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(
                    1,
                    2,
                    GL11.GL_FLOAT,
                    false,
                    5 * Float.BYTES + Integer.BYTES,
                    3 * Float.BYTES);
                // brightness attribute (int)
                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 1, GL11.GL_INT, false, 5 * Float.BYTES + Integer.BYTES, 5 * Float.BYTES);
            }
        });
    public static final VertexStructure POSITION_COLOR_LIGHTMAP = new VertexStructure(
        7 * Float.BYTES + Integer.BYTES,
        () -> new VAO() {

            @Override
            public void initVertexAttrib(BufferObject vbo) {
                // position attribute
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 * Float.BYTES + Integer.BYTES, 0);
                // color attribute
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(
                    1,
                    4,
                    GL11.GL_FLOAT,
                    false,
                    7 * Float.BYTES + Integer.BYTES,
                    3 * Float.BYTES);
                // brightness attribute (int)
                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 1, GL11.GL_INT, false, 7 * Float.BYTES + Integer.BYTES, 7 * Float.BYTES);
            }
        });

    private final int vertexStride;
    private final Supplier<VAO> vaoFactory;

    public VertexStructure(int vertexStride, Supplier<VAO> vaoFactory) {
        this.vertexStride = vertexStride;
        this.vaoFactory = vaoFactory;
    }

    public VAO createVAO() {
        return vaoFactory.get();
    }

    public int getVertexStride() {
        return vertexStride;
    }
}
