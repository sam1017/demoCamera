package com.example.myfacebeautycamera.camera;

import android.graphics.SurfaceTexture;

public interface OnFrameAvailableListener {
    void onFrameAvailable(SurfaceTexture surfaceTexture);
}
