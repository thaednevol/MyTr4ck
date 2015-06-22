package com.elaborandofuturo.animalfitness;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;



public class Notificacion extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        
            NotificationCompat.Builder mBuilder = 
                    new NotificationCompat.Builder(context)
                    .setContentTitle(context.getResources().getString(R.string.app_name));
            Intent resultIntent = new Intent(context, LeerTag.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(LeerTag.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
    }
    
}  
