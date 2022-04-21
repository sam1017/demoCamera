package com.example.myfacebeautycamera;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cgfay.facedetect.engine.FaceTracker;
import com.example.myfacebeautycamera.fragment.CameraPreviewFragment;

public class BeautyActivity extends AppCompatActivity {

    private CameraPreviewFragment mPreviewFragment;
    private static final String FRAGMENT_CAMERA = "fragment_camera";
    private static final String TAG = "BeautyActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Log.i(TAG,"onCreate savedInstanceState = " + savedInstanceState + " mPreviewFragment = " + mPreviewFragment);
        if (null == savedInstanceState && mPreviewFragment == null) {
            mPreviewFragment = new CameraPreviewFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, mPreviewFragment, FRAGMENT_CAMERA)
                    .commit();
        }
        faceTrackerRequestNetwork();
    }

    /**
     * 人脸检测SDK验证，可以替换成自己的SDK
     */
    private void faceTrackerRequestNetwork() {
        new Thread(() -> FaceTracker.requestFaceNetwork(BeautyActivity.this)).start();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

