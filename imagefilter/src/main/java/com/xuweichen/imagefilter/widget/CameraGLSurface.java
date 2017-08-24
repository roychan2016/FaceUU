package com.xuweichen.imagefilter.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.xuweichen.imagefilter.Filter.base.GPUImageFilter;
import com.xuweichen.imagefilter.manager.FaceCameraManager;
import com.xuweichen.imagefilter.utils.OpenGLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by xuweichen on 2017/8/22.
 */

public class CameraGLSurface extends BaseGLSurface {
    private final static String TAG = CameraGLSurface.class.getSimpleName();

    public CameraGLSurface(Context context) {
        this(context, null);
    }

    public CameraGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.getHolder().addCallback(this);
    }

    private SurfaceTexture surfaceTexture;

    private GPUImageFilter baseFilter;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);

        if (null == baseFilter)
        {
            baseFilter = new GPUImageFilter();
        }

        if (textureId == OpenGLUtils.NO_TEXTURE)
            textureId = OpenGLUtils.getExternalOESTextureID();
        if (textureId != OpenGLUtils.NO_TEXTURE){
            surfaceTexture = new SurfaceTexture(textureId);
            surfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);
        } else {
            Log.e(TAG, "onSurfaceCreated OpenGLUtils.getExternalOESTextureID fail.");
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        super.onSurfaceChanged(gl, width, height);
        openCamera();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        super.onDrawFrame(gl);
        if(surfaceTexture == null)
            return;
        surfaceTexture.updateTexImage();
        float[] mtx = new float[16];
        surfaceTexture.getTransformMatrix(mtx);
        baseFilter.drawFrame(textureId);
    }

    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            requestRender();  //在数据准备好的时候通知Render绘制
        }
    };

    private void openCamera() {
        FaceCameraManager.Instance().openFrontCamera();
        FaceCameraManager.Instance().startPreview(surfaceTexture);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        FaceCameraManager.Instance().releaseCamera();
        if (null != baseFilter) baseFilter.destroy();
    }
}
