package com.korosensei.simplerenderlib.lib.internal.model.loader;

import com.korosensei.simplerenderlib.lib.internal.model.OBJModel;
import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.VertexStructure;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 静态 OBJ 模型加载器
 * 解析坐标 (v) 和纹理 UV (vt)，不解析法线 (vn)。
 * 支持三角形和四边形面 (f) 的解析。
 * 支持通过 o 或 g 标签进行分组。
 */
public class ObjModelLoader {

    /**
     * 从 ResourceLocation 加载 OBJ 模型并返回一个包含顶点和索引的 OBJModel 对象。
     *
     * @param modelLocation 模型的资源路径
     * @return 解析完成的 OBJModel 对象
     */
    public static OBJModel loadModel(ResourceLocation modelLocation) {
        List<float[]> tempVertices = new ArrayList<>();
        List<float[]> tempUVs = new ArrayList<>();
        
        Map<String, List<float[]>> groupBuffers = new LinkedHashMap<>();
        String currentGroupName = "Default";
        groupBuffers.put(currentGroupName, new ArrayList<>());
        
        List<float[]> allVertexList = new ArrayList<>();

        boolean hasUV = false;

        try (InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(modelLocation).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // 忽略注释和空行
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                String[] tokens = line.split("\\s+");
                if (tokens.length == 0) continue;

                String type = tokens[0];

                switch (type) {
                    case "o":
                    case "g":
                        if (tokens.length > 1) {
                            currentGroupName = tokens[1];
                            groupBuffers.putIfAbsent(currentGroupName, new ArrayList<>());
                        }
                        break;
                    case "v":
                        // 解析顶点坐标: v x y z
                        if (tokens.length >= 4) {
                            float x = Float.parseFloat(tokens[1]);
                            float y = Float.parseFloat(tokens[2]);
                            float z = Float.parseFloat(tokens[3]);
                            tempVertices.add(new float[]{x, y, z});
                        }
                        break;
                    case "vt":
                        // 解析纹理 UV: vt u v
                        if (tokens.length >= 3) {
                            float u = Float.parseFloat(tokens[1]);
                            float v = Float.parseFloat(tokens[2]);
                            // Minecraft 的 V 轴通常与 OBJ 相反，需要翻转 (1.0 - v)
                            tempUVs.add(new float[]{u, 1.0f - v});
                            hasUV = true;
                        }
                        break;
                    case "f":
                        List<float[]> currentGroupBuffer = groupBuffers.get(currentGroupName);
                        
                        // 解析面: f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 (可能没有 vt 或 vn)
                        if (tokens.length == 4) {
                            // 三角形面
                            parseFaceVertex(tokens[1], tempVertices, tempUVs, currentGroupBuffer, allVertexList, hasUV);
                            parseFaceVertex(tokens[2], tempVertices, tempUVs, currentGroupBuffer, allVertexList, hasUV);
                            parseFaceVertex(tokens[3], tempVertices, tempUVs, currentGroupBuffer, allVertexList, hasUV);
                        } else if (tokens.length == 5) {
                            // 四边形面 (转换为两个三角形)
                            parseFaceVertex(tokens[1], tempVertices, tempUVs, currentGroupBuffer, allVertexList, hasUV);
                            parseFaceVertex(tokens[2], tempVertices, tempUVs, currentGroupBuffer, allVertexList, hasUV);
                            parseFaceVertex(tokens[3], tempVertices, tempUVs, currentGroupBuffer, allVertexList, hasUV);

                            parseFaceVertex(tokens[1], tempVertices, tempUVs, currentGroupBuffer, allVertexList, hasUV);
                            parseFaceVertex(tokens[3], tempVertices, tempUVs, currentGroupBuffer, allVertexList, hasUV);
                            parseFaceVertex(tokens[4], tempVertices, tempUVs, currentGroupBuffer, allVertexList, hasUV);
                        }
                        break;
                }
            }

            VertexStructure structure = hasUV ? VertexStructure.POSITION_UV : VertexStructure.POSITION;
            float[][] allVerticesData = allVertexList.toArray(new float[0][]);
            
            // 简单的线性索引生成 (0, 1, 2, 3...)
            int[] allIndicesData = new int[allVerticesData.length];
            for (int i = 0; i < allVerticesData.length; i++) {
                allIndicesData[i] = i;
            }

            Map<String, float[][]> groupedVertices = new HashMap<>();
            Map<String, int[]> groupedIndices = new HashMap<>();
            
            // 为了修复索引错误，我们需要知道每个组在全局顶点列表中的绝对位置
            int absoluteVertexIndex = 0;
            for (Map.Entry<String, List<float[]>> entry : groupBuffers.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    List<float[]> groupVerts = entry.getValue();
                    groupedVertices.put(entry.getKey(), groupVerts.toArray(new float[0][]));
                    
                    int[] groupInds = new int[groupVerts.size()];
                    for(int i = 0; i < groupVerts.size(); i++) {
                        // 这里的索引必须是相对于整个 allVerticesData 的绝对索引
                        // 因为我们在解析阶段是按组顺序往 allVertexList 里面添加顶点的，
                        // 所以可以简单地累加 absoluteVertexIndex
                        groupInds[i] = absoluteVertexIndex++;
                    }
                    groupedIndices.put(entry.getKey(), groupInds);
                }
            }

            return new OBJModel(groupedVertices, groupedIndices, allVerticesData, allIndicesData, structure);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load OBJ model: " + modelLocation, e);
        }
    }

    private static void parseFaceVertex(String faceToken, List<float[]> tempVertices, List<float[]> tempUVs, 
                                 List<float[]> currentGroupBuffer, List<float[]> allVertexList, boolean useUV) {
        String[] parts = faceToken.split("/");

        // OBJ 索引从 1 开始，需减 1 转为数组索引
        int vIndex = Integer.parseInt(parts[0]) - 1;
        float[] pos = tempVertices.get(vIndex);

        float[] finalVertex;
        if (useUV && parts.length > 1 && !parts[1].isEmpty()) {
            int vtIndex = Integer.parseInt(parts[1]) - 1;
            float[] uv = tempUVs.get(vtIndex);
            // 组装 POSITION_UV: [x, y, z, u, v]
            finalVertex = new float[]{pos[0], pos[1], pos[2], uv[0], uv[1]};
        } else {
            // 组装 POSITION: [x, y, z]
            finalVertex = new float[]{pos[0], pos[1], pos[2]};
        }
        
        currentGroupBuffer.add(finalVertex);
        allVertexList.add(finalVertex);
    }
}
