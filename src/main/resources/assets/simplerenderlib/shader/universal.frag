#version 430 core

in vec4 fragVertexColor;
in vec2 fragUV;
in vec2 fragLightmapUV;
flat in float vLightIntensity;

out vec4 fragColor;

layout(binding = 0) uniform sampler2D baseTexture;
layout(binding = 1) uniform sampler2D lightmapTexture;

void main() {
    vec4 finalColor = vec4(1.0);

    if (fragUV.x != -1.0) {
        finalColor *= texture(baseTexture, fragUV);
    }

    if (fragVertexColor.r >= 0.0) {
        finalColor *= fragVertexColor;
    }

    if (fragLightmapUV.x != -1.0) {
        vec4 lightColor = texture(lightmapTexture, fragLightmapUV);
        finalColor.rgb *= lightColor.rgb;
    }

    if (vLightIntensity != -1.0) {
        finalColor.rgb *= vLightIntensity;
    }

    fragColor = finalColor;
}

