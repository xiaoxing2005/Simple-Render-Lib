package com.korosensei.simplerenderlib;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.VertexStructure;
import com.korosensei.simplerenderlib.lib.internal.render.renderer.GeneralRenderer;

public class TestTESR extends TileEntitySpecialRenderer {

    private GeneralRenderer renderer;
    private boolean initialized = false;
    private int lastLight = -1;

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!initialized) {
            initRenderData();
        }

        // 获取当前方块位置的动态光照值（天空光 + 方块光）
        int currentLight = tile.getWorldObj().getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord, tile.zCoord, 0);

        // 如果环境光照发生变化，则动态更新顶点数据中的亮度分量
        if (currentLight != lastLight) {
            updateTriangleLight(currentLight);
            lastLight = currentLight;
        }

        // 所有的矩阵变换我们都通过 Renderer 的矩阵栈来处理
        // 注意：在 render() 调用时，它会从 OpenGL 当前状态抓取 ViewMatrix
        // 在 TESR 开始时，GL_MODELVIEW_MATRIX 已经包含了相机的位移旋转
        
        renderer.pushMatrix();
        // 将相对玩家相机的坐标 (x, y, z) 应用到模型矩阵中
        renderer.translate((float) x, (float) y, (float) z);

        // 执行渲染操作
        // 内部会自动处理 Shader 绑定、矩阵上传以及渲染后的状态恢复
        renderer.render();

        // 恢复渲染器内部的矩阵栈
        renderer.popMatrix();
    }

    private void initRenderData() {
        renderer = new GeneralRenderer();
        // 初始化为 POSITION_COLOR_LIGHTMAP 结构
        renderer.init(VertexStructure.POSITION_COLOR_LIGHTMAP, GL11.GL_TRIANGLES, GL15.GL_STATIC_DRAW);
        initialized = true;
    }

    /**
     * 动态更新顶点缓存，将最新的光照值注入
     */
    private void updateTriangleLight(int light) {
        float packedLight = Float.intBitsToFloat(light);

        // 重新构建三角形的顶点数据（x, y, z, r, g, b, a, brightness）
        float[][] triangleVertices = {
            // 顶点 1：上方，红色
            { 0.5f, 1.0f, 0.5f,  1.0f, 0.0f, 0.0f, 1.0f, packedLight },
            // 顶点 2：左下，绿色
            { 0.0f, 0.0f, 0.5f,  0.0f, 1.0f, 0.0f, 1.0f, packedLight },
            // 顶点 3：右下，蓝色
            { 1.0f, 0.0f, 0.5f,  0.0f, 0.0f, 1.0f, 1.0f, packedLight }
        };

        // 清空渲染器之前的顶点缓存并添加新顶点
        renderer.clearVertices();
        renderer.addVertices(triangleVertices);
    }
}
