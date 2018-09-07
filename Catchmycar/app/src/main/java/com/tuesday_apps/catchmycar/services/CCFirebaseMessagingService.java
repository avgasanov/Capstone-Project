package com.tuesday_apps.catchmycar.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.MainActivity;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.widget.CatchWidget;
import com.tuesday_apps.catchmycar.widget.WidgetIntentService;

import java.util.Map;

public class CCFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getUid();
        if (userId == null) return;
        String databaseRef = getString(R.string.database_ref);
        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference(databaseRef);
        databaseReference
                .child(FirebaseHelper.TOKENS_REFERENCE)
                .child(userId).setValue(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        if (data.size() > 0) {
            sendNotification(data);
        }


    }

    private void sendNotification(Map<String, String> data) {

        String carId = data.get(FirebaseHelper.NOTIFICATION_KEY_CARID);
        String body = data.get(FirebaseHelper.NOTIFICATION_BODY);
        String title = data.get(FirebaseHelper.NOTIFICATION_TITLE);

        if (TextUtils.isEmpty(carId)) return;

        Intent intent = MainActivity.generateCarPhotoIntent(this, carId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT);


        if (body.length() > FirebaseHelper.NOTIFICATION_MAX_CHARACTERS) {
            body = body.substring(0, FirebaseHelper.NOTIFICATION_MAX_CHARACTERS) + "\u2026";
        }

        if (title.length() > FirebaseHelper.NOTIFICATION_TITLE_MAX_CHARACTERS) {
            title = title.substring(0, FirebaseHelper.NOTIFICATION_TITLE_MAX_CHARACTERS) + "\u2026";
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Crashlytics.log(getString(R.string.notification_man_error));
            return;
        }
        notificationManager.notify(0, notificationBuilder.build());
        CatchWidget.update(getApplication(), getApplicationContext());
    }
}
