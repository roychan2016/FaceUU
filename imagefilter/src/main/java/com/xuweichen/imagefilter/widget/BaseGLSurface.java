package com.xuweichen.imagefilter.widget;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.xuweichen.imagefilter.helper.SavePictureTask;
import com.xuweichen.imagefilter.utils.OpenGLUtils;
import com.xuweichen.imagefilter.utils.Rotation;
import com.xuweichen.imagefilter.utils.TextureRotationUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by xuweichen on 2017/8/22.
 */

public abstract class BaseGLSurface extends GLSurfaceView implements GLSurfaceView.Renderer {

    /**
     * GLSurfaceView的宽高
     */
    protected int surfaceWidth, surfaceHeight;
    /**
     * 图像宽高
     */
    protected int imageWidth, imageHeight;

    /**
     * SurfaceTexure纹理id
     */
    protected int textureId = OpenGLUtils.NO_TEXTURE;

    public BaseGLSurface(Context context) {
        this(context, null);
    }

    public BaseGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glDisable(GL10.GL_DITHER);
        GLES20.glClearColor(0,0, 0, 0);
        GLES20.glEnable(GL10.GL_CULL_FACE);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    public abstract void savePicture(SavePictureTask savePictureTask);
}
