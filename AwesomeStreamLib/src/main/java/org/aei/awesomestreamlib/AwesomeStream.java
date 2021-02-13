package org.aei.awesomestreamlib;

import android.media.Image;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class AwesomeStream{
    private boolean started = false;
    private final ArrayList<AwesomeStreamCallback> callbacks = new ArrayList<>();
    private OutputStream outputStream;

    public void addOnBufferCallback(AwesomeStreamCallback callback){this.callbacks.add(callback);}

    public void pushBuffer(ByteBuffer buffer) throws IOException {
        if(started) {
            if(outputStream != null) {
                outputStream.write(buffer.array());
                outputStream.flush();
            }
            notifySubscribers(buffer);
        }
    }

    private void notifySubscribers(ByteBuffer buffer) {
        callbacks.forEach(callback->callback.onBuffer(buffer));
    }

    public void start(){this.started = true;}
    public void stop(){this.started = false;}

    public void setOutputStream(OutputStream outputStream){
        this.outputStream = outputStream;
    }
    public AwesomeInputStream getInputStream(){ return new AwesomeInputStream(this); }
    public interface AwesomeStreamCallback{
        void onBuffer(ByteBuffer buffer);
    }
}
