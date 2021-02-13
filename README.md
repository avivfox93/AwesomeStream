
# AwesomeStream

## Overview:
AwesomeStream let you get a pure bytes stream of encoded data from your phone screen.
The stream can be used to stream* your screen, record your screen or whatever you can think of.

##### *The library does not yet contain a Packetizer needed for wrapping the data for streaming capabilities, but you can build your own or find one and implement it in your app. We hope to build one and implement it in our library as soon as possible.
## How to Use:
Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Then add the dependency
```gradle
dependencies {
        implementation 'com.github.avivfox93:AwesomeStream:1.0.0'
}
```

## Example:
### Getting a byte[] stream from screen:
```java
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
 Do Whatever you like with raw video buffer */  Log.d("Buffer", "got buffer " +
                    buffer.array().length);
  });

  provider.setOnSPSandPPSCallback(((sps, pps) -> {
        /*
 You can use SPS and PPS for streaming purposes */  }));

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
```
### Creating a InputStream from AwesomeStream:
![](examples/RetroGameDialog.gif)
```java
DisplayStreamProvider provider;
...
**initialize provider**
...
InputStream inputStream = provider.getStream().getInputStream();
```

## TODO:
- [ ] Add a H.264 Packetizer
- [ ] Add Camera Stream
- [ ] Add RTP Server to stream the video through RTP Protocol
- [x] Create a custom InputStream

Feel free to add suggestions/issues through the issues tab.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
