package com.xuweichen.faceuu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

import static com.xuweichen.imagefilter.helper.FaceHolder.beautyLevel;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private FaceEngine faceEngine;

    private CameraGLSurface cameraGLSurface;

    private ImageView photoButton;

    private ImageView beautAndFilterButton;
    private LinearLayout beautAndFilterPanel;

    private TextView sizeTab;
    private TextView beautyTab;
    private TextView filterTab;

    private LinearLayout sizePanel;
    private TextView[] sizeLevel;

    private LinearLayout beautyPanel;
    private TextView[] beautyLevel;

    private LinearLayout filterPanel;

    private int selectColor;
    private int defaultColor;

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
        defaultColor = Color.WHITE;
        selectColor = Color.rgb(0, 255, 255);

        cameraGLSurface = (CameraGLSurface) findViewById(R.id.camera_surface);
        faceEngine = FaceEngine.Builder.build(cameraGLSurface);
        cameraGLSurface.setOnClickListener(onClickListener);

        photoButton = (ImageView) findViewById(R.id.photo_button);
        photoButton.setOnClickListener(onClickListener);

        beautAndFilterButton = (ImageView) findViewById(R.id.beauty_and_filter_button);
        beautAndFilterButton.setOnClickListener(onClickListener);
        beautAndFilterPanel = (LinearLayout) findViewById(R.id.beauty_and_filter);
        beautAndFilterPanel.setOnClickListener(onClickListener);

        sizeTab = (TextView) findViewById(R.id.size_tab);
        sizeTab.setOnClickListener(onClickListener);
        beautyTab = (TextView) findViewById(R.id.beauty_tab);
        beautyTab.setOnClickListener(onClickListener);
        filterTab = (TextView) findViewById(R.id.filter_tab);
        filterTab.setOnClickListener(onClickListener);

        sizePanel = (LinearLayout) findViewById(R.id.size_layout);
        sizeLevel = new TextView[6];
        sizeLevel[0] = (TextView) findViewById(R.id.no_size);
        sizeLevel[1] = (TextView) findViewById(R.id.one_size);
        sizeLevel[2] = (TextView) findViewById(R.id.two_size);
        sizeLevel[3] = (TextView) findViewById(R.id.three_size);
        sizeLevel[4] = (TextView) findViewById(R.id.four_size);
        sizeLevel[5] = (TextView) findViewById(R.id.five_size);
        for (TextView sizeItem : sizeLevel) {
            sizeItem.setOnClickListener(onClickListener);
        }

        beautyPanel = (LinearLayout) findViewById(R.id.beauty_layout);
        beautyLevel = new TextView[6];
        beautyLevel[0] = (TextView) findViewById(R.id.no_beauty);
        beautyLevel[1] = (TextView) findViewById(R.id.one_beauty);
        beautyLevel[2] = (TextView) findViewById(R.id.two_beauty);
        beautyLevel[3] = (TextView) findViewById(R.id.three_beauty);
        beautyLevel[4] = (TextView) findViewById(R.id.four_beauty);
        beautyLevel[5] = (TextView) findViewById(R.id.five_beauty);
        for (TextView beautyItem : beautyLevel) {
            beautyItem.setOnClickListener(onClickListener);
        }

        filterPanel =  (LinearLayout) findViewById(R.id.filter_layout);
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
                    showBeautAndFilterPanel(false);
                    break;
                case R.id.beauty_and_filter_button:
                    showBeautAndFilterPanel(true);
                    break;
                case R.id.size_tab:
                    showBeautyOrFilter(0);
                    break;
                case R.id.filter_tab:
                    showBeautyOrFilter(1);
                    break;
                case R.id.beauty_tab:
                    showBeautyOrFilter(2);
                    break;
                case R.id.no_size:
                    setSizeLevel(0);
                    break;
                case R.id.one_size:
                    setSizeLevel(1);
                    break;
                case R.id.two_size:
                    setSizeLevel(2);
                    break;
                case R.id.three_size:
                    setSizeLevel(3);
                    break;
                case R.id.four_size:
                    setSizeLevel(4);
                    break;
                case R.id.five_size:
                    setSizeLevel(5);
                    break;
                case R.id.no_beauty:
                    setBeautyLevel(0);
                    break;
                case R.id.one_beauty:
                    setBeautyLevel(1);
                    break;
                case R.id.two_beauty:
                    setBeautyLevel(2);
                    break;
                case R.id.three_beauty:
                    setBeautyLevel(3);
                    break;
                case R.id.four_beauty:
                    setBeautyLevel(4);
                    break;
                case R.id.five_beauty:
                    setBeautyLevel(5);
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

    private void showBeautAndFilterPanel(boolean show) {
        beautAndFilterButton.setVisibility(show ? View.GONE : View.VISIBLE);
        beautAndFilterPanel.setVisibility(show ? View.VISIBLE : View.GONE);
        photoButton.setScaleX(show ? 0.75f : 1.0f);
        photoButton.setScaleY(show ? 0.75f : 1.0f);

//        //动画，但是有问题，显示不全，疑似超出按钮的范围无法显示
//        beautAndFilterPanel.animate()
//                .translationY(show ? 0 : beautAndFilterPanel.getHeight())
//                .setDuration(200)
//                .start();
//        photoButton.animate()
//                .scaleX(show ? 0.75f : 1.0f)
//                .scaleY(show ? 0.75f : 1.0f)
//                .setDuration(500)
//                .start();
//        ScaleAnimation photoBtnAnim = new ScaleAnimation(
//                show ? 1.0f : 0.75f, show ? 0.75f : 1.0f,
//                show ? 1.0f : 0.75f, show ? 0.75f : 1.0f,
//                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f
//        );
//        photoBtnAnim.setDuration(500);
//        photoBtnAnim.setFillAfter(true);
//        photoButton.startAnimation(photoBtnAnim);
//
//        TranslateAnimation BeautyPanelAnim = new TranslateAnimation(
//                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
//                0.0f, Animation.RELATIVE_TO_SELF, show ? 1.0f : 0.0f,
//                Animation.RELATIVE_TO_SELF, show ? 0.0f : 1.0f);
//        BeautyPanelAnim.setDuration(200);
//        BeautyPanelAnim.setFillAfter(true);
//        beautAndFilterPanel.setVisibility(View.VISIBLE);
//        beautAndFilterPanel.startAnimation(BeautyPanelAnim);
    }

    private void showBeautyOrFilter(int tabIndex) {
        sizeTab.setTextColor(tabIndex == 0 ? selectColor : defaultColor);
        filterTab.setTextColor(tabIndex == 1 ? selectColor : defaultColor);
        beautyTab.setTextColor(tabIndex == 2 ? selectColor : defaultColor);

        sizePanel.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        filterPanel.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        beautyPanel.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
    }

    private void setSizeLevel(int level) {
        for (int i=0; i<sizeLevel.length; i++) {
            boolean isLevel = i == level;
            if (i!=0) sizeLevel[i].setText(isLevel ? "V" : Integer.toString(i));
            sizeLevel[i].setTextColor(isLevel ? selectColor : defaultColor);
            sizeLevel[i].setBackgroundResource(isLevel ? R.drawable.blue_black_circle : R.drawable.white_black_circle);
        }

        Toast.makeText(this, "开启美形 级别："+level, Toast.LENGTH_SHORT).show();
    }

    private void setBeautyLevel(int level) {
        for (int i=0; i<beautyLevel.length; i++) {
            boolean isLevel = i == level;
            beautyLevel[i].setTextColor(isLevel ? selectColor : defaultColor);
            beautyLevel[i].setBackgroundResource(isLevel ? R.drawable.blue_black_circle : R.drawable.white_black_circle);
        }
        //这是美颜效果为Level级别
        if (null != faceEngine) faceEngine.setBeautyLevel(level);
        Toast.makeText(this, "开启美颜 级别："+level, Toast.LENGTH_SHORT).show();
    }
}
