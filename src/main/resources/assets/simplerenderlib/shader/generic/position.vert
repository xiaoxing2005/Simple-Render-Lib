#version 430 core

layout(location = 0) in vec3 inPos;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * modelViewMatrix * vec4(inPos, 1.0);
}
