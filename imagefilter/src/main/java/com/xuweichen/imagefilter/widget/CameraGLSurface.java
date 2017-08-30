package com.xuweichen.imagefilter.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import com.xuweichen.imagefilter.filter.base.GPUImageFilter;
import com.xuweichen.imagefilter.helper.SavePictureTask;
import com.xuweichen.imagefilter.manager.FaceCameraManager;
import com.xuweichen.imagefilter.utils.FaceCameraInfo;
import com.xuweichen.imagefilter.utils.OpenGLUtils;
import com.xuweichen.imagefilter.utils.Rotation;
import com.xuweichen.imagefilter.utils.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.R.attr.width;

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

        FaceCameraInfo info = FaceCameraManager.Instance().getCameraInfo();
        if(info.orientation == 90 || info.orientation == 270){
            imageWidth = info.previewHeight;
            imageHeight = info.previewWidth;
        }else{
            imageWidth = info.previewWidth;
            imageHeight = info.previewHeight;
        }
        GLES20.glViewport(0, 0, imageWidth, imageHeight);
        if (null != baseFilter) baseFilter.setTexelSize(imageWidth, imageHeight);
    }

    /******************************************************
     ************************* 美颜 ***********************
     ******************************************************/

    public void setBeautyLevel(int level) {
        if (null != baseFilter) baseFilter.setBeautyLevel(level);
    }


    /******************************************************
    ************************* 拍照 ************************
    ******************************************************/

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        FaceCameraManager.Instance().releaseCamera();
        if (null != baseFilter) baseFilter.destroy();
    }

    @Override
    public void savePicture(final SavePictureTask savePictureTask) {
        FaceCameraManager.Instance().takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FaceCameraManager.Instance().stopPreview();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap photo = drawPhoto(bitmap);
                        GLES20.glViewport(0, 0, imageWidth, imageHeight);
                        if (photo != null)
                            savePictureTask.execute(photo);
                    }
                });
                FaceCameraManager.Instance().startPreview();
            }
        });
    }

//    private Bitmap drawPhoto(Bitmap bitmap)
//    {
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int[] mFrameBuffers = new int[1];
//        int[] mFrameBufferTextures = new int[1];
//
//        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
//        GLES20.glGenTextures(1, mFrameBufferTextures, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
//                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
//                GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);
//
//        GLES20.glViewport(0, 0, width, height);
//        int textureId = OpenGLUtils.loadTexture(bitmap, OpenGLUtils.NO_TEXTURE, true);
//
//        FloatBuffer gLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        FloatBuffer gLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        gLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);
//        gLTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.ROTATION_270, false, true)).position(0);
//
//        baseFilter.drawFrame(textureId, gLCubeBuffer, gLTextureBuffer, true);
//
//        IntBuffer PixelBuffer = IntBuffer.allocate(width*height);
//        PixelBuffer.position(0);
//        GLES20.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, PixelBuffer);
//
//        PixelBuffer.position(0);//这里要把读写位置重置下
//        int pix[] = new int[width*height];
//        PixelBuffer.get(pix);//这是将intbuffer中的数据赋值到pix数组中
//
//        Bitmap result = Bitmap.createBitmap(pix, width, height,Bitmap.Config.ARGB_8888);//pix是上面读到的像素
//
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//        GLES20.glDeleteTextures(1, new int[]{textureId}, 0);
//        GLES20.glDeleteFramebuffers(mFrameBuffers.length, mFrameBuffers, 0);
//        GLES20.glDeleteTextures(mFrameBufferTextures.length, mFrameBufferTextures, 0);
//
//        return result;
//    }

    //上下左右翻转
    private Bitmap drawPhoto(Bitmap bitmap){
        Canvas canvas = new Canvas();
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(output);
        Matrix matrix = new Matrix();
        // 缩放 当sy为-1时向上翻转 当sx为-1时向左翻转 sx、sy都为-1时相当于旋转180°
        matrix.postScale(1, -1);
        // 因为向上翻转了所以y要向下平移一个bitmap的高度
        matrix.postTranslate(0, bitmap.getHeight());
        canvas.drawBitmap(bitmap, matrix, null);
        return output;
    }


}
