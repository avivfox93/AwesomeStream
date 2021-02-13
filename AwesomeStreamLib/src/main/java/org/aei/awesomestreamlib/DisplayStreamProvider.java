package org.aei.awesomestreamlib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.app.Activity.RESULT_OK;

public class DisplayStreamProvider extends AwesomeStreamProvider {
    private final MediaProjectionManager projectionManager;
    private Surface surface;
    private final int screenDensity;
    private VirtualDisplay virtualDisplay;
    private MediaProjection mediaProjection;
    private final MediaCodec mediaCodec;
    private final AwesomeStream stream;
    private ByteBuffer sps;
    private ByteBuffer pps;
    private OnSPSandPPS onSPSandPPS;

    public DisplayStreamProvider(@NonNull Context context, @NonNull MediaCodec mediaCodec) {
        super(context);
        this.mediaCodec = mediaCodec;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        this.stream = new AwesomeStream();
        screenDensity = metrics.densityDpi;
        projectionManager = (MediaProjectionManager)context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public void prepare(){
        context.startService(new Intent(context,AwesomeDisplayService.class));
    }

    @Override
    public AwesomeStream getStream() {
        return stream;
    }

    @Override
    public void setResolution(Resolution resolution) {
        this.width = resolution.getWidth();
        this.height = resolution.getHeight();
    }

    @Override
    public void startStream() {
        stream.start();
        try {
            surface = mediaCodec.createInputSurface();
        }catch (Exception e){
            e.printStackTrace();
        }
        mediaCodec.setCallback(this);
        mediaCodec.start();
        virtualDisplay = createVirtualDisplay();
    }

    @Override
    public void stopStream() {
        sps = null;
        pps = null;
        stream.stop();
        mediaProjection.stop();
        mediaCodec.stop();
    }

    @Override
    public void release() {
        context.stopService(new Intent(context,AwesomeDisplayService.class));
        mediaCodec.release();
        virtualDisplay.release();
    }

    private VirtualDisplay createVirtualDisplay() {
        return mediaProjection.createVirtualDisplay("AwesomeScreen",
                width, height, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null /*Callbacks*/, null /*Handler*/);
    }

    @Override
    public void requestPermission(Activity activity, int code) {
        activity.startActivityForResult(projectionManager.createScreenCaptureIntent(),
                code);
    }

    public boolean onPermissionResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return false;
        }
        mediaProjection = projectionManager.getMediaProjection(resultCode, data);
        mediaProjection.registerCallback(new MediaProjectionCallback(), null);
        return true;
    }

    @Override
    public Surface getSurface() {
        return surface;
    }

    @Override
    public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {
    }

    @Override
    public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int i, @NonNull MediaCodec.BufferInfo bufferInfo) {
        try {
            if(sps == null || pps == null){
                mediaCodec.releaseOutputBuffer(i,false);
                return;
            }
            ByteBuffer buffer = mediaCodec.getOutputBuffer(i);
            ByteBuffer b = ByteBuffer.allocate(buffer.limit());
            b.put(buffer);
            stream.pushBuffer(b);
            mediaCodec.releaseOutputBuffer(i,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
        e.printStackTrace();
    }

    @Override
    public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
        Log.d("Codec", "Updated output format! New height:"
                + mediaFormat.getInteger(MediaFormat.KEY_HEIGHT) + " new width: " +
                mediaFormat.getInteger(MediaFormat.KEY_WIDTH));
        this.sps = mediaFormat.getByteBuffer("csd-0");
        this.pps = mediaFormat.getByteBuffer("csd-1");
        if(onSPSandPPS != null)
            onSPSandPPS.onReady(sps,pps);
    }

    public void setOnSPSandPPSCallback(OnSPSandPPS onSPSandPPS) {
        this.onSPSandPPS = onSPSandPPS;
    }

    public ByteBuffer getSps() {
        return sps;
    }

    public ByteBuffer getPps() {
        return pps;
    }

    private class MediaProjectionCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mediaProjection = null;
        }
    }

    public interface OnSPSandPPS{
        void onReady(ByteBuffer sps, ByteBuffer pps);
    }
}
