#version 430 core

in vec4 fragVertexColor;
in vec2 fragLightmapUV;
out vec4 fragColor;

// 绑定光照贴图，通常在 1 号纹理单元
layout(binding = 1) uniform sampler2D lightmapTexture;

void main() {
    // 采样光照贴图
    vec4 lightColor = texture(lightmapTexture, fragLightmapUV);
    
    // 混合顶点颜色与光照颜色
    fragColor = fragVertexColor * lightColor;
}
