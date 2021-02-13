package org.aei.awesomestream;

import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.aei.awesomestreamlib.DisplayStreamProvider;
import org.aei.awesomestreamlib.Resolution;

import java.io.IOException;

//10.0.0.8
public class MainActivity extends AppCompatActivity{
    private static final int AWESOME_PERMISSION_CODE = 123;
    private DisplayStreamProvider provider;
    private MediaCodec mediaCodec;

    private static final int FRAME_RATE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mMediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,360,640);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 220000);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_HEIGHT,800);
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_WIDTH,600);
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,100000);
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mMediaFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000);
        mMediaFormat.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMediaFormat.setInteger(MediaFormat.KEY_LATENCY, 0);
        }
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        // Set the encoder priority to realtime.
        mMediaFormat.setInteger(MediaFormat.KEY_PRIORITY, 0x00);

        findViewById(R.id.start_button).setOnClickListener(v->{
            mediaCodec.reset();
            mediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            provider = new DisplayStreamProvider(this,mediaCodec);
            provider.setResolution(new Resolution(640,360));
            provider.prepare();
            provider.requestPermission(this,AWESOME_PERMISSION_CODE);

            provider.getStream().addOnBufferCallback((buffer) -> {
            /*
            Do Whatever you like with raw video buffer
             */
                Log.d("Buffer", "got buffer " +
                        buffer.array().length);
            });

            provider.setOnSPSandPPSCallback(((sps, pps) -> {
            /*
            You can use SPS and PPS for streaming purposes
             */
            }));

        });

        findViewById(R.id.stop_button).setOnClickListener(v->{
            provider.stopStream();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AWESOME_PERMISSION_CODE) {
            provider.onPermissionResult(resultCode, data);
            provider.startStream();
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(provider != null){
            provider.stopStream();
            provider.release();
        }
    }
}