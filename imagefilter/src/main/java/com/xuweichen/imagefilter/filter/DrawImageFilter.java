package com.xuweichen.imagefilter.filter;

import android.graphics.PointF;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.xuweichen.imagefilter.R;
import com.xuweichen.imagefilter.filter.base.GPUImageFilter;
import com.xuweichen.imagefilter.helper.FaceHolder;
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
 * Created by xuweichen on 2017/8/30.
 */

public class DrawImageFilter extends GPUImageFilter{
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;

    public DrawImageFilter(){
        super(NO_FILTER_VERTEX_SHADER ,
                OpenGLUtils.readShaderFromRawResource(R.raw.beauty));
    }

    protected void onInit() {
        super.onInit();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
        setBeautyLevel(FaceHolder.beautyLevel);
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / w, 2.0f / h});
    }

    @Override
    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    public void setBeautyLevel(int level){
        switch (level) {
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

    public void onBeautyLevelChanged(){
        setBeautyLevel(FaceHolder.beautyLevel);
    }
}
