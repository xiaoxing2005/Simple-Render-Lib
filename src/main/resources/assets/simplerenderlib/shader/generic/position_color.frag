#version 430 core

in vec4 vColor;
uniform vec4 uBrushColor;

out vec4 fragColor;

void main() {
    fragColor = vColor * uBrushColor;
}
