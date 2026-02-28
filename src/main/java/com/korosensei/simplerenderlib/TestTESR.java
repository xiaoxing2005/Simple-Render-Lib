package com.korosensei.simplerenderlib;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.IndirectBuffer;
import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;
import com.korosensei.simplerenderlib.lib.internal.render.gl.VAO;
import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.DrawArraysIndirectCommand;
import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.InstanceData;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import java.nio.FloatBuffer;

import static com.korosensei.simplerenderlib.SimpleRenderLib.LOG;
import static com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject.createSSBO;
import static com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject.createVBO;

public class TestTESR extends TileEntitySpecialRenderer {

    private static final int GRID_SIZE_X = 32;
    private static final int GRID_SIZE_Y = 32;
    private static final int GRID_SIZE_Z = 128;
    private static final int INSTANCE_COUNT = GRID_SIZE_X * GRID_SIZE_Y * GRID_SIZE_Z; // 131072
    private static final float GRID_CELL_SIZE = 1.0f; // one block per triangle
    private static final float GRID_ORIGIN_X = 0.0f;
    private static final float GRID_ORIGIN_Y = 0.0f;
    private static final float GRID_ORIGIN_Z = 0.0f;
    private static final float TRIANGLE_SIZE = 1.0f;

    private boolean initialized = false;
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private ShaderProgram shaderProgram;
    private BufferObject vbo;
    private VAO vao;
    private BufferObject ssbo;
    private IndirectBuffer indirectBuffer;

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!initialized) {
            initRenderData();
        }

        if (shaderProgram == null || !shaderProgram.isValid()) {
            LOG.error("TestTESR shader program is invalid, skipping render");
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        boolean wasCullingEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);

        shaderProgram.use();
        uploadMatrices();

        vao.Bind();
        ssbo.bindBase(0);
        GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, indirectBuffer.ID);
        GL43.glMultiDrawArraysIndirect(GL11.GL_TRIANGLES, 0, 1, 0);

        indirectBuffer.unBind();
        vao.Unbind();
        GL20.glUseProgram(0);

        if (wasCullingEnabled) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }

        GL11.glPopMatrix();
    }

    private void initRenderData() {
        shaderProgram = new ShaderProgram(
            "simplerenderlib:shader/Texture.vert",
            "simplerenderlib:shader/Texture.frag");

        vbo = createVBO(GL15.GL_STATIC_DRAW);
        vao = new VAO(vbo) {
            @Override
            public void BindVertexAttrib(BufferObject VBO) {
                VBO.Bind();
                GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0L);
                GL20.glEnableVertexAttribArray(0);
                VBO.unBind();
            }
        };

        float halfWidth = TRIANGLE_SIZE * 0.5f;
        float halfHeight = TRIANGLE_SIZE * 0.5f;

        // Base triangle around origin, each instance shifts it in world space.
        vbo.uploadData(
            0.0f, halfHeight, 0.0f,
            -halfWidth, -halfHeight, 0.0f,
            halfWidth, -halfHeight, 0.0f
        );
        vao.init();

        ssbo = createSSBO(GL15.GL_STATIC_DRAW);
        ssbo.uploadData(buildGridInstances());

        indirectBuffer = new IndirectBuffer(GL15.GL_STATIC_DRAW);
        indirectBuffer.uploadData(new DrawArraysIndirectCommand(3, INSTANCE_COUNT, 0, 0));

        initialized = true;
    }

    private InstanceData[] buildGridInstances() {
        InstanceData[] instances = new InstanceData[INSTANCE_COUNT];
        int idx = 0;

        for (int zIndex = 0; zIndex < GRID_SIZE_Z; zIndex++) {
            float centerZ = GRID_ORIGIN_Z + (zIndex + 0.5f) * GRID_CELL_SIZE;

            for (int row = 0; row < GRID_SIZE_Y; row++) {
                float rowT = GRID_SIZE_Y == 1 ? 0.0f : (float) row / (GRID_SIZE_Y - 1);
                float centerY = GRID_ORIGIN_Y + (GRID_SIZE_Y - row - 0.5f) * GRID_CELL_SIZE;
                float[] rgb = rainbow(rowT);

                for (int col = 0; col < GRID_SIZE_X; col++) {
                    float centerX = GRID_ORIGIN_X + (col + 0.5f) * GRID_CELL_SIZE;
                    instances[idx++] = InstanceData.of(
                        centerX, centerY, centerZ, 0.0f,
                        rgb[0], rgb[1], rgb[2], 1.0f
                    );
                }
            }
        }

        return instances;
    }

    // Vertical colorful gradient (top-left -> bottom-left), reused across columns.
    private float[] rainbow(float t) {
        float hue = 0.08f + 0.72f * clamp01(t);
        float saturation = 0.9f;
        float value = 1.0f;

        float h6 = hue * 6.0f;
        int region = ((int) Math.floor(h6)) % 6;
        float f = h6 - (float) Math.floor(h6);
        float p = value * (1.0f - saturation);
        float q = value * (1.0f - f * saturation);
        float u = value * (1.0f - (1.0f - f) * saturation);

        switch (region) {
            case 0:
                return new float[]{value, u, p};
            case 1:
                return new float[]{q, value, p};
            case 2:
                return new float[]{p, value, u};
            case 3:
                return new float[]{p, q, value};
            case 4:
                return new float[]{u, p, value};
            default:
                return new float[]{value, p, q};
        }
    }

    private float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private void uploadMatrices() {
        int locView = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "modelViewMatrix");
        int locProj = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "projectionMatrix");

        matrixBuffer.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrixBuffer);
        matrixBuffer.rewind();
        GL20.glUniformMatrix4(locView, false, matrixBuffer);

        matrixBuffer.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, matrixBuffer);
        matrixBuffer.rewind();
        GL20.glUniformMatrix4(locProj, false, matrixBuffer);
    }
}
