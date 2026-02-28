package com.korosensei.simplerenderlib.lib.internal.render.gl;

import static com.korosensei.simplerenderlib.SimpleRenderLib.LOG;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ShaderProgram implements AutoCloseable {

    private final int programID;
    private boolean deleted;

    public ShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        final int vertexShader = compileShader(vertexShaderSource, GL20.GL_VERTEX_SHADER);
        final int fragmentShader = compileShader(fragmentShaderSource, GL20.GL_FRAGMENT_SHADER);
        this.programID = createProgram(vertexShader, fragmentShader);
    }

    public int getProgramID() {
        return programID;
    }

    public boolean isValid() {
        return programID != 0 && !deleted;
    }

    public void use() {
        if (isValid()) {
            GL20.glUseProgram(programID);
        }
    }

    public static void resetShader() {
        GL20.glUseProgram(0);
    }

    private static int compileShader(String shaderSource, int shaderType) {
        if (shaderSource == null || shaderSource.isEmpty()) {
            LOG.error("Shader source cannot be null or empty");
            return 0;
        }

        String code = loadFile(new ResourceLocation(shaderSource));
        if (code == null) {
            LOG.error("Failed to load shader source: {}", shaderSource);
            return 0;
        }

        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, code);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            LOG.error("Could not compile shader {}: {}", shaderSource, getShaderLogInfo(shader));
            GL20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    private static int createProgram(int vertexShader, int fragmentShader) {
        if (vertexShader == 0 || fragmentShader == 0) {
            LOG.error("Failed to compile shaders");
            return 0;
        }

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            LOG.error("Could not link shader: {}", getProgramLogInfo(program));
            GL20.glDeleteProgram(program);
            return 0;
        }

        GL20.glValidateProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
            LOG.error("Could not validate shader: {}", getProgramLogInfo(program));
            GL20.glDeleteProgram(program);
            return 0;
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
        return program;
    }

    private static String loadFile(ResourceLocation resourceLocation) {
        try {
            StringBuilder code = new StringBuilder();
            try (InputStream inputStream = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(resourceLocation)
                .getInputStream();
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    code.append(line)
                        .append('\n');
                }
            }
            return code.toString();
        } catch (Exception e) {
            LOG.error("Could not load shader file {}!", resourceLocation, e);
            return null;
        }
    }

    private static String getShaderLogInfo(int obj) {
        return GL20.glGetShaderInfoLog(obj, GL20.glGetShaderi(obj, GL20.GL_INFO_LOG_LENGTH));
    }

    private static String getProgramLogInfo(int obj) {
        return GL20.glGetProgramInfoLog(obj, GL20.glGetProgrami(obj, GL20.GL_INFO_LOG_LENGTH));
    }

    public void delete() {
        if (!deleted && programID != 0) {
            GL20.glDeleteProgram(programID);
            deleted = true;
        }
    }

    @Override
    public void close() {
        delete();
    }
}
