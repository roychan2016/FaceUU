package com.xuweichen.imagefilter;

import com.xuweichen.imagefilter.helper.SavePictureTask;
import com.xuweichen.imagefilter.helper.FaceHolder;
import com.xuweichen.imagefilter.widget.BaseGLSurface;
import com.xuweichen.imagefilter.widget.CameraGLSurface;

import java.io.File;

/**
 * Created by xuweichen on 2017/8/24.
 */

public class FaceEngine {
    private static FaceEngine faceEngine;

    private FaceEngine() {
        faceEngine = this;
    }

    public static class Builder{

        public static FaceEngine build(BaseGLSurface glSurface) {
            FaceHolder.context = glSurface.getContext();
            FaceHolder.glSurface = glSurface;
            return new FaceEngine();
        }
    }

    public static FaceEngine Instance(){
        if(faceEngine == null)
            throw new NullPointerException("FaceEngine must be built first");
        else
            return faceEngine;
    }

    public  void savePhoto(File file) {
        savePhoto(file, null);
    }

    public void savePhoto(File file, SavePictureTask.OnPictureSaveListener listener) {
        SavePictureTask savePictureTask = new SavePictureTask(file, listener);
        FaceHolder.glSurface.savePicture(savePictureTask);
    }

    public void setBeautyLevel(int level) {
        if (null != FaceHolder.glSurface
                && FaceHolder.glSurface instanceof CameraGLSurface
                && FaceHolder.beautyLevel != level) {
            FaceHolder.beautyLevel = level;
            ((CameraGLSurface) FaceHolder.glSurface).setBeautyLevel(level);
        }
    }
}
