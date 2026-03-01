package com.korosensei.simplerenderlib.lib.internal.model;

import java.util.Set;

import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.VertexStructure;

/**
 * 基础网格接口
 * 定义了网格必须提供的顶点数据和顶点结构。
 */
public interface IMesh {

    /**
     * 获取指定分组的顶点数据数组
     *
     * @param groupName 分组名称 (OBJ 中的 o 或 g)
     * @return 包含该分组所有顶点数据的二维浮点数组。如果该组不存在则返回 null 或空数组。
     */
    float[][] getVertexs(String groupName);

    /**
     * 获取此模型中包含的所有分组名称
     */
    Set<String> getGroupNames();

    /**
     * 获取网格的顶点数据数组（所有分组的合并数据，或默认分组）
     *
     * @return 包含所有顶点数据的二维浮点数组
     */
    float[][] getVertexs();

    /**
     * 获取此网格对应的顶点结构定义
     *
     * @return 顶点结构 (例如 VertexStructure.POSITION_UV_NORMAL 等)
     */
    VertexStructure getVertexStructure();
}
