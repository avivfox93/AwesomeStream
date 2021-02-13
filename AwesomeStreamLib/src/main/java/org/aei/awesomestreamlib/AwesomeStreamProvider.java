package org.aei.awesomestreamlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.view.Surface;

import androidx.annotation.NonNull;

public abstract class AwesomeStreamProvider extends MediaCodec.Callback {
    protected Context context;
    protected int width,height;

    AwesomeStreamProvider(@NonNull Context context){
        this.context = context;
    }
    abstract public AwesomeStream getStream();
    abstract public void setResolution(Resolution resolution);
    abstract public void startStream();
    abstract public void stopStream();
    abstract public void release();
    abstract public void requestPermission(Activity activity, int code);
    abstract public boolean onPermissionResult(int resultCode, Intent data);
    abstract public Surface getSurface();
}
