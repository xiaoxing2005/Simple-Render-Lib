package com.korosensei.simplerenderlib.lib.internal.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.korosensei.simplerenderlib.lib.api.model.IIndexedMesh;
import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.VertexStructure;

/**
 * 基础模型类
 * 保存解析好的顶点数据和索引数据，用于后续的渲染。
 */
public class Model implements IIndexedMesh {

    private final Map<String, float[][]> groupedVertices;
    private final Map<String, int[]> groupedIndices;
    private final float[][] allVertices;
    private final int[] allIndices;
    private final VertexStructure structure;

    public Model(Map<String, float[][]> groupedVertices, Map<String, int[]> groupedIndices, float[][] allVertices,
        int[] allIndices, VertexStructure structure) {
        this.groupedVertices = new HashMap<>(groupedVertices);
        this.groupedIndices = new HashMap<>(groupedIndices);
        this.allVertices = allVertices;
        this.allIndices = allIndices;
        this.structure = structure;
    }

    @Override
    public float[][] getVertexs(String groupName) {
        return groupedVertices.get(groupName);
    }

    @Override
    public Set<String> getGroupNames() {
        return groupedVertices.keySet();
    }

    @Override
    public float[][] getVertexs() {
        return allVertices;
    }

    @Override
    public int[] getIndices() {
        return allIndices;
    }

    @Override
    public int[] getIndices(String groupName) {
        return groupedIndices.get(groupName);
    }

    @Override
    public VertexStructure getVertexStructure() {
        return structure;
    }
}
