#version 430 core

in vec4 fragVertexColor;
in vec2 fragUV;
in vec2 fragLightmapUV;
in mat4 inversePV;
in vec3 vWorldPosition;
in vec2 u_Size;
in float u_Far;
flat in float vLightIntensity;

out vec4 fragColor;

uniform vec2 screenSize;

layout(binding = 0) uniform sampler2D baseTexture;
layout(binding = 1) uniform sampler2D lightmapTexture;
layout(binding = 2) uniform sampler2D depthTextur;

float u_Near = 0.05;

float LinearizeDepth(float nonLinearDepth) {
    float z_ndc = nonLinearDepth * 2.0 - 1.0; // 从 [0, 1] 映射到 NDC 的 [-1, 1]
    return (2.0 * u_Near * u_Far) / (u_Far + u_Near - z_ndc * (u_Far - u_Near));
}

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

    finalColor = vec4(1,1,1,0);

    vec2 screenUV = gl_FragCoord.xy / u_Size;
    float sceneZNonLinear = texture2D(depthTextur, screenUV).r;
    float sceneZ = LinearizeDepth(sceneZNonLinear);
    float shieldZNonLinear = gl_FragCoord.z;
    float shieldZ = LinearizeDepth(shieldZNonLinear);
    float depthDiff = sceneZ - shieldZ;

    float intensity = step(depthDiff, 0.04);

//    vec3 finalColor1 = vec3(1.0) + vec3(1.0) * intensity * 2.5;

//    float finalAlpha = finalColor.a + step(0.0,intensity);

    fragColor = finalColor + vec4(0,0,0,intensity);
//    fragColor = vec4(sceneZ,sceneZ,sceneZ,1);
}

