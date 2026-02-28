#version 430 core
layout(location = 0) in vec3 inPos;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
struct InstanceData {
    vec4 offset;
    vec4 color;
};
layout(std430, binding = 0) readonly buffer InstanceBuffer {
    InstanceData instances[];
};
out vec4 vColor;
void main() {
    InstanceData data = instances[gl_InstanceID];
    vec3 finalPos = inPos + data.offset.xyz;
    gl_Position = projectionMatrix * modelViewMatrix * vec4(finalPos, 1.0);
    vColor = data.color;
}
