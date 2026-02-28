#version 430 core

in vec4 fragVertexColor;
out vec4 fragColor;

void main() {
    fragColor = fragVertexColor;
}
