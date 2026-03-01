package com.korosensei.simplerenderlib.lib.internal.model;

import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.VertexStructure;

import java.util.Map;

/**
 * 专为 OBJ 格式定制的模型类
 * 继承自基础 Model，并确保按组获取数据和索引的逻辑完善。
 */
public class OBJModel extends Model {

    public OBJModel(Map<String, float[][]> groupedVertices, Map<String, int[]> groupedIndices, float[][] allVertices, int[] allIndices, VertexStructure structure) {
        super(groupedVertices, groupedIndices, allVertices, allIndices, structure);
    }

    /**
     * 根据组名获取特定的索引数组
     * 这个方法重写了父类方法以提供更安全的后备机制：如果请求的组不存在，返回一个空的索引数组而不是 null
     */
    @Override
    public int[] getIndices(String groupName) {
        int[] indices = super.getIndices(groupName);
        if (indices == null) {
            return new int[0];
        }
        return indices;
    }

    /**
     * 根据组名获取特定的顶点数组
     * 同样提供安全返回机制
     */
    @Override
    public float[][] getVertexs(String groupName) {
        float[][] vertexs = super.getVertexs(groupName);
        if (vertexs == null) {
            return new float[0][];
        }
        return vertexs;
    }
}
