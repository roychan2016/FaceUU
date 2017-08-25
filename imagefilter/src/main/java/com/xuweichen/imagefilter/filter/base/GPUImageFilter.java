package com.xuweichen.imagefilter.filter.base;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.xuweichen.imagefilter.R;
import com.xuweichen.imagefilter.utils.OpenGLUtils;
import com.xuweichen.imagefilter.utils.Rotation;
import com.xuweichen.imagefilter.utils.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by xuweichen on 2017/8/23.
 */

public class GPUImageFilter {
    private final String mVertexShader;
    private final String mFragmentShader;
    protected int mGLProgramId;

    protected int mGLAttributePosition;
    protected int mGLAttributeTextureCoordinate;
    protected int mGLUniformTexture;

    protected FloatBuffer mGLVertexBuffer;
    protected FloatBuffer mGLTextureBuffer;

    public GPUImageFilter() {
        this(OpenGLUtils.readShaderFromRawResource(R.raw.default_vertex) ,
                OpenGLUtils.readShaderFromRawResource(R.raw.default_fragment));
    }

    public GPUImageFilter(final String vertexShader, final String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;

        init();
    }

    public void init() {
        mGLProgramId = OpenGLUtils.loadProgram(mVertexShader, mFragmentShader);

        OpenGLUtils.checkGlError("loadProgram");

        mGLAttributePosition = GLES20.glGetAttribLocation(mGLProgramId, "position");
        mGLAttributeTextureCoordinate = GLES20.glGetAttribLocation(mGLProgramId,
                "inputTextureCoordinate");
        mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgramId, "inputImageTexture");

        OpenGLUtils.checkGlError("get param");

        mGLVertexBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLVertexBuffer.put(TextureRotationUtil.CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.ROTATION_270, false, true)).position(0);

        OpenGLUtils.checkGlError("init buffer");
    }

    public final void destroy() {
        GLES20.glDeleteProgram(mGLProgramId);
    }

    public int drawFrame(int textureId) {
        return drawFrame(textureId, mGLVertexBuffer, mGLTextureBuffer, false);
    }

    public int drawFrame(int textureId, FloatBuffer vertexBuffer,
                           final FloatBuffer textureBuffer, boolean isPhoto) {
        GLES20.glUseProgram(mGLProgramId);

        OpenGLUtils.checkGlError("use program");

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttributePosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttributePosition);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttributeTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttributeTextureCoordinate);

        OpenGLUtils.checkGlError("set param");

        if (textureId != OpenGLUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            if(isPhoto)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            else
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

            GLES20.glUniform1i(mGLUniformTexture, 0);

            OpenGLUtils.checkGlError("Bind Texture");
        }

        onDrawArraysPre();

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        OpenGLUtils.checkGlError("Draw Arrays");

        GLES20.glDisableVertexAttribArray(mGLAttributePosition);
        GLES20.glDisableVertexAttribArray(mGLAttributeTextureCoordinate);

        OpenGLUtils.checkGlError("Disable Vertex Attribute Array");

        onDrawArraysAfter();

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        OpenGLUtils.checkGlError("Unbind Texture");

        return 0;
    }

    protected void onDrawArraysPre() {}
    protected void onDrawArraysAfter() {}
}

