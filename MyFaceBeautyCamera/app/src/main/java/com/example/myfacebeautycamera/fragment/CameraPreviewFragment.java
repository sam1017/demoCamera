package com.example.myfacebeautycamera.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.cgfay.uitls.utils.BrightnessUtils;
import com.cgfay.uitls.utils.PermissionUtils;
import com.cgfay.uitls.widget.RoundOutlineProvider;
import com.example.myfacebeautycamera.R;
import com.example.myfacebeautycamera.camera.CameraParam;
import com.example.myfacebeautycamera.model.GalleryType;
import com.example.myfacebeautycamera.presenter.CameraPreviewPresenter;
import com.example.myfacebeautycamera.widget.CameraMeasureFrameLayout;
import com.example.myfacebeautycamera.widget.CameraPreviewTopbar;
import com.example.myfacebeautycamera.widget.CameraTextureView;
import com.example.myfacebeautycamera.widget.PreviewMeasureListener;
import com.example.myfacebeautycamera.widget.RecordButton;
import com.example.myfacebeautycamera.widget.RecordCountDownView;
import com.example.myfacebeautycamera.widget.RecordProgressView;
import com.example.myfacebeautycamera.widget.RecordSpeedLevelBar;

public class CameraPreviewFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "CameraPreviewFragment";
    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final String FRAGMENT_DIALOG = "dialog";
    private static final boolean VERBOSE = true;

    private static final int ALBUM_LOADER_ID = 1;
    // ????????????
    private CameraParam mCameraParam;

    private final Handler mMainHandler;
    private FragmentActivity mActivity;
    private CameraPreviewPresenter mPreviewPresenter;

    // Fragment?????????
    private View mContentView;

    // ????????????????????????
    private LoaderManager mLocalImageLoader;

    // ????????????
    private CameraMeasureFrameLayout mPreviewLayout;
    private CameraTextureView mCameraTextureView;

    private FrameLayout mFragmentContainer;
    // fps??????
    private TextView mFpsView;
    // ???????????????
    private RecordCountDownView mCountDownView;
    // ??????topbar
    private CameraPreviewTopbar mPreviewTopbar;
    // ???????????????
    private RecordSpeedLevelBar mSpeedBar;
    private boolean mSpeedBarShowing;
    // ????????????
    private LinearLayout mBtnStickers;
    // ????????????
    private RecordButton mBtnRecord;
    private View mLayoutMedia;
    private LinearLayout mLayoutDelete;
    //private CameraTabView mCameraTabView;
    private View mTabIndicator;
    private Button mBtnNext;
    private Button mBtnDelete;
    private RecordProgressView mProgressView;
    // ???????????????
    private ImageView mBtnMedia;

    public CameraPreviewFragment() {
        Log.i(TAG,"CameraPreviewFragment");
        mCameraParam = CameraParam.getInstance();
        mMainHandler = new Handler(Looper.getMainLooper());
        mPreviewPresenter = new CameraPreviewPresenter(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
        if (context instanceof Activity) {
            mActivity = (FragmentActivity) context;
        } else {
            mActivity = getActivity();
        }
        mPreviewPresenter.onAttach(mActivity);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        mPreviewPresenter.onCreate();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_camera_preview, container, false);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isCameraEnable()) {
            initView(mContentView);
        } else {
            PermissionUtils.requestCameraPermission(this);
        }

        if (PermissionUtils.permissionChecking(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            mLocalImageLoader = LoaderManager.getInstance(this);
            //mLocalImageLoader.initLoader(ALBUM_LOADER_ID, null, this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mPreviewPresenter.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        enhancementBrightness();
        mPreviewPresenter.onResume();
        Log.d(TAG, "onResume: ");
    }

    /**
     * ????????????
     */
    private void enhancementBrightness() {
        BrightnessUtils.setWindowBrightness(mActivity, mCameraParam.luminousEnhancement
                ? BrightnessUtils.MAX_BRIGHTNESS : mCameraParam.brightness);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        mPreviewPresenter.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        mPreviewPresenter.onStop();
    }

    /**
     * ???????????????
     * @param view
     */
    private void initView(View view) {
        Log.i(TAG,"initView");
        initPreviewSurface();
        initPreviewTopbar();
        initBottomLayout(view);
        //initCameraTabView();
        mCameraParam.mGalleryType = GalleryType.PICTURE;
        if (!isStorageEnable()) {
            PermissionUtils.requestRecordSoundPermission(CameraPreviewFragment.this);
        }
        if (mBtnRecord != null) {
            mBtnRecord.setRecordEnable(false);
        }

    }


    /**
     * ?????????????????????
     * @param view
     */
    private void initBottomLayout(@NonNull View view) {
        mFragmentContainer = view.findViewById(R.id.fragment_bottom_container);
        mSpeedBar = view.findViewById(R.id.record_speed_bar);
        mSpeedBar.setOnSpeedChangedListener((speed) -> {
            //mPreviewPresenter.setSpeedMode(SpeedMode.valueOf(speed.getSpeed()));
        });

        mBtnStickers = view.findViewById(R.id.btn_stickers);
        mBtnStickers.setOnClickListener(this);
        mLayoutMedia = view.findViewById(R.id.layout_media);
        mBtnMedia = view.findViewById(R.id.btn_media);
        mBtnMedia.setOnClickListener(this);

        mBtnRecord = view.findViewById(R.id.btn_record);
        mBtnRecord.setOnClickListener(this);
        mBtnRecord.addRecordStateListener(mRecordStateListener);

        mLayoutDelete = view.findViewById(R.id.layout_delete);
        mBtnDelete = view.findViewById(R.id.btn_record_delete);
        mBtnDelete.setOnClickListener(this);
        mBtnNext = view.findViewById(R.id.btn_goto_edit);
        mBtnNext.setOnClickListener(this);

        setShowingSpeedBar(mSpeedBarShowing);
    }

    /**
     * ?????????????????????
     */
    private RecordButton.RecordStateListener mRecordStateListener = new RecordButton.RecordStateListener() {
        @Override
        public void onRecordStart() {
            mPreviewPresenter.startRecord();
        }

        @Override
        public void onRecordStop() {
            mPreviewPresenter.stopRecord();
        }

        @Override
        public void onZoom(float percent) {

        }
    };

    /**
     * ???????????????topbar
     */
    private void initPreviewTopbar() {
        mPreviewTopbar = mContentView.findViewById(R.id.camera_preview_topbar);
        mPreviewTopbar.addOnCameraCloseListener(this::closeCamera)
                .addOnCameraSwitchListener(this::switchCamera)
                .addOnShowPanelListener(type -> {
                    switch (type) {
                        case CameraPreviewTopbar.PanelMusic: {
                            //openMusicPicker();
                            Log.i(TAG,"CameraPreviewTopbar.PanelMusic");
                            break;
                        }

                        case CameraPreviewTopbar.PanelSpeedBar: {
                            setShowingSpeedBar(mSpeedBar.getVisibility() != View.VISIBLE);
                            break;
                        }

                        case CameraPreviewTopbar.PanelFilter: {
                            //showEffectFragment();
                            Log.i(TAG,"CameraPreviewTopbar.PanelFilter");
                            break;
                        }

                        case CameraPreviewTopbar.PanelSetting: {
                            //showSettingFragment();
                            Log.i(TAG,"CameraPreviewTopbar.PanelSetting");
                            break;
                        }
                    }
                });
    }

    /**
     * ????????????
     */
    private void closeCamera() {
        if (mActivity != null) {
            mActivity.finish();
            //mActivity.overridePendingTransition(0, R.anim.anim_slide_down);
        }
    }

    /**
     * ????????????
     */
    private void switchCamera() {
        if (!isCameraEnable()) {
            PermissionUtils.requestCameraPermission(this);
            return;
        }
        mPreviewPresenter.switchCamera();
    }

    private void initPreviewSurface() {
        mFpsView = mContentView.findViewById(R.id.tv_fps);
        mPreviewLayout = mContentView.findViewById(R.id.layout_camera_preview);
        mCameraTextureView = new CameraTextureView(mActivity);
        mCameraTextureView.addOnTouchScroller(mTouchScroller);
        mCameraTextureView.addMultiClickListener(mMultiClickListener);
        mCameraTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mPreviewLayout.addView(mCameraTextureView);

        // ??????????????????
        if (Build.VERSION.SDK_INT >= 21) {
            mCameraTextureView.setOutlineProvider(new RoundOutlineProvider(getResources().getDimension(com.cgfay.utilslibrary.R.dimen.dp7)));
            mCameraTextureView.setClipToOutline(true);
        }
        mPreviewLayout.setOnMeasureListener(new PreviewMeasureListener(mPreviewLayout));
        mProgressView = mContentView.findViewById(R.id.record_progress);
        mCountDownView = mContentView.findViewById(R.id.count_down_view);
    }


    // ---------------------------- TextureView SurfaceTexture?????? ---------------------------------
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.onSurfaceCreated(surface);
            mPreviewPresenter.onSurfaceChanged(width, height);
            Log.d(TAG, "onSurfaceTextureAvailable: width = " + width + " height = " + height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mPreviewPresenter.onSurfaceChanged(width, height);
            Log.d(TAG, "onSurfaceTextureSizeChanged: width = " + width + " height = " + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mPreviewPresenter.onSurfaceDestroyed();
            Log.d(TAG, "onSurfaceTextureDestroyed: ");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    // ------------------------------- TextureView ????????????????????? ----------------------------------
    private CameraTextureView.OnTouchScroller mTouchScroller = new CameraTextureView.OnTouchScroller() {

        @Override
        public void swipeBack() {
            mPreviewPresenter.nextFilter();
        }

        @Override
        public void swipeFrontal() {
            mPreviewPresenter.previewFilter();
        }

        @Override
        public void swipeUpper(boolean startInLeft, float distance) {
            if (VERBOSE) {
                Log.d(TAG, "swipeUpper, startInLeft ? " + startInLeft + ", distance = " + distance);
            }
        }

        @Override
        public void swipeDown(boolean startInLeft, float distance) {
            if (VERBOSE) {
                Log.d(TAG, "swipeDown, startInLeft ? " + startInLeft + ", distance = " + distance);
            }
        }

    };

    /**
     * ?????????????????????
     */
    private CameraTextureView.OnMultiClickListener mMultiClickListener = new CameraTextureView.OnMultiClickListener() {

        @Override
        public void onSurfaceSingleClick(final float x, final float y) {
            // ????????????Fragment
            if (onBackPressed()) {
                return;
            }

            // ?????????????????????????????????????????????????????????????????????
            if (mCameraParam.touchTake) {
                takePicture();
                return;
            }

            // todo ??????????????????????????????

        }

        @Override
        public void onSurfaceDoubleClick(float x, float y) {
            switchCamera();
        }

    };


    /**
     * ??????
     */
    private void takePicture() {
        Log.i(TAG,"takePicture");
        if (isStorageEnable()) {
            Log.i(TAG,"mCameraParam.mGalleryType = " + mCameraParam.mGalleryType);
            if (mCameraParam.mGalleryType == GalleryType.PICTURE) {
                if (mCameraParam.takeDelay) {
                    mCountDownView.addOnCountDownListener(new RecordCountDownView.OnCountDownListener() {
                        @Override
                        public void onCountDownEnd() {
                            mPreviewPresenter.takePicture();
                            resetAllLayout();
                        }

                        @Override
                        public void onCountDownCancel() {
                            resetAllLayout();
                        }
                    });
                    mCountDownView.start();
                    hideAllLayout();
                } else {
                    Log.i(TAG,"mPreviewPresenter.takePicture()");
                    mPreviewPresenter.takePicture();
                }
            }
        } else {
            PermissionUtils.requestStoragePermission(this);
        }
    }

    /**
     * ??????????????????
     */
    private void hideAllLayout() {
        mMainHandler.post(()-> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.hideAllView();
            }
            if (mSpeedBar != null) {
                mSpeedBar.setVisibility(View.GONE);
            }
            if (mBtnStickers != null) {
                mBtnStickers.setVisibility(View.GONE);
            }
            if (mBtnRecord != null) {
                mBtnRecord.setVisibility(View.GONE);
            }
            if (mLayoutMedia != null) {
                mLayoutMedia.setVisibility(View.GONE);
            }
            if (mLayoutDelete != null) {
                mLayoutDelete.setVisibility(View.GONE);
            }
/*            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.GONE);
            }*/
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.GONE);
            }
        });
    }


    /**
     * ??????????????????
     */
    public void resetAllLayout() {
        mMainHandler.post(()-> {
            if (mPreviewTopbar != null) {
                mPreviewTopbar.resetAllView();
            }
            setShowingSpeedBar(mSpeedBarShowing);
            if (mBtnStickers != null) {
                mBtnStickers.setVisibility(View.VISIBLE);
            }
            if (mBtnRecord != null) {
                mBtnRecord.setVisibility(View.VISIBLE);
            }
            if (mLayoutDelete != null) {
                mLayoutDelete.setVisibility(View.VISIBLE);
            }
/*            if (mCameraTabView != null) {
                mCameraTabView.setVisibility(View.VISIBLE);
            }*/
            if (mTabIndicator != null) {
                mTabIndicator.setVisibility(View.VISIBLE);
            }
            resetDeleteButton();
            if (mBtnRecord != null) {
                mBtnRecord.reset();
            }
        });
    }

    /**
     * ??????????????????
     */
    private void resetDeleteButton() {
        boolean hasRecordVideo = (mPreviewPresenter.getRecordedVideoSize() > 0);
        if (mBtnNext != null) {
            mBtnNext.setVisibility(hasRecordVideo ? View.VISIBLE : View.GONE);
        }
        if (mBtnDelete != null) {
            mBtnDelete.setVisibility(hasRecordVideo ? View.VISIBLE : View.GONE);
        }
        if (mLayoutMedia != null) {
            mLayoutMedia.setVisibility(hasRecordVideo ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * ?????????????????????
     * @param show
     */
    private void setShowingSpeedBar(boolean show) {
        mSpeedBarShowing = show;
        mSpeedBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mPreviewTopbar.setSpeedBarOpen(show);
    }

    /**
     * ????????????????????????????????????
     * @return
     */
    private boolean isStorageEnable() {
        return PermissionUtils.permissionChecking(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * ????????????????????????
     * @return ??????????????????????????????
     */
    public boolean onBackPressed() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        if (fragment != null) {
            //hideFragmentAnimating();
            return true;
        }

        // ?????????
        if (mCountDownView != null && mCountDownView.isCountDowning()) {
            mCountDownView.cancel();
            return true;
        }
        return false;
    }

    /**
     * ??????????????????
     * @return
     */
    private boolean isCameraEnable() {
        return PermissionUtils.permissionChecking(mActivity, Manifest.permission.CAMERA);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_stickers) {
            //showStickers();
        } else if (i == R.id.btn_media) {
            //openMediaPicker();
        } else if (i == R.id.btn_record) {
            Log.i(TAG,"onClick btn_record");
            takePicture();
        } else if (i == R.id.btn_record_delete) {
           // deleteRecordedVideo();
        } else if (i == R.id.btn_goto_edit) {
            //stopRecordOrPreviewVideo();
        }
    }

    /**
     * ????????????????????????
     */
 /*   private void showStickers() {
        if (mFragmentAnimating) {
            return;
        }
        if (mResourcesFragment == null) {
            mResourcesFragment = new PreviewResourceFragment();
        }
        mResourcesFragment.addOnChangeResourceListener((data) -> {
            mPreviewPresenter.changeResource(data);
        });
        if (!mResourcesFragment.isAdded()) {
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_bottom_container, mResourcesFragment, FRAGMENT_TAG)
                    .commitAllowingStateLoss();
        } else {
            getChildFragmentManager()
                    .beginTransaction()
                    .show(mResourcesFragment)
                    .commitAllowingStateLoss();
        }
        showFragmentAnimating(false);
    }*/


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //return AlbumDataLoader.getImageLoaderWithoutBucketSort(mActivity);
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
