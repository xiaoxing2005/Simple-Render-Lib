package com.korosensei.simplerenderlib.lib.internal.render.gl;

import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.DrawArraysIndirectCommand;
import com.korosensei.simplerenderlib.lib.internal.render.gl.structs.DrawElementsIndirectCommand;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL40;

import java.nio.IntBuffer;

public class IndirectBuffer extends GPUBuffer {

    public IndirectBuffer(int drawType) {
        super(GL40.GL_DRAW_INDIRECT_BUFFER, drawType);
    }

    public void uploadData(int... data) {
        if (data == null || data.length == 0) {
            allocate(0);
            return;
        }

        IntBuffer stage = stageInts(data.length);
        stage.put(data);
        stage.flip();
        try {
            upload(stage);
        } finally {
            clearIntStage();
        }
    }

    public void uploadData(DrawArraysIndirectCommand... commands) {
        int totalInts = commands == null ? 0 : commands.length * DrawArraysIndirectCommand.INTS;
        if (totalInts == 0) {
            allocate(0);
            return;
        }

        IntBuffer stage = stageInts(totalInts);
        for (DrawArraysIndirectCommand command : commands) {
            if (command != null) {
                command.writeTo(stage);
            } else {
                stage.position(stage.position() + DrawArraysIndirectCommand.INTS);
            }
        }
        stage.flip();
        try {
            upload(stage);
        } finally {
            clearIntStage();
        }
    }

    public void uploadData(DrawElementsIndirectCommand... commands) {
        int totalInts = commands == null ? 0 : commands.length * DrawElementsIndirectCommand.INTS;
        if (totalInts == 0) {
            allocate(0);
            return;
        }

        IntBuffer stage = stageInts(totalInts);
        for (DrawElementsIndirectCommand command : commands) {
            if (command != null) {
                command.writeTo(stage);
            } else {
                stage.position(stage.position() + DrawElementsIndirectCommand.INTS);
            }
        }
        stage.flip();
        try {
            upload(stage);
        } finally {
            clearIntStage();
        }
    }

    public void BufferSubData(long offsetBytes, int... data) {
        if (data == null || data.length == 0) {
            return;
        }

        IntBuffer stage = stageInts(data.length);
        stage.put(data);
        stage.flip();
        try {
            Bind();
            GL15.glBufferSubData(bufferType(), offsetBytes, stage);
            unBind();
        } finally {
            clearIntStage();
        }
    }

    public void BufferSubData(long offsetBytes, DrawArraysIndirectCommand... commands) {
        int totalInts = commands == null ? 0 : commands.length * DrawArraysIndirectCommand.INTS;
        if (totalInts == 0) {
            return;
        }

        IntBuffer stage = stageInts(totalInts);
        for (DrawArraysIndirectCommand command : commands) {
            if (command != null) {
                command.writeTo(stage);
            } else {
                stage.position(stage.position() + DrawArraysIndirectCommand.INTS);
            }
        }
        stage.flip();
        try {
            Bind();
            GL15.glBufferSubData(bufferType(), offsetBytes, stage);
            unBind();
        } finally {
            clearIntStage();
        }
    }

    public void BufferSubData(long offsetBytes, DrawElementsIndirectCommand... commands) {
        int totalInts = commands == null ? 0 : commands.length * DrawElementsIndirectCommand.INTS;
        if (totalInts == 0) {
            return;
        }

        IntBuffer stage = stageInts(totalInts);
        for (DrawElementsIndirectCommand command : commands) {
            if (command != null) {
                command.writeTo(stage);
            } else {
                stage.position(stage.position() + DrawElementsIndirectCommand.INTS);
            }
        }
        stage.flip();
        try {
            Bind();
            GL15.glBufferSubData(bufferType(), offsetBytes, stage);
            unBind();
        } finally {
            clearIntStage();
        }
    }
}
