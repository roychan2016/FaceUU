package com.xuweichen.faceuu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.xuweichen.imagefilter.FaceEngine;
import com.xuweichen.imagefilter.widget.CameraGLSurface;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.R.attr.mode;

public class MainActivity extends AppCompatActivity {
    private FaceEngine faceEngine;

    private CameraGLSurface cameraGLSurface;
    private ImageView photoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //检查权限
        if (PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.CAMERA },
                    0);
        }

        initView();
    }

    private void initView() {
        cameraGLSurface = (CameraGLSurface) findViewById(R.id.camera_surface);
        faceEngine = FaceEngine.Builder.build(cameraGLSurface);
        photoButton = (ImageView) findViewById(R.id.photo_button);
        photoButton.setOnClickListener(buttonClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if (null != cameraGLSurface) cameraGLSurface.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if (null != cameraGLSurface) cameraGLSurface.onPause();
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.photo_button:
                    //拍照
                    if (PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                1);
                    } else {
                        takePhoto();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void takePhoto() {
        if (null != faceEngine) faceEngine.savePhoto(getOutputMediaFile());
    }

    public File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "FaceCamera");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
