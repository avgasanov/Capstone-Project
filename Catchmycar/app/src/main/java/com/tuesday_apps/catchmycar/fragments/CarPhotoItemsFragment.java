package com.tuesday_apps.catchmycar.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.car.CarPhotos;
import com.tuesday_apps.catchmycar.car.UserInPhoto;

public class CarPhotoItemsFragment extends HomeFragment {


    public static CarPhotoItemsFragment newInstance(String carId) {
        Bundle args = generateArgs(carId);
        CarPhotoItemsFragment fragment = new CarPhotoItemsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected void processValueEventListenerChange(DataSnapshot dataSnapshot, boolean isRefreshing) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String carPhotoKey = snapshot.getKey();
            if (carPhotoKey == null) continue;
            mDatabaseReference
                    .child(FirebaseHelper.CAR_PHOTOS_REFERENCE)
                    .child(carPhotoKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        CarPhotos carPhoto = dataSnapshot.getValue(CarPhotos.class);
                        if (carPhoto == null) return;
                        UserInPhoto randomCatchedUser = createRandomCatchedUser(carPhoto);
                        addPhotoToAdapterHelper(isRefreshing, carPhoto, carPhotoKey, randomCatchedUser);
                    }
                    else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected DatabaseReference getPhotosReference() {
        return mDatabaseReference
                .child(FirebaseHelper.CARS_REFERENCE)
                .child(getItemId())
                .child(FirebaseHelper.PHOTOS_IN_CARS_REFERENCE);
    }
}
