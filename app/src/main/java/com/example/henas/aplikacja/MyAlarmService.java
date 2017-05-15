package com.example.henas.aplikacja;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;


/**
 * Created by Henas on 15.05.2017.
 */

public class MyAlarmService extends Service {

    private NotificationManager mManager;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() { super.onCreate(); System.out.println(NewTaskActivity.getDescription());}

    @Override
    public void onDestroy() { super.onDestroy(); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        super.onStartCommand(intent, flags, startId);
        mManager = (NotificationManager) this.getApplicationContext().getSystemService(this.getApplicationContext().NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(this.getApplicationContext(), NotificationView.class);
        Notification notification = new Notification.Builder(this.getApplicationContext())
                .setContentTitle("Menadżer zadań")
                .setContentText(NewTaskActivity.getDescription())
                .setSmallIcon(R.mipmap.icon)
                .build();
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        mManager.notify(0, notification);

        return startId;


    }


}