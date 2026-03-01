package com.korosensei.simplerenderlib.lib.internal.render.gl.structs;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.VAO;
import java.util.function.Supplier;

public class VertexStructure {

    public static final VertexStructure POSITION = new VertexStructure(3 * Float.BYTES, () -> new VAO() {
        @Override
        public void initVertexAttrib(BufferObject vbo) {
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        }
    });

    public static final VertexStructure POSITION_UV = new VertexStructure(5 * Float.BYTES, () -> new VAO() {
        @Override
        public void initVertexAttrib(BufferObject vbo) {
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        }
    });

    public static final VertexStructure POSITION_COLOR = new VertexStructure(7 * Float.BYTES, () -> new VAO() {
        @Override
        public void initVertexAttrib(BufferObject vbo) {
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 * Float.BYTES, 0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES);
        }
    });

    public static final VertexStructure POSITION_LIGHTMAP = new VertexStructure(
        3 * Float.BYTES + Integer.BYTES,
        () -> new VAO() {
            @Override
            public void initVertexAttrib(BufferObject vbo) {
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES + Integer.BYTES, 0);
                // 关键修复：使用 glVertexAttribIPointer 处理整数属性
                GL20.glEnableVertexAttribArray(1);
                GL30.glVertexAttribIPointer(1, 1, GL11.GL_INT, 3 * Float.BYTES + Integer.BYTES, 3 * Float.BYTES);
            }
        });

    public static final VertexStructure POSITION_UV_LIGHTMAP = new VertexStructure(
        5 * Float.BYTES + Integer.BYTES,
        () -> new VAO() {
            @Override
            public void initVertexAttrib(BufferObject vbo) {
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES + Integer.BYTES, 0);
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES + Integer.BYTES, 3 * Float.BYTES);
                // 关键修复：使用 glVertexAttribIPointer
                GL20.glEnableVertexAttribArray(2);
                GL30.glVertexAttribIPointer(2, 1, GL11.GL_INT, 5 * Float.BYTES + Integer.BYTES, 5 * Float.BYTES);
            }
        });

    public static final VertexStructure POSITION_COLOR_LIGHTMAP = new VertexStructure(
        7 * Float.BYTES + Integer.BYTES,
        () -> new VAO() {
            @Override
            public void initVertexAttrib(BufferObject vbo) {
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 7 * Float.BYTES + Integer.BYTES, 0);
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 7 * Float.BYTES + Integer.BYTES, 3 * Float.BYTES);
                // 关键修复：使用 glVertexAttribIPointer
                GL20.glEnableVertexAttribArray(2);
                GL30.glVertexAttribIPointer(2, 1, GL11.GL_INT, 7 * Float.BYTES + Integer.BYTES, 7 * Float.BYTES);
            }
        });

    public static final VertexStructure POSITION_UV_NORMAL = new VertexStructure(8 * Float.BYTES, () -> new VAO() {
        @Override
        public void initVertexAttrib(BufferObject vbo) {
            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 0);
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
            GL20.glEnableVertexAttribArray(2);
            GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES, 5 * Float.BYTES);
        }
    });

    public static final VertexStructure POSITION_UV_NORMAL_LIGHTMAP = new VertexStructure(
        8 * Float.BYTES + Integer.BYTES,
        () -> new VAO() {
            @Override
            public void initVertexAttrib(BufferObject vbo) {
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES + Integer.BYTES, 0);
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 8 * Float.BYTES + Integer.BYTES, 3 * Float.BYTES);
                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 8 * Float.BYTES + Integer.BYTES, 5 * Float.BYTES);
                GL20.glEnableVertexAttribArray(3);
                GL30.glVertexAttribIPointer(3, 1, GL11.GL_INT, 8 * Float.BYTES + Integer.BYTES, 8 * Float.BYTES);
            }
        });

    public static final VertexStructure UNIVERSAL = new VertexStructure(
        12 * Float.BYTES + Integer.BYTES,
        () -> new VAO() {
            @Override
            public void initVertexAttrib(BufferObject vbo) {
                // 0: Position (3 floats)
                GL20.glEnableVertexAttribArray(0);
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 12 * Float.BYTES + Integer.BYTES, 0);
                // 1: Color (4 floats)
                GL20.glEnableVertexAttribArray(1);
                GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 12 * Float.BYTES + Integer.BYTES, 3 * Float.BYTES);
                // 2: UV (2 floats)
                GL20.glEnableVertexAttribArray(2);
                GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 12 * Float.BYTES + Integer.BYTES, 7 * Float.BYTES);
                // 3: Normal (3 floats)
                GL20.glEnableVertexAttribArray(3);
                GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 12 * Float.BYTES + Integer.BYTES, 9 * Float.BYTES);
                // 4: Brightness (1 int)
                GL20.glEnableVertexAttribArray(4);
                GL30.glVertexAttribIPointer(4, 1, GL11.GL_INT, 12 * Float.BYTES + Integer.BYTES, 12 * Float.BYTES);
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
