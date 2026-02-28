#version 430 core

layout(location = 0) in vec3 inPos;
layout(location = 1) in vec2 inUv;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

out vec2 vUv;

void main() {
    gl_Position = projectionMatrix * modelViewMatrix * vec4(inPos, 1.0);
    vUv = inUv;
}
