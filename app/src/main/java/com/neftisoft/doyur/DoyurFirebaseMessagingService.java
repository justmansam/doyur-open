package com.neftisoft.doyur;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class DoyurFirebaseMessagingService extends FirebaseMessagingService {

    public static final int VISIBILITY_PUBLIC = 999;
    private static final String CHANNEL_ID = "987321";
    String msgTxt;
    String msgTtl;

    public DoyurFirebaseMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        //Log.e("TAG", "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        /*
        if (remoteMessage.getNotification() != null) {
            //Log.e("TAG", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            //mesaj = remoteMessage.getNotification().getBody();
            //Log.e("TAG", "Mesaj: " + mesaj);
        }
         */

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Log.e("TAG", "Message data payload: " + remoteMessage.getData());

            msgTxt = remoteMessage.getData().get("body");
            //Log.e("TAG", "Mesaj: " + msgTxt);

            msgTtl = remoteMessage.getData().get("title");
            //Log.e("TAG", "Başlık: " + msgTtl);

            createNotificationChannel();
            createNotification();

            /*
            if (true) { //Check if data needs to be processed by long running job
                // For long-running tasks (10 seconds or more) use WorkManager.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                //handleNow();
            }
            */
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /*
    NOTIFICATION ACTIONS !!!
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "doyur";
            String description = "Notification Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }
    private void createNotification() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, LobbyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.doyur_notification_icon)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setContentTitle(msgTtl)
                .setContentText(msgTxt)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //builder.setSound(Uri.parse("uri://sadfasdfasdf.mp3"));

        //Creation of the Notification!
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        //Vibration when notified!
        /*
        Vibrator vb = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vb.vibrate(500);
        */

        //notificationId is a unique int for each notification that you must define
        notificationManager.notify(123789, builder.build());
    }
}
