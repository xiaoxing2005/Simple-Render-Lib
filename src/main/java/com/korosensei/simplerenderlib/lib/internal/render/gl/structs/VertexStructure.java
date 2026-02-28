package com.korosensei.simplerenderlib.lib.internal.render.gl.structs;

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
