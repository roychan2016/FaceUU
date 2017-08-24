package com.xuweichen.imagefilter.manager;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceView;

import com.xuweichen.imagefilter.utils.CameraUtils;

import java.io.IOException;

/**
 * Created by xuweichen on 2017/8/23.
 */

public class FaceCameraManager {
    private final static String TAG = FaceCameraManager.class.getSimpleName();

    private static class SingletonHolder {
        private static final FaceCameraManager INSTANCE = new FaceCameraManager();
    }
    public static FaceCameraManager Instance() {
        return SingletonHolder.INSTANCE;
    }

    private Camera camera = null;
    private boolean isFront = true;
    private int frontCameraId, backCameraId;
    private SurfaceTexture surfaceTexture;
    private SurfaceView surfaceView;

    private FaceCameraManager() {
        facingCamera();
    }

    public Camera getCamera(){
        return camera;
    }

    public boolean openFrontCamera() {
        isFront = true;
        return openCamera(frontCameraId);
    }

    public boolean openBackCamera() {
        isFront = false;
        return openCamera(backCameraId);
    }

    public void switchCamera(){
        releaseCamera();
        isFront = !isFront;
        openCamera(isFront ? frontCameraId : backCameraId);
        startPreview(surfaceTexture);
    }

    private boolean openCamera(int id){
        if(camera == null){
            try{
                camera = Camera.open(id);
                setDefaultParameters();
                return true;
            }catch(RuntimeException e){
                return false;
            }
        }
        return false;
    }

    public void releaseCamera(){
        if(camera != null){
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void startPreview(SurfaceTexture surfaceTexture){
        this.surfaceTexture = surfaceTexture;
        if(camera != null) {
            try {
                camera.setPreviewTexture(surfaceTexture);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startPreview(){
        if(camera != null) camera.startPreview();
    }

    public void stopPreview(){
        if(camera != null) camera.stopPreview();
    }

    public void setRotation(int rotation){
        if(camera != null) {
            Camera.Parameters params = camera.getParameters();
            params.setRotation(rotation);
            camera.setParameters(params);
        }
    }

    private void setDefaultParameters(){
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        Camera.Size previewSize = CameraUtils.getLargePreviewSize(camera);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        Camera.Size pictureSize = CameraUtils.getLargePictureSize(camera);
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        parameters.setRotation(90);
        camera.setParameters(parameters);
    }

    private void facingCamera() {
        boolean facedFrond = false;
        boolean facedBack = false;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontCameraId = i;
                facedFrond = true;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backCameraId = i;
                facedBack = true;
            }
            if (facedFrond && facedBack) break;
        }
    }
}
