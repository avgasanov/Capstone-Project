package com.tuesday_apps.catchmycar.widget;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.GlideApp;
import com.tuesday_apps.catchmycar.R;



import java.util.Map;


public class CatchWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.catch_widget);

        setPhoto(views, context);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            Log.v("ANACODE", "no user");
            views
                    .setTextViewText(R.id.appwidget_text,
                            context.getString(R.string.no_photos_found));
        } else {
            DatabaseReference databaseReference =
                    FirebaseDatabase
                            .getInstance()
                            .getReference(context.getString(R.string.database_ref));

            databaseReference
                    .child(FirebaseHelper.SEARCH_INDEX_REFERENCE)
                    .child(FirebaseHelper.SEARCH_USER_PHOTOS_REFERENCE)
                    .child(firebaseUser.getUid())
                    .orderByKey()
                    .limitToLast(1)
                    .addListenerForSingleValueEvent(
                            getPhotoUrlGetterEvent(databaseReference,
                                    context,
                                    views,
                                    appWidgetId));
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static ValueEventListener getPhotoUrlGetterEvent(DatabaseReference databaseReference,
                                                             Context context,
                                                             RemoteViews views,
                                                             int appWidgetId) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        Map<String, Long> photo =
                                (Map<String, Long>) dataSnapshot.getValue();
                        String photoKey = (String) photo.keySet().toArray()[0];
                        databaseReference
                                .child(FirebaseHelper.CAR_PHOTOS_REFERENCE)
                                .child(photoKey)
                                .child(FirebaseHelper.PHOTO_URL_IN_CARPHOTOS_REFERENCE)
                                .addListenerForSingleValueEvent(
                                        getPhotoUrlEventListener(views, context, appWidgetId));

                    } catch (Exception e) {
                        setNoPhoto(views, context);
                        Log.v("ANACODE", "error caught");
                    }
                } else {
                    setNoPhoto(views, context);
                    Log.v("ANACODE", "snap not exist getPhotoUrlGetter");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setNoPhoto(views, context);
                Log.v("ANACODE", "cancelled getPhotoUrlGetter");
            }
        };
    }

    private static ValueEventListener getPhotoUrlEventListener(RemoteViews views,
                                                               Context context,
                                                               int appWidgetId) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String photoUrl = (String) dataSnapshot.getValue();
                    if (photoUrl != null) {
                        loadPhoto(photoUrl, context, views, appWidgetId);
                    } else {
                        Log.v("ANACODE", "photo url is empty");
                        setNoPhoto(views, context);
                    }
                }
                else {
                    Log.v("ANACODE", "photo url snap not exists");
                    setNoPhoto(views, context);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.v("ANACODE", "photo url connection error");
                setNoPhoto(views, context);
            }
        };
    }

    private static void loadPhoto(String photoUrl, Context context, RemoteViews views, int appWidgetId) {
        Log.v("ANACODE", "glide loads photo");

        AppWidgetTarget appWidgetTarget =
                new AppWidgetTarget(context, R.id.appwidget_iv, views, appWidgetId) {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
            }
        };

        GlideApp
                .with(context.getApplicationContext())
                .asBitmap()
                .override(480, 480)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(photoUrl)
                .into(appWidgetTarget);


    }

    private static void setNoPhoto(RemoteViews views, Context context) {
        views
                .setTextViewText(R.id.appwidget_text,
                        context
                                .getString(R.string.no_photos_found));
    }

    private static void setPhoto(RemoteViews views, Context context) {
        views
                .setViewVisibility(R.id.appwidget_text, View.GONE);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    public static void update(Application application, Context context) {
        Intent intent = new Intent(context, CatchWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(application)
                .getAppWidgetIds(new ComponentName(application, CatchWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}

