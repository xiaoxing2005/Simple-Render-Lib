package com.korosensei.simplerenderlib.lib.internal.render.gl;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL43;

import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.InstanceData;

public class BufferObject extends GPUBuffer {

    public BufferObject(int drawType, int bufferType) {
        super(bufferType, drawType);
    }

    public static BufferObject createVBO(int drawType) {
        return new BufferObject(drawType, GL15.GL_ARRAY_BUFFER);
    }

    public static BufferObject createEBO(int drawType) {
        return new BufferObject(drawType, GL15.GL_ELEMENT_ARRAY_BUFFER);
    }

    public static BufferObject createUBO(int drawType) {
        return new BufferObject(drawType, GL31.GL_UNIFORM_BUFFER);
    }

    public static BufferObject createSSBO(int drawType) {
        return new BufferObject(drawType, GL43.GL_SHADER_STORAGE_BUFFER);
    }

    public void uploadData(float... data) {
        if (data == null || data.length == 0) {
            allocate(0);
            return;
        }

        FloatBuffer stage = stageFloats(data.length);
        stage.put(data);
        stage.flip();
        try {
            upload(stage);
        } finally {
            clearFloatStage();
        }
    }

    public void uploadBuffer(FloatBuffer data) {
        if (data == null || data.capacity() == 0) {
            allocate(0);
            return;
        }

        data.rewind();
        try {
            upload(data);
        } finally {
            clearFloatStage();
        }
    }

    public void uploadData(InstanceData... instances) {
        int totalFloats = InstanceData.totalFloats(instances);
        if (totalFloats == 0) {
            allocate(0);
            return;
        }

        FloatBuffer stage = stageFloats(totalFloats);
        for (InstanceData instance : instances) {
            if (instance != null) {
                assert stage != null;
                instance.writeTo(stage);
            }
        }
        stage.flip();
        try {
            upload(stage);
        } finally {
            clearFloatStage();
        }
    }

    public void BufferSubData(long offsetBytes, float... data) {
        if (data == null || data.length == 0) {
            return;
        }

        FloatBuffer stage = stageFloats(data.length);
        stage.put(data);
        stage.flip();
        try {
            Bind();
            GL15.glBufferSubData(bufferType(), offsetBytes, stage);
            unBind();
        } finally {
            clearFloatStage();
        }
    }

    public void BufferSubData(long offsetBytes, InstanceData... instances) {
        int totalFloats = InstanceData.totalFloats(instances);
        if (totalFloats == 0) {
            return;
        }

        FloatBuffer stage = stageFloats(totalFloats);
        for (InstanceData instance : instances) {
            if (instance != null) {
                instance.writeTo(stage);
            }
        }
        stage.flip();
        try {
            Bind();
            GL15.glBufferSubData(bufferType(), offsetBytes, stage);
            unBind();
        } finally {
            clearFloatStage();
        }
    }
}
