package com.xuweichen.faceuu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;

import com.xuweichen.imagefilter.FaceEngine;
import com.xuweichen.imagefilter.widget.CameraGLSurface;

public class MainActivity extends AppCompatActivity {

    private CameraGLSurface cameraGLSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        cameraGLSurface = (CameraGLSurface) findViewById(R.id.camera_surface);
        FaceEngine.Builder.build(cameraGLSurface);
        if (PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.CAMERA },
                    1);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length != 1 || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //cameraGLSurface.openCamera();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != cameraGLSurface) cameraGLSurface.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != cameraGLSurface) cameraGLSurface.onPause();
    }
}
