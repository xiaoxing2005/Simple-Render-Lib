#version 430 core

in vec2 fragUV;
in vec2 fragLightmapUV;
out vec4 fragColor;

// 绑定基础纹理，通常在 0 号纹理单元
layout(binding = 0) uniform sampler2D baseTexture;
// 绑定光照贴图，通常在 1 号纹理单元
layout(binding = 1) uniform sampler2D lightmapTexture;

void main() {
    // 采样基础纹理
    vec4 texColor = texture(baseTexture, fragUV);
    // 采样光照贴图
    vec4 lightColor = texture(lightmapTexture, fragLightmapUV);
    
    // 混合纹理颜色与光照颜色
    fragColor = texColor * lightColor;
}
