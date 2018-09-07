package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.car.Car;
import com.tuesday_apps.catchmycar.utils.Utils;

import java.util.ArrayList;


import butterknife.BindView;
import butterknife.ButterKnife;

public class UserProfileRecyclerViewAdapter extends RecyclerView.Adapter<UserProfileRecyclerViewAdapter.ViewHolder> {

    private static final String INSTANCE_CAR_ITEMS = "instance-car-items";
    private static final String INSTANCE_CAR_IDS = "instance-car-ids";

    private ArrayList<Car> mCars;
    private ArrayList<String> mCarIds;
    private final UserProfileFragmentListener mListener;
    private final Context mContext;

    public UserProfileRecyclerViewAdapter(UserProfileFragmentListener listener, Context context) {
        mContext = context;
        mListener = listener;
        initializeLocalVariables();
    }

    public Bundle saveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(INSTANCE_CAR_ITEMS, mCars);
        outState.putStringArrayList(INSTANCE_CAR_IDS, mCarIds);
        return outState;
    }

    public void restoreInstanceState(Bundle inState) {
        mCars = inState.getParcelableArrayList(INSTANCE_CAR_ITEMS);
        mCarIds = inState.getStringArrayList(INSTANCE_CAR_IDS);
        if (mCars == null || mCarIds == null) {
            initializeLocalVariables();
        }
    }

    private void initializeLocalVariables() {
        mCars = new ArrayList<>();
        mCarIds = new ArrayList<>();
    }

    public void addCar(String carId, Car car) {
        if (!existInList(carId)) {
            mCarIds.add(carId);
            mCars.add(car);
            this.notifyDataSetChanged();
        }
    }

    private boolean existInList(String carId) {
        int carIdx = mCarIds.indexOf(carId);
        return carIdx != -1;
    }

    public void removeCar(String carId) {
        int carIdx = mCarIds.indexOf(carId);
        mCarIds.remove(carIdx);
        mCars.remove(carIdx);
        this.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_userprofile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mCar = mCars.get(position);
        holder.mCarnameTextView.setText(mCars.get(position).getCarName());
        Uri avatarUri = Utils.createUriFromString(holder.mCar.getCarAvatar());

        Glide.with(mContext).load(avatarUri).into(holder.mCarAvatarImageView);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onUserProfileFragmentListClick(mCarIds.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCars.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @BindView(R.id.car_name_tv)
        TextView mCarnameTextView;
        @BindView(R.id.car_avatar_iv)
        ImageView mCarAvatarImageView;

        public Car mCar;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }
    }
}
