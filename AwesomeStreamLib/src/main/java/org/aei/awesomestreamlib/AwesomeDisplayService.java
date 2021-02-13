package org.aei.awesomestreamlib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

public class AwesomeDisplayService extends Service {

    private static final String CHANNEL_ID = "AWESOME_SERVICE_ID";
    private static final int NOTIFICATION = 0;
    private NotificationManager notificationManager;

    public AwesomeDisplayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"service",NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null)
            intent = new Intent();
        intent.setAction("start");
        switch (intent.getAction()){
            case "start":
                final Notification notification = new Notification.Builder(this,CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .setWhen(System.currentTimeMillis())
                        .setContentText("Recording...")
                        .build();
                startForeground(startId,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
                break;
            case "stop":
                stopForeground(true);
                return START_NOT_STICKY;
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}