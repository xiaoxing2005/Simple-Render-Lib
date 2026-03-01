package com.korosensei.simplerenderlib.lib.api.model;

import com.korosensei.simplerenderlib.lib.internal.model.IMesh;

/**
 * 带有索引的模型网格接口
 * 用于支持 Element Buffer Object (EBO/IBO) 的渲染模式。
 */
public interface IIndexedMesh extends IMesh {

    /**
     * 获取顶点的索引数组
     *
     * @return 包含所有顶点索引的整型数组
     */
    int[] getIndices();

    /**
     * 获取指定分组的顶点索引数组
     *
     * @param groupName 分组名称
     * @return 包含该分组所有顶点索引的整型数组。如果该组不存在则返回 null 或空数组。
     */
    int[] getIndices(String groupName);
}
