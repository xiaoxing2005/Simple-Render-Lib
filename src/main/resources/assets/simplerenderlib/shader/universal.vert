#version 430 core

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec4 inColor;
layout(location = 2) in vec2 inUV;
layout(location = 3) in vec3 inNormal;
layout(location = 4) in int inBrightness;

out vec4 fragVertexColor;
out vec2 fragUV;
out vec2 fragLightmapUV;
flat out float vLightIntensity;

layout (std140, binding = 0) uniform Matrices
{
    mat4 projectionMatrix;
    mat4 viewMatrix;
};
uniform mat4 modelMatrix;
uniform int uBrightness; // Global brightness fallback

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(inPosition, 1.0);

    fragVertexColor = inColor;
    fragUV = inUV;

    int effectiveBrightness = inBrightness;
    if (effectiveBrightness == -1) {
        effectiveBrightness = uBrightness;
    }

    if (effectiveBrightness != -1) {
        float blockLight;
        float skyLight;

        if (effectiveBrightness <= 255) {
            blockLight = float(effectiveBrightness);
            skyLight = float(effectiveBrightness);
        } else {
            blockLight = float(effectiveBrightness & 0xFFFF);
            skyLight = float((effectiveBrightness >> 16) & 0xFFFF);
        }

        fragLightmapUV = vec2((blockLight + 8.0) / 256.0, (skyLight + 8.0) / 256.0);
    } else {
        fragLightmapUV = vec2(-1.0);
    }

    if (inNormal.x != -1.0 || inNormal.y != -1.0 || inNormal.z != -1.0) {
        vec3 worldNormal = normalize(mat3(modelMatrix) * inNormal);
        vec3 light0Dir = normalize(vec3(0.20000000298023224, 1.0, -0.699999988079071));
        vec3 light1Dir = normalize(vec3(-0.20000000298023224, 1.0, 0.699999988079071));

        float diffuse0 = max(dot(worldNormal, light0Dir), 0.0);
        float diffuse1 = max(dot(worldNormal, light1Dir), 0.0);

        // Ambient = 0.4, Diffuse = 0.6
        float intensity = 0.4 + 0.6 * diffuse0 + 0.6 * diffuse1;
        vLightIntensity = min(intensity, 1.0);
    } else {
        vLightIntensity = -1.0;
    }
}
