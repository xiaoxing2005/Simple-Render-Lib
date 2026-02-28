#version 430 core

in vec2 vUv;

uniform vec4 uBrushColor;
uniform sampler2D uTexture;
uniform int uUseTexture;

out vec4 fragColor;

void main() {
    vec4 baseColor = uUseTexture == 1 ? texture(uTexture, vUv) : vec4(1.0);
    fragColor = baseColor * uBrushColor;
}
