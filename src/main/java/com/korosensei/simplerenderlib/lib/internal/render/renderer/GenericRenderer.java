package com.korosensei.simplerenderlib.lib.internal.render.renderer;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;
import com.korosensei.simplerenderlib.lib.internal.render.gl.VAO;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.Objects;

import static com.korosensei.simplerenderlib.SimpleRenderLib.LOG;

public class GenericRenderer extends AbstractRenderer {

    public enum VertexStructure {
        POSITION(
            3,
            "simplerenderlib:shader/generic/position.vert",
            "simplerenderlib:shader/generic/position.frag"),
        POSITION_UV(
            5,
            "simplerenderlib:shader/generic/position_uv.vert",
            "simplerenderlib:shader/generic/position_uv.frag"),
        POSITION_COLOR(
            7,
            "simplerenderlib:shader/generic/position_color.vert",
            "simplerenderlib:shader/generic/position_color.frag");

        private final int attributeFloatCount;
        private final String vertexShaderPath;
        private final String fragmentShaderPath;

        VertexStructure(
            int attributeFloatCount,
            String vertexShaderPath,
            String fragmentShaderPath
        ) {
            this.attributeFloatCount = attributeFloatCount;
            this.vertexShaderPath = vertexShaderPath;
            this.fragmentShaderPath = fragmentShaderPath;
        }

        public int attributeFloatCount() {
            return attributeFloatCount;
        }

        public int strideBytes() {
            return attributeFloatCount * Float.BYTES;
        }

        public String vertexShaderPath() {
            return vertexShaderPath;
        }

        public String fragmentShaderPath() {
            return fragmentShaderPath;
        }

        public boolean hasUv() {
            return this == POSITION_UV;
        }

        public boolean hasColor() {
            return this == POSITION_COLOR;
        }
    }

    private final VertexStructure vertexStructure;
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    private ShaderProgram baseShaderProgram;
    private ShaderProgram normalShaderProgram;
    private ResourceLocation material;
    private float brushR = 1.0f;
    private float brushG = 1.0f;
    private float brushB = 1.0f;
    private float brushA = 1.0f;

    public GenericRenderer(VertexStructure vertexStructure) {
        this(vertexStructure, GL15.GL_DYNAMIC_DRAW, 256);
    }

    public GenericRenderer(VertexStructure vertexStructure, int drawType, int initialFloatCapacity) {
        super(Objects.requireNonNull(vertexStructure, "vertexStructure").strideBytes(), drawType, initialFloatCapacity);
        this.vertexStructure = vertexStructure;
    }

    public GenericRenderer setBrushColor(float r, float g, float b, float a) {
        this.brushR = clamp01(r);
        this.brushG = clamp01(g);
        this.brushB = clamp01(b);
        this.brushA = clamp01(a);
        return this;
    }

    public GenericRenderer setMaterial(ResourceLocation material) {
        this.material = material;
        return this;
    }

    public GenericRenderer setLighting(boolean enabled) {
        setLightingEnabled(enabled);
        return this;
    }

    public void submit(float[] packedVertices) {
        addVertices(packedVertices);
    }

    public void submit(float[]... packedVertexGroups) {
        addVertexGroups(packedVertexGroups);
    }

    public void triangle(float[] vertex0, float[] vertex1, float[] vertex2) {
        addVertices(packVertices(vertex0, vertex1, vertex2));
    }

    public void square(float[] topLeft, float[] topRight, float[] bottomLeft, float[] bottomRight) {
        addVertices(packVertices(
            topLeft, topRight, bottomLeft,
            bottomLeft, topRight, bottomRight));
    }

    public void rightTriangle(float[] rightAngle, float[] edgeX, float[] edgeY) {
        triangle(rightAngle, edgeX, edgeY);
    }

    public void triangle(
        float x1, float y1, float z1,
        float x2, float y2, float z2,
        float x3, float y3, float z3
    ) {
        float[] v0 = createVertex(x1, y1, z1, 0.0f, 0.0f);
        float[] v1 = createVertex(x2, y2, z2, 1.0f, 0.0f);
        float[] v2 = createVertex(x3, y3, z3, 0.0f, 1.0f);
        addVertices(packVertices(v0, v1, v2));
    }

    public void square(float x, float y, float z, float width, float height) {
        float[] topLeft = createVertex(x, y, z, 0.0f, 0.0f);
        float[] topRight = createVertex(x + width, y, z, 1.0f, 0.0f);
        float[] bottomLeft = createVertex(x, y - height, z, 0.0f, 1.0f);
        float[] bottomRight = createVertex(x + width, y - height, z, 1.0f, 1.0f);
        square(topLeft, topRight, bottomLeft, bottomRight);
    }

    public void rightTriangle(float x, float y, float z, float width, float height) {
        float[] rightAngle = createVertex(x, y, z, 0.0f, 0.0f);
        float[] edgeX = createVertex(x + width, y, z, 1.0f, 0.0f);
        float[] edgeY = createVertex(x, y - height, z, 0.0f, 1.0f);
        rightTriangle(rightAngle, edgeX, edgeY);
    }

    @Override
    protected void onInit(BufferObject vbo, VAO vao) {
        baseShaderProgram = new ShaderProgram(vertexStructure.vertexShaderPath(), vertexStructure.fragmentShaderPath());
        if (!baseShaderProgram.isValid()) {
            LOG.error("GenericRenderer base shader invalid for {}", vertexStructure);
        }
        if (!normalShaderProgram.isValid()) {
            LOG.error("GenericRenderer normal shader invalid for {}", vertexStructure);
        }
    }

    @Override
    protected void configureVertexAttrib(int vertexStrideBytes, boolean withNormal) {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, vertexStrideBytes, 0L);

        int attributeOffsetBytes = 3 * Float.BYTES;

        if (vertexStructure.hasUv()) {
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, vertexStrideBytes, attributeOffsetBytes);
        } else if (vertexStructure.hasColor()) {
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, vertexStrideBytes, attributeOffsetBytes);
        }
    }

    @Override
    protected void draw(BufferObject vbo, int vertexCount) {
        ShaderProgram shaderProgram = selectShaderProgram();
        if (shaderProgram == null || !shaderProgram.isValid() || vertexCount <= 0) {
            return;
        }

        shaderProgram.use();
        uploadMatrices(shaderProgram);
        uploadBrushColor(shaderProgram);

        if (vertexStructure.hasUv()) {
            boolean hasTexture = bindMaterialIfPresent();
            uploadTextureState(shaderProgram, hasTexture);
        }

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);

        if (vertexStructure.hasUv()) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        ShaderProgram.resetShader();
    }

    @Override
    protected void onClose() {
        if (baseShaderProgram != null) {
            baseShaderProgram.close();
            baseShaderProgram = null;
        }
        if (normalShaderProgram != null) {
            normalShaderProgram.close();
            normalShaderProgram = null;
        }
    }

    private float[] createVertex(float x, float y, float z, float u, float v) {
        switch (vertexStructure) {
            case POSITION:
                return new float[]{x, y, z};
            case POSITION_UV:
                return new float[]{x, y, z, u, v};
            case POSITION_COLOR:
                return new float[]{x, y, z, brushR, brushG, brushB, brushA};
            default:
                throw new IllegalStateException("Unsupported vertex structure: " + vertexStructure);
        }
    }

    private float[] packVertices(float[]... vertices) {
        int attributeCount = vertexStructure.attributeFloatCount();
        float[] packed = new float[vertices.length * attributeCount];
        int writeOffset = 0;
        for (float[] vertex : vertices) {
            if (vertex == null || vertex.length != attributeCount) {
                throw new IllegalArgumentException(
                    "Each vertex must contain exactly " + attributeCount + " floats for " + vertexStructure);
            }
            System.arraycopy(vertex, 0, packed, writeOffset, attributeCount);
            writeOffset += attributeCount;
        }
        return packed;
    }

    private ShaderProgram selectShaderProgram() {
        if (isLightingEnabled()) {
            return normalShaderProgram != null && normalShaderProgram.isValid() ? normalShaderProgram : baseShaderProgram;
        }
        return baseShaderProgram;
    }

    private void uploadMatrices(ShaderProgram shaderProgram) {
        int locView = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "modelViewMatrix");
        int locProj = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "projectionMatrix");

        matrixBuffer.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrixBuffer);
        matrixBuffer.rewind();
        if (locView >= 0) {
            GL20.glUniformMatrix4(locView, false, matrixBuffer);
        }

        matrixBuffer.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, matrixBuffer);
        matrixBuffer.rewind();
        if (locProj >= 0) {
            GL20.glUniformMatrix4(locProj, false, matrixBuffer);
        }
    }

    private void uploadBrushColor(ShaderProgram shaderProgram) {
        int locBrush = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "uBrushColor");
        if (locBrush >= 0) {
            GL20.glUniform4f(locBrush, brushR, brushG, brushB, brushA);
        }
    }

    private void uploadTextureState(ShaderProgram shaderProgram, boolean hasTexture) {
        int locUseTexture = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "uUseTexture");
        if (locUseTexture >= 0) {
            GL20.glUniform1i(locUseTexture, hasTexture ? 1 : 0);
        }
        int locTexture = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "uTexture");
        if (locTexture >= 0) {
            GL20.glUniform1i(locTexture, 0);
        }
    }

    private boolean bindMaterialIfPresent() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        if (material == null) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            return false;
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(material);
        return true;
    }

    private float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
