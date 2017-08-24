package com.xuweichen.imagefilter;

import com.xuweichen.imagefilter.utils.FaceHolder;
import com.xuweichen.imagefilter.widget.BaseGLSurface;

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
}
