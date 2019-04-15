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
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.icode.concaregh.application.R;
import io.icode.concaregh.application.chatApp.ChatActivity;

// ...
@SuppressWarnings("deprecation")
    public class FirebaseBroadCastMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    int notifyId = 0;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("Service", "From: " + remoteMessage.getFrom());

        String sent = remoteMessage.getData().get("sent");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //if(sent.equals(firebaseUser.getUid()))

        if (remoteMessage.getData().size() > 0) {
                    Log.d("Service", "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
                    Log.d("Service", "Message notification: " + remoteMessage.getNotification().getBody());
        }

        // method call to sendNotification
        sendNotification(remoteMessage);

    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String user  = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("isNotify", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.app_logo_round)
                .setColor(ContextCompat.getColor(this,R.color.orange_light))
                .setContentTitle("CONCARE GH")
                .setWhen(System.currentTimeMillis())
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(uri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(notifyId, builder.build());
                notifyId++;
            }

}
