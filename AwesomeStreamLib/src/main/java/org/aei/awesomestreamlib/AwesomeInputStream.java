package org.aei.awesomestreamlib;

import android.util.Log;

import org.aei.awesomestreamlib.AwesomeStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

public class AwesomeInputStream extends InputStream implements AwesomeStream.AwesomeStreamCallback {

    private Semaphore semaphore;
    private AwesomeStream stream;
    volatile private int index = 0;
    volatile private ByteBuffer byteBuffer;

    public AwesomeInputStream(AwesomeStream stream){
        this.stream = stream;
        this.semaphore = new Semaphore(1);
        stream.addOnBufferCallback(this);
    }

    @Override
    public int read() throws IOException {
        synchronized (this) {
//            Log.d("Awesome InputStream", "Waiting...");
            while (byteBuffer == null) ;
            while (index >= byteBuffer.array().length);
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
//            Log.d("Awesome InputStream", "read()");
//            throw new IOException(String.format("Buffer size is: %d, index is: %d",length,index));
            int res = this.byteBuffer.array()[index++];
            semaphore.release();
            return res;
        }
    }

    @Override
    public int available() throws IOException {
        return super.available();
    }

    @Override
    public void onBuffer(ByteBuffer buffer) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        this.byteBuffer = ByteBuffer.allocate(buffer.limit());
        this.byteBuffer.put(buffer);
        this.index = 0;
        semaphore.release();
    }
}
