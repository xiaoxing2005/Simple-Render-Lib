package com.korosensei.simplerenderlib.lib.manager;

import static com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject.createUBO;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.korosensei.simplerenderlib.lib.internal.render.gl.BufferObject;
import com.korosensei.simplerenderlib.lib.internal.render.gl.ShaderProgram;

public class ShaderManager {

    public static ShaderManager INSTANCE;
    private final ArrayList<ShaderProgram> shaderList;



    private ShaderManager() {
        shaderList = new ArrayList<>();

    }

    public static void init() {
        INSTANCE = new ShaderManager();
    }

    public void registerShader(ShaderProgram shader) {
        shaderList.add(shader);
    }

    public ArrayList<ShaderProgram> getShaderList() {
        return shaderList;
    }

    public void updateUBO() {






    }
}
