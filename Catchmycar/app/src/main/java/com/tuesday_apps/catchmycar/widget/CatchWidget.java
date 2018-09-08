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
import com.tuesday_apps.catchmycar.car.CarPhotos;


import java.util.Map;


public class CatchWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.catch_widget);


            DatabaseReference databaseReference =
                    FirebaseDatabase
                            .getInstance()
                            .getReference(context.getString(R.string.database_ref));

            databaseReference
                    .child(FirebaseHelper.CAR_PHOTOS_REFERENCE)
                    .orderByKey()
                    .limitToLast(1)
                    .addListenerForSingleValueEvent(
                            getPhotoUrlGetterEvent(databaseReference,
                                    context,
                                    views,
                                    appWidgetId));


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
                        CarPhotos carPhoto = null;
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            carPhoto = snap.getValue(CarPhotos.class);
                        }
                        if (carPhoto != null && carPhoto.getPhotoUrl() != null) {
                             loadPhoto(carPhoto.getPhotoUrl(), context, views, appWidgetId);
                        }
                        setPhoto(views, context);
                    } catch (Exception e) {
                        setNoPhoto(views, context);
                    }
                } else {
                    setNoPhoto(views, context);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setNoPhoto(views, context);
            }
        };
    }


    private static void loadPhoto(String photoUrl, Context context, RemoteViews views, int appWidgetId) {


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
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {

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

