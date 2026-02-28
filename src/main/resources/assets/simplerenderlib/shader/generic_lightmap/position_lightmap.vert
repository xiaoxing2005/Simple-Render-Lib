#version 430 core

layout(location = 0) in vec3 inPosition;
layout(location = 1) in int inBrightness;

out vec2 fragLightmapUV;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

// 1.7.10 原版计算亮度的解包：低16位是 Block Light，高16位是 Sky Light
// 但传入到着色器中的坐标为了对应光照贴图（256x256，但实际UV范围常为0~1）
// 我们需要将光照值从 0-240 映射到纹理UV。通常的做法是除以256或者65536的倍数。
// Minecraft 中常用 OpenGlHelper.setLightmapTextureCoords 
// 对于 Shader 来说，光照 UV 一般是 vec2( (light % 65536) / 256.0, (light / 65536) / 256.0 ) 
// 注意在旧版 OpenGL，光照贴图通常绑定在 GL_TEXTURE1

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(inPosition, 1.0);

    // 解包亮度值为 Lightmap UV (0.0 到 1.0 的范围)
    // 根据原版算法：
    // short skyLight = (inBrightness >> 16) & 0xFFFF;
    // short blockLight = inBrightness & 0xFFFF;
    // 原版 UV 取值通常是从 0 到 240
    float blockLight = float(inBrightness & 0xFFFF);
    float skyLight = float((inBrightness >> 16) & 0xFFFF);
    
    // 转换为纹理坐标 (0.0 - 1.0)
    fragLightmapUV = vec2(blockLight / 256.0, skyLight / 256.0);
}
