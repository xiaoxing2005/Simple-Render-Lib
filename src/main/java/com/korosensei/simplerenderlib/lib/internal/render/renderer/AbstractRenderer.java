package com.korosensei.simplerenderlib.lib.internal.render.renderer;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.VAO;
import org.lwjgl.opengl.GL15;

import java.util.Arrays;

import static com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject.createVBO;

/**
 * Base renderer that provides minimum reusable render infrastructure:
 * - vertex buffer and VAO creation
 * - CPU-side vertex collection
 * - GPU upload lifecycle
 * - abstract draw entry for concrete render strategy
 */
public abstract class AbstractRenderer implements AutoCloseable {

    private static final int FLOAT_BYTES = Float.BYTES;

    private final int baseVertexStrideBytes;
    private final int baseVertexAttributeFloatCount;
    private final int bufferDrawType;
    private final int initialFloatCapacity;
    private FloatCollector[] vertices;

    private int vertexGroupCount;
    private int totalVertexCount;
    private boolean lightingEnabled;
    private boolean vertexAttribDirty = true;

    private BufferObject vertexBuffer;
    private VAO vao;
    private boolean initialized;
    private boolean closed;
    private boolean dirty = true;

    protected AbstractRenderer(int baseVertexStrideBytes) {
        this(baseVertexStrideBytes, GL15.GL_STATIC_DRAW, 256);
    }

    protected AbstractRenderer(int baseVertexStrideBytes, int bufferDrawType, int initialFloatCapacity) {
        if (baseVertexStrideBytes <= 0) {
            throw new IllegalArgumentException("baseVertexStrideBytes must be > 0");
        }
        if (baseVertexStrideBytes % FLOAT_BYTES != 0) {
            throw new IllegalArgumentException("baseVertexStrideBytes must be a multiple of " + FLOAT_BYTES);
        }
        if (initialFloatCapacity < 0) {
            throw new IllegalArgumentException("initialFloatCapacity must be >= 0");
        }
        this.baseVertexStrideBytes = baseVertexStrideBytes;
        this.baseVertexAttributeFloatCount = baseVertexStrideBytes / FLOAT_BYTES;
        this.bufferDrawType = bufferDrawType;
        this.initialFloatCapacity = Math.max(initialFloatCapacity, baseVertexAttributeFloatCount);
        this.vertices = new FloatCollector[8];
    }

    public final void addVertices(float[] packedAttributes) {
        ensureOpen();
        appendVertexGroup(packedAttributes);
    }

    public final void addVertexGroups(float[]... packedVertexGroups) {
        ensureOpen();
        if (packedVertexGroups == null) {
            return;
        }
        for (float[] packedAttributes : packedVertexGroups) {
            appendVertexGroup(packedAttributes);
        }
    }

    public final void clearVertices() {
        ensureOpen();
        for (int i = 0; i < vertexGroupCount; i++) {
            if (vertices[i] != null) {
                vertices[i].clear();
            }
        }
        vertexGroupCount = 0;
        totalVertexCount = 0;
        dirty = true;
    }

    public final void setLightingEnabled(boolean enabled) {
        ensureOpen();
        if (lightingEnabled == enabled) {
            return;
        }
        lightingEnabled = enabled;
        vertexAttribDirty = true;
        dirty = true;
    }

    public final boolean isLightingEnabled() {
        return lightingEnabled;
    }

    public final int getVertexStrideBytes() {
        return getCurrentVertexStrideBytes();
    }

    public final int getBaseVertexStrideBytes() {
        return baseVertexStrideBytes;
    }

    public final int getVertexAttributeFloatCount() {
        return getCurrentVertexAttributeFloatCount();
    }

    public final int getBaseVertexAttributeFloatCount() {
        return baseVertexAttributeFloatCount;
    }

    public final int getCurrentVertexStrideBytes() {
        return getCurrentVertexAttributeFloatCount() * FLOAT_BYTES;
    }

    public final int getCurrentVertexAttributeFloatCount() {
        return baseVertexAttributeFloatCount;
    }

    public final int getVertexCount() {
        return totalVertexCount;
    }

    public final boolean hasVertices() {
        return getVertexCount() > 0;
    }

    public final boolean isInitialized() {
        return initialized;
    }

    public final void render() {
        ensureOpen();
        ensureInitialized();
        if (!hasVertices()) {
            return;
        }
        refreshVertexAttribIfNeeded();
        uploadIfNeeded();
        vao.Bind();
        beforeDraw(vertexBuffer, getVertexCount());
        draw(vertexBuffer, getVertexCount());
        afterDraw(vertexBuffer, getVertexCount());
        vao.Unbind();
    }

    public final void upload() {
        ensureOpen();
        ensureInitialized();
        refreshVertexAttribIfNeeded();
        uploadIfNeeded();
    }

    protected final BufferObject vertexBuffer() {
        ensureInitialized();
        return vertexBuffer;
    }

    protected final int vaoId() {
        ensureInitialized();
        return vao.id;
    }

    @Override
    public final void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (vao != null) {
            vao.Delete();
        }
        if (vertexBuffer != null) {
            vertexBuffer.Delete();
        }
        onClose();
    }

    private void ensureInitialized() {
        if (initialized) {
            return;
        }
        vertexBuffer = createVBO(bufferDrawType);
        vao = new VAO(vertexBuffer) {
            @Override
            public void BindVertexAttrib(BufferObject VBO) {
                VBO.Bind();
                configureVertexAttrib(getCurrentVertexStrideBytes(), lightingEnabled);
                VBO.unBind();
            }
        };
        vao.init();
        vertexAttribDirty = false;
        initialized = true;
        onInit(vertexBuffer, vao);
    }

    private void refreshVertexAttribIfNeeded() {
        if (!vertexAttribDirty) {
            return;
        }
        vao.Bind();
        vertexBuffer.Bind();
        configureVertexAttrib(getCurrentVertexStrideBytes(), lightingEnabled);
        vertexBuffer.unBind();
        vao.Unbind();
        vertexAttribDirty = false;
    }

    private void uploadIfNeeded() {
        if (!dirty) {
            return;
        }
        vertexBuffer.uploadData(collectUploadData());
        dirty = false;
        onUpload(vertexBuffer, getVertexCount());
    }

    private void appendVertexGroup(float[] packedAttributes) {
        if (packedAttributes == null || packedAttributes.length == 0) {
            return;
        }
        if (packedAttributes.length % baseVertexAttributeFloatCount != 0) {
            throw new IllegalArgumentException(
                "Packed vertex data length must be a multiple of base attribute count per vertex(" +
                    baseVertexAttributeFloatCount + ")");
        }

        ensureGroupCapacity(vertexGroupCount + 1);
        FloatCollector collector = vertices[vertexGroupCount];
        if (collector == null) {
            collector = new FloatCollector(Math.max(initialFloatCapacity, packedAttributes.length));
            vertices[vertexGroupCount] = collector;
        }
        collector.replaceWith(packedAttributes);
        vertexGroupCount++;
        totalVertexCount += packedAttributes.length / baseVertexAttributeFloatCount;
        dirty = true;
    }

    private void ensureGroupCapacity(int minCapacity) {
        if (vertices.length >= minCapacity) {
            return;
        }
        int newSize = vertices.length;
        while (newSize < minCapacity) {
            newSize <<= 1;
        }
        vertices = Arrays.copyOf(vertices, newSize);
    }

    private float[] collectUploadData() {
        if (totalVertexCount == 0) {
            return new float[0];
        }
        return collectBaseVertexData();
    }

    private float[] collectBaseVertexData() {
        float[] output = new float[totalVertexCount * baseVertexAttributeFloatCount];
        int writeOffset = 0;
        for (int i = 0; i < vertexGroupCount; i++) {
            FloatCollector collector = vertices[i];
            if (collector == null || collector.size() == 0) {
                continue;
            }
            writeOffset = collector.copyTo(output, writeOffset);
        }
        return output;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Renderer has been closed");
        }
    }

    protected void onInit(BufferObject vbo, VAO vao) {
    }

    protected void onUpload(BufferObject vbo, int vertexCount) {
    }

    protected void beforeDraw(BufferObject vbo, int vertexCount) {
    }

    protected void afterDraw(BufferObject vbo, int vertexCount) {
    }

    protected void onClose() {
    }

    /**
     * Configure VAO vertex attributes for this renderer.
     */
    protected abstract void configureVertexAttrib(int vertexStrideBytes, boolean withNormal);

    /**
     * Execute actual draw call(s). Implementation decides draw mode and strategy.
     */
    protected abstract void draw(BufferObject vbo, int vertexCount);

    private static final class FloatCollector {
        private float[] data;
        private int size;

        private FloatCollector(int initialCapacity) {
            this.data = new float[Math.max(16, initialCapacity)];
        }

        private void addAll(float[] values) {
            ensureCapacity(size + values.length);
            System.arraycopy(values, 0, data, size, values.length);
            size += values.length;
        }

        private void replaceWith(float[] values) {
            clear();
            addAll(values);
        }

        private void clear() {
            size = 0;
        }

        private int size() {
            return size;
        }

        private float[] toArray() {
            return Arrays.copyOf(data, size);
        }

        private int copyTo(float[] target, int offset) {
            System.arraycopy(data, 0, target, offset, size);
            return offset + size;
        }

        private void ensureCapacity(int minCapacity) {
            if (data.length >= minCapacity) {
                return;
            }
            int newCap = data.length;
            while (newCap < minCapacity) {
                newCap <<= 1;
            }
            data = Arrays.copyOf(data, newCap);
        }
    }
}
