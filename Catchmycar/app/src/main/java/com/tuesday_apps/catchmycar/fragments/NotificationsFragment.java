package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.user.CCNotification;

public class NotificationsFragment extends BasePagingFragment {
    private String mUserId;

    @Override
    protected void setLocalVariables() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            mUserId = firebaseUser.getUid();
        }
    }

    @Override
    protected DatabaseReference getPagingReference(String databaseName) {
        return FirebaseDatabase
                .getInstance()
                .getReference(databaseName)
                .child(FirebaseHelper.NOTIFICATIONS_REF)
                .child(mUserId);
    }

    @Override
    protected BasePagingRecyclerViewAdapter getBaseAdapter(Context context) {
        return new NotificationsAdapter(this, context);
    }

    @Override
    protected Parcelable getEntityVal(DataSnapshot snapshot) {
        return snapshot.getValue(CCNotification.class);
    }

    @Override
    public void openDetailItem(String itemId) {
        CarPhotoItemsFragment carPhotoItemsFragment =
                CarPhotoItemsFragment.newInstance(itemId);
        pushFragment(carPhotoItemsFragment);
    }
}
