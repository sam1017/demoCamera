package com.example.myfacebeautycamera.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.myfacebeautycamera.camera.CameraParam;
import com.example.myfacebeautycamera.presenter.CameraPreviewPresenter;

public class CameraPreviewFragment extends Fragment {

    private static final String TAG = "CameraPreviewFragment";
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final String FRAGMENT_DIALOG = "dialog";

    // 预览参数
    private CameraParam mCameraParam;

    private final Handler mMainHandler;
    private FragmentActivity mActivity;
    private CameraPreviewPresenter mPreviewPresenter;

    public CameraPreviewFragment() {
        mCameraParam = CameraParam.getInstance();
        mMainHandler = new Handler(Looper.getMainLooper());
        mPreviewPresenter = new CameraPreviewPresenter(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (FragmentActivity) context;
        } else {
            mActivity = getActivity();
        }
        mPreviewPresenter.onAttach(mActivity);
        Log.d(TAG, "onAttach: ");
    }
}
