package com.xuweichen.faceuu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xuweichen.imagefilter.FaceEngine;
import com.xuweichen.imagefilter.widget.CameraGLSurface;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FaceEngine faceEngine;

    private CameraGLSurface cameraGLSurface;

    private ImageView photoButton;

    private ImageView beautifulButton;
    private ImageView filterButton;

    private LinearLayout beautifulPanel;
    private TextView noBeautiful;
    private TextView oneBeautiful;
    private TextView twoBeautiful;
    private TextView threeBeautiful;
    private TextView fourBeautiful;
    private TextView fiveBeautiful;

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
        cameraGLSurface.setOnClickListener(onClickListener);

        photoButton = (ImageView) findViewById(R.id.photo_button);
        photoButton.setOnClickListener(onClickListener);

        beautifulButton = (ImageView) findViewById(R.id.beautiful_button);
        beautifulButton.setOnClickListener(onClickListener);
        filterButton = (ImageView) findViewById(R.id.filter_button);
        filterButton.setOnClickListener(onClickListener);

        beautifulPanel = (LinearLayout) findViewById(R.id.beautiful_layout);
        oneBeautiful = (TextView) findViewById(R.id.one_beautiful);
        twoBeautiful = (TextView) findViewById(R.id.two_beautiful);
        threeBeautiful = (TextView) findViewById(R.id.three_beautiful);
        fourBeautiful = (TextView) findViewById(R.id.four_beautiful);
        fiveBeautiful = (TextView) findViewById(R.id.five_beautiful);
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
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
                case R.id.camera_surface:
                    showBeautifulPanel(false);
                    break;
                case R.id.beautiful_button:
                    showBeautifulPanel(true);
                    break;
                case R.id.filter_button:
                    Toast.makeText(MainActivity.this, "打开滤镜面板", Toast.LENGTH_SHORT).show();
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

    private void showBeautifulPanel(boolean show) {
        beautifulButton.setVisibility(show ? View.GONE : View.VISIBLE);
        filterButton.setVisibility(show ? View.GONE : View.VISIBLE);

        ScaleAnimation photoBtnAnim = new ScaleAnimation(
                show ? 1.0f : 0.75f, show ? 0.75f : 1.0f,
                show ? 1.0f : 0.75f, show ? 0.75f : 1.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f
        );
        photoBtnAnim.setDuration(500);
        photoBtnAnim.setFillAfter(true);
        photoButton.startAnimation(photoBtnAnim);

        TranslateAnimation beautifulPanelAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, show ? 1.0f : 0.0f,
                Animation.RELATIVE_TO_SELF, show ? 0.0f : 1.0f);
        beautifulPanelAnim.setDuration(500);
        beautifulPanelAnim.setFillAfter(true);

        beautifulPanel.setVisibility(View.VISIBLE);
        beautifulPanel.startAnimation(beautifulPanelAnim);
    }
}
