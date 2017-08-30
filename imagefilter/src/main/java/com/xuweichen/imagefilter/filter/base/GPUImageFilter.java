package com.xuweichen.imagefilter.filter.base;

import android.graphics.PointF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.xuweichen.imagefilter.R;
import com.xuweichen.imagefilter.utils.OpenGLUtils;
import com.xuweichen.imagefilter.utils.Rotation;
import com.xuweichen.imagefilter.utils.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

    private int mSingleStepOffsetLocation;
    private int mParamsLocation;

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

        OpenGLUtils.checkGlError("get param 1");

        mGLAttributeTextureCoordinate = GLES20.glGetAttribLocation(mGLProgramId,
                "inputTextureCoordinate");

        OpenGLUtils.checkGlError("get param 2");

        mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgramId, "inputImageTexture");

        OpenGLUtils.checkGlError("get param 3");

        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(mGLProgramId, "singleStepOffset");

        OpenGLUtils.checkGlError("get param 4");

        mParamsLocation = GLES20.glGetUniformLocation(mGLProgramId, "params");

        OpenGLUtils.checkGlError("get param 5");

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

        runAllSetOnDrawTask();

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

    public void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / w, 2.0f / h});
    }

    public void setBeautyLevel(int level){
        switch (level) {
            case 0:
                setFloat(mParamsLocation, 0.0f);
                break;
            case 1:
                setFloat(mParamsLocation, 1.0f);
                break;
            case 2:
                setFloat(mParamsLocation, 0.8f);
                break;
            case 3:
                setFloat(mParamsLocation,0.6f);
                break;
            case 4:
                setFloat(mParamsLocation, 0.4f);
                break;
            case 5:
                setFloat(mParamsLocation,0.33f);
                break;
            default:
                break;
        }
    }

    private Map<Integer, Runnable> synchronizedSetHashMap;

    private void runSetOnDraw(final int location, final Runnable runnable) {
        if (null == synchronizedSetHashMap)
            synchronizedSetHashMap = Collections.synchronizedMap(new HashMap<Integer, Runnable>());
        synchronizedSetHashMap.put(location, runnable);
    }

    private void runAllSetOnDrawTask() {
        if (null == synchronizedSetHashMap || synchronizedSetHashMap.isEmpty()) return;

        Iterator<Map.Entry<Integer, Runnable>> it = synchronizedSetHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Runnable> setRun = it.next();
            Runnable runnable = setRun.getValue();
            if (null != runnable) runnable.run();
            it.remove();
        }
    }

    protected void setInteger(final int location, final int intValue) {
        runSetOnDraw(location, new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1i(location, intValue);
            }
        });
    }

    protected void setFloat(final int location, final float floatValue) {
        runSetOnDraw(location, new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }

    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runSetOnDraw(location, new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec3(final int location, final float[] arrayValue) {
        runSetOnDraw(location, new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatVec4(final int location, final float[] arrayValue) {
        runSetOnDraw(location, new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setFloatArray(final int location, final float[] arrayValue) {
        runSetOnDraw(location, new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1fv(location, arrayValue.length, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void setPoint(final int location, final PointF point) {
        runSetOnDraw(location, new Runnable() {

            @Override
            public void run() {
                float[] vec2 = new float[2];
                vec2[0] = point.x;
                vec2[1] = point.y;
                GLES20.glUniform2fv(location, 1, vec2, 0);
            }
        });
    }

    protected void setUniformMatrix3f(final int location, final float[] matrix) {
        runSetOnDraw(location, new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0);
            }
        });
    }

    protected void setUniformMatrix4f(final int location, final float[] matrix) {
        runSetOnDraw(location, new Runnable() {

            @Override
            public void run() {
                GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0);
            }
        });
    }
}

