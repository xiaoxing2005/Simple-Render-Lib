#version 430 core

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inUV;
layout(location = 2) in int inBrightness;

out vec2 fragUV;
out vec2 fragLightmapUV;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(inPosition, 1.0);

    fragUV = inUV;

    float blockLight = float(inBrightness & 0xFFFF);
    float skyLight = float((inBrightness >> 16) & 0xFFFF);
    
    fragLightmapUV = vec2(blockLight / 256.0, skyLight / 256.0);
}
