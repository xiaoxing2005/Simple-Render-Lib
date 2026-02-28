#version 430 core

out vec4 fragColor;

void main() {
    // 纯位置顶点，没有颜色和纹理，默认输出白色
    fragColor = vec4(1.0);
}
