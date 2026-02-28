#version 430 core

in vec2 fragUV;
out vec4 fragColor;

// 绑定基础纹理，通常在 0 号纹理单元
layout(binding = 0) uniform sampler2D baseTexture;

void main() {
    fragColor = texture(baseTexture, fragUV);
}
