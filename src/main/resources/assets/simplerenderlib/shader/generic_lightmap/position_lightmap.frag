#version 430 core

in vec2 fragLightmapUV;
out vec4 fragColor;

// 绑定光照贴图，通常在 1 号纹理单元
layout(binding = 1) uniform sampler2D lightmapTexture;

void main() {
    // 采样光照贴图并作为颜色输出
    vec4 lightColor = texture(lightmapTexture, fragLightmapUV);
    // 这里因为只有光照，没有实际颜色，基础为白色，受光照影响
    fragColor = vec4(1.0) * lightColor;
}
