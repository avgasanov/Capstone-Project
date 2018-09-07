package com.tuesday_apps.catchmycar.widget;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;


public class WidgetIntentService extends IntentService {

    private static final String ACTION_PHOTO_TO_WIDGET = "action-photo-to-widget";

    private static final String PHOTO_URL_EXTRA = "photo-url-extra";


    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    public static void startActionWidgetPhotoUpdate(Context context, String photoUrl) {
        Intent intent = new Intent(context, WidgetIntentService.class);
        intent.setAction(ACTION_PHOTO_TO_WIDGET);
        intent.putExtra(PHOTO_URL_EXTRA, photoUrl);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PHOTO_TO_WIDGET.equals(action)) {
                final String photoUrl = intent.getStringExtra(PHOTO_URL_EXTRA);
                handleActionWidgetPhoto(photoUrl);
            }
        }
    }

    private void handleActionWidgetPhoto(String photoURL) {

    }

}
