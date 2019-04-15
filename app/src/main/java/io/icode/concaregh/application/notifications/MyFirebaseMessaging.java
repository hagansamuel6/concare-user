package io.icode.concaregh.application.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.icode.concaregh.application.R;
import io.icode.concaregh.application.activities.HomeActivity;
import io.icode.concaregh.application.chatApp.ChatActivity;
import io.icode.concaregh.application.chatApp.MessageActivity;

@SuppressWarnings("ALL")
public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sent = remoteMessage.getData().get("sent");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //firebaseUser != null && sent.equals(firebaseUser.getUid())

        if(FirebaseAuth.getInstance().getCurrentUser() != null){

            //checks build version is Oreo(8.0 and above)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                sendOreoNotification(remoteMessage);
            }
            else{
                // method call to send notification to devices below android Oreo(8.0)
                sendNotification(remoteMessage);
            }

        }

        /*** Code for push notification from firebase console */
        /*Log.d("Service", "From: " + remoteMessage.getFrom());

        //if(sent.equals(firebaseUser.getUid()))

        if (remoteMessage.getData().size() > 0) {
            Log.d("Service", "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.d("Service", "Message notification: " + remoteMessage.getNotification().getBody());
        }
        */


    }

    // sending notification to devices with versions below Oreo(android 8.0)
    private void sendNotification(RemoteMessage remoteMessage) {

        String user  = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        String msg = remoteMessage.getNotification().getBody();
        String nTitle = remoteMessage.getNotification().getTitle();

        int i = 0;

        RemoteMessage.Notification notification = remoteMessage.getNotification();
       // int j = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("uid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.app_logo_round)
                .setContentTitle(nTitle)
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setVibrate(new long[]{1000,1000,1000})
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(i,builder.build());

    }

    // method to send notification to Oreo devices and above
    private void sendOreoNotification(RemoteMessage remoteMessage) {

        String user  = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        String msg = remoteMessage.getNotification().getBody();
        String nTitle = remoteMessage.getNotification().getTitle();

        int i = 0;

        RemoteMessage.Notification notification = remoteMessage.getNotification();

        //int j = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this,MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("uid",user);
        intent.putExtras(bundle);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(nTitle,msg,
                pendingIntent,defaultSound,icon);

        // building notification using notification manager
        oreoNotification.getManager().notify(i,builder.build());

    }
}
