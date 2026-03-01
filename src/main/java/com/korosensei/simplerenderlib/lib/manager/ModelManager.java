package com.korosensei.simplerenderlib.lib.manager;


import com.korosensei.simplerenderlib.lib.api.model.IIndexedMesh;
import com.korosensei.simplerenderlib.lib.internal.model.IMesh;
import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.VAO;
import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.VertexStructure;
import org.lwjgl.opengl.GL15;

import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 唯一的巨型 VBO 模型管理器
 * 负责接收不同结构的模型顶点，自动填充为 UNIVERSAL 结构，
 * 并将它们合并到一个巨型的 VBO 和 EBO 中进行统一管理。
 */
public class ModelManager {

    private static final ModelManager INSTANCE = new ModelManager();

    private BufferObject VBO;
    private BufferObject EBO;
    private VAO VAO;

    // 在内存中累积的统一格式(13-float)顶点
    private final List<float[]> vertexBuffer = new ArrayList<>();
    // 累积的索引缓冲
    private final List<Integer> indexBuffer = new ArrayList<>();

    private boolean isDirty = false;
    private int currentVertexCount = 0;
    private int currentIndexCount = 0;

    // 记录模型的分组分配信息
    public static class ModelGroupAllocation {
        public final int firstIndex;
        public final int indexCount;

        public ModelGroupAllocation(int firstIndex, int indexCount) {
            this.firstIndex = firstIndex;
            this.indexCount = indexCount;
        }
    }

    // 记录每个模型在巨型 VBO 中的分配信息
    public static class ModelAllocation {
        public final int first;
        public final int count;
        public final Map<String, ModelGroupAllocation> groups = new HashMap<>();

        public ModelAllocation(int first, int count) {
            this.first = first;
            this.count = count;
        }
    }

    private final Map<String, ModelAllocation> allocationMap = new HashMap<>();

    private ModelManager() {
    }

    public static ModelManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化巨型 VBO, EBO 和配套的 UNIVERSAL VAO
     */
    public void init() {
        if (VBO == null) {
            VBO = BufferObject.createVBO(GL15.GL_STATIC_DRAW);
            EBO = BufferObject.createEBO(GL15.GL_STATIC_DRAW);
            VAO = VertexStructure.UNIVERSAL.createVAO();

            VAO.init(VBO);

            // 将 EBO 绑定记录到 VAO 中
            VAO.Bind();
            EBO.Bind();
            VAO.Unbind();
            EBO.unBind();
        }
    }

    /**
     * 向管理器中注册一个模型
     */
    public void registerModel(String identifier, IMesh mesh) {
        if (allocationMap.containsKey(identifier)) {
            return;
        }

        VertexStructure structure = mesh.getVertexStructure();
        float[][] originalVertices = mesh.getVertexs();

        if (originalVertices == null || originalVertices.length == 0) {
            return;
        }

        int startOffset = currentVertexCount;
        int vertexCount = originalVertices.length;

        for (float[] v : originalVertices) {
            vertexBuffer.add(padVertexData(v, structure));
        }

        currentVertexCount += vertexCount;
        isDirty = true;

        ModelAllocation alloc = new ModelAllocation(startOffset, vertexCount);

        // 如果模型支持索引，处理并记录所有分组的索引
        if (mesh instanceof IIndexedMesh) {
            IIndexedMesh indexedMesh = (IIndexedMesh) mesh;

            for (String group : indexedMesh.getGroupNames()) {
                int[] groupIndices = indexedMesh.getIndices(group);
                if (groupIndices != null && groupIndices.length > 0) {
                    int groupFirstIndex = currentIndexCount;
                    for (int idx : groupIndices) {
                        // 关键：将局部索引加上当前模型在巨型 VBO 中的起始偏移
                        indexBuffer.add(startOffset + idx);
                    }
                    int groupIndexCount = groupIndices.length;
                    currentIndexCount += groupIndexCount;
                    alloc.groups.put(group, new ModelGroupAllocation(groupFirstIndex, groupIndexCount));
                }
            }
        }

        allocationMap.put(identifier, alloc);
    }

    /**
     * 一次性上传数据
     */
    public void uploadToGPU() {
        if (!isDirty || VBO == null) {
            return;
        }

        // 上传 VBO
        int totalFloats = vertexBuffer.size() * 13;
        float[] allData = new float[totalFloats];
        int offset = 0;
        for (float[] v : vertexBuffer) {
            System.arraycopy(v, 0, allData, offset, 13);
            offset += 13;
        }
        VBO.uploadData(allData);

        // 上传 EBO
        if (!indexBuffer.isEmpty()) {
            IntBuffer iBuffer = BufferUtils.createIntBuffer(indexBuffer.size());
            for (int i : indexBuffer) {
                iBuffer.put(i);
            }
            iBuffer.flip();

            EBO.Bind();
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, iBuffer, GL15.GL_STATIC_DRAW);
            EBO.unBind();
        }

        isDirty = false;
        vertexBuffer.clear();
        indexBuffer.clear();
    }

    public void bind() {
        if (VAO != null) {
            VAO.Bind();
        }
    }

    public void unbind() {
        if (VAO != null) {
            VAO.Unbind();
        }
    }

    public ModelAllocation getAllocation(String identifier) {
        return allocationMap.get(identifier);
    }

    private float[] padVertexData(float[] input, VertexStructure structure) {
        float[] padded = new float[13];
        padded[3] = -1.0f; padded[4] = -1.0f; padded[5] = -1.0f; padded[6] = -1.0f;
        padded[7] = -1.0f; padded[8] = -1.0f;
        padded[9] = -1.0f; padded[10] = -1.0f; padded[11] = -1.0f;
        padded[12] = Float.intBitsToFloat(-1);

        padded[0] = input[0];
        padded[1] = input[1];
        padded[2] = input[2];

        if (structure == VertexStructure.POSITION) {
        } else if (structure == VertexStructure.POSITION_UV) {
            padded[7] = input[3]; padded[8] = input[4];
        } else if (structure == VertexStructure.POSITION_COLOR) {
            padded[3] = input[3]; padded[4] = input[4]; padded[5] = input[5]; padded[6] = input[6];
        } else if (structure == VertexStructure.POSITION_LIGHTMAP) {
            padded[12] = input[3];
        } else if (structure == VertexStructure.POSITION_UV_LIGHTMAP) {
            padded[7] = input[3]; padded[8] = input[4];
            padded[12] = input[5];
        } else if (structure == VertexStructure.POSITION_COLOR_LIGHTMAP) {
            padded[3] = input[3]; padded[4] = input[4]; padded[5] = input[5]; padded[6] = input[6];
            padded[12] = input[7];
        } else if (structure == VertexStructure.POSITION_UV_NORMAL) {
            padded[7] = input[3]; padded[8] = input[4];
            padded[9] = input[5]; padded[10] = input[6]; padded[11] = input[7];
        } else if (structure == VertexStructure.POSITION_UV_NORMAL_LIGHTMAP) {
            padded[7] = input[3]; padded[8] = input[4];
            padded[9] = input[5]; padded[10] = input[6]; padded[11] = input[7];
            padded[12] = input[8];
        }
        return padded;
    }
}
