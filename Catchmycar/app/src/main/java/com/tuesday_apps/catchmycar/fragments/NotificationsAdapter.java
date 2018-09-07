package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.Glide;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.user.CCNotification;

public class NotificationsAdapter extends BasePagingRecyclerViewAdapter<CCNotification>{

    public NotificationsAdapter(BasePagingInterface listener, Context context) {
        super(listener, context);
    }

    @Override
    protected String getTitle(int position) {
        CCNotification currentNotif = getItems().get(position);
        String catcher = currentNotif.getCatcherName();
        return catcher + " " + getContext().getString(R.string.catched_you);
    }

    @Override
    protected String getImageUrl(int position) {
        CCNotification currentNotif = getItems().get(position);
        return currentNotif.getPhotoUrl();
    }

    @Override
    protected String getDetailItemId(int position) {
        return getItems().get(position).getCarId();
    }
}
