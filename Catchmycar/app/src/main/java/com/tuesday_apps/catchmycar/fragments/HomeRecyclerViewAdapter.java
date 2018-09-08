package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.GlideApp;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.car.CarPhotoAdapterItem;
import com.tuesday_apps.catchmycar.car.CarPhotos;
import com.tuesday_apps.catchmycar.car.UserInPhoto;


import java.util.ArrayList;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {

    private static final String CARPHOTOITEMS_INSTANCE = "car_photo_items";
    private final Context mContext;
    private final homePhotoCardListItemListener mListener;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mLikesReference;
    private ArrayList<CarPhotoAdapterItem> mCarPhotos;
    private HashSet<String> mCarPhotoIds;
    private int mArrayInsertionIndex;
    private StorageReference mStorageReference;

    private final CircularProgressDrawable mCircularProgressDrawable;

    public HomeRecyclerViewAdapter(homePhotoCardListItemListener listener, Context context) {
        mCarPhotos = new ArrayList<>();
        mCarPhotoIds = new HashSet<>();

        mListener = listener;
        mContext = context;
        mDatabase = FirebaseDatabase.getInstance();
        mLikesReference = mDatabase
                .getReference(mContext.getString(R.string.database_ref))
                .child(FirebaseHelper.LIKES_REFERENCE);
        mArrayInsertionIndex = 0;

        mCircularProgressDrawable = new CircularProgressDrawable(mContext);
        mCircularProgressDrawable.setStrokeWidth(5f);
        mCircularProgressDrawable.setCenterRadius(30f);
        mCircularProgressDrawable.start();
        mStorageReference = FirebaseStorage.getInstance().getReference().child(FirebaseHelper.STORAGE_CARPHOTOS);
    }

    public void addPhoto(CarPhotos carPhoto, String carPhotoId, UserInPhoto catched) {
        insertPhoto(carPhoto, carPhotoId, catched, mArrayInsertionIndex);
    }

    public void addPhotoToTop(CarPhotos carPhoto, String carPhotoId, UserInPhoto catched) {
        insertPhoto(carPhoto, carPhotoId, catched, 0);
    }

    private void insertPhoto(CarPhotos carPhoto, String carPhotoId, UserInPhoto catched, int insertionPos) {
        if (carPhoto != null
                && mCarPhotos != null
                && !mCarPhotoIds.contains(carPhotoId)) {
            mCarPhotos
                    .add(insertionPos, new CarPhotoAdapterItem(carPhoto, carPhotoId, catched));
            mCarPhotoIds.add(carPhotoId);
            notifyDataSetChanged();
        }
    }

    public void nextPage() {
        mArrayInsertionIndex = this.getItemCount();
    }

    public String getFirstPhotoId() {
        if (mCarPhotos == null || this.getItemCount() == 0) {
            return null;
        }

        CarPhotoAdapterItem firstItem = mCarPhotos.get(0);

        if (firstItem != null) {
            return firstItem.getCarPhotoId();
        } else {
            return null;
        }
    }

    public String getLastPhotoId() {
        if (mCarPhotos == null || this.getItemCount() == 0) {
            return null;
        }

        int lastPosition = this.getItemCount() - 1;
        CarPhotoAdapterItem lastItem = mCarPhotos.get(lastPosition);

        if(lastItem != null) {
            return lastItem.getCarPhotoId();
        } else {
            return null;
        }
    }

    public void cleanAdapter() {
        mCarPhotos = new ArrayList<>();
    }

    public Bundle saveState(Bundle outState) {
        outState.putParcelableArrayList(CARPHOTOITEMS_INSTANCE, mCarPhotos);
        return outState;
    }

    public void restoreState(Bundle in) {
        if (in == null) return;
        mCarPhotos = in.getParcelableArrayList(CARPHOTOITEMS_INSTANCE);
        if (mCarPhotos == null) {
            mCarPhotos = new ArrayList<CarPhotoAdapterItem>();
        } else {
            for (CarPhotoAdapterItem photoItem : mCarPhotos) {
                mCarPhotoIds.add(photoItem.getCarPhotoId());
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.car_photo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        CarPhotoAdapterItem currentCarItem = mCarPhotos.get(position);
        String carPhotoId = currentCarItem.getCarPhotoId();

        String currentUserId = FirebaseAuth.getInstance().getUid();

        CarPhotos currentCar = currentCarItem.getCarPhoto();
        if (currentCar == null) {
            Crashlytics.log(currentCarItem.getCarPhotoId() + " car photo load error");
            return;
        }
        String carPhoto = currentCar.getPhotoUrl();
        String carPhotoTitle = currentCar.getPhotoTitle();

        String catcherUsername = currentCar.getCatcherName();
        String catcherAvatar = currentCar.getCatcherAvatar();

        holder.mCatcherUsernameTextView.setText(catcherUsername);
        GlideApp
                .with(mContext)
                .load(catcherAvatar)
                .placeholder(R.drawable.avatart)
                .into(holder.mCatcherAvatarImageView);

        UserInPhoto catchedUser = currentCarItem.getCatchedUserInPhoto();

        if (catchedUser != null) {
            String catchedUsername = catchedUser.getUsername();
            String catchedAvatar = catchedUser.getProfilePicture();

            holder.mCatchedUsernameTextView.setText(catchedUsername);
            GlideApp
                    .with(mContext)
                    .load(catchedAvatar)
                    .placeholder(R.drawable.avatart)
                    .into(holder.mCatchedAvatarImageView);
        }

        int likesCount = currentCar.getLikeCount();

        Glide
                .with(mContext)
                .load(currentCar.getPhotoUrl())
                .into(holder.mCarPhotoImageView);


        holder.mLikesCountTextView.setText(String.valueOf(likesCount));
        holder.mCarPhotoTitleTextView.setText(carPhotoTitle);

        initLike(position, carPhotoId, currentUserId, holder.mLikeButton);

        View.OnClickListener clickListener = createListener(position, holder);

        holder.mCatcherAvatarImageView.setOnClickListener(clickListener);
        holder.mCatcherUsernameTextView.setOnClickListener(clickListener);
        holder.mCatchedUsernameTextView.setOnClickListener(clickListener);
        holder.mCatchedAvatarImageView.setOnClickListener(clickListener);
        holder.mLikeButton.setOnClickListener(clickListener);
        holder.mCarPhotoCommentsTextView.setOnClickListener(clickListener);
        holder.mCommentsButton.setOnClickListener(clickListener);

        if(position == getItemCount() - 1) {
            mListener.loadNext();
        }
    }

    private void initLike(int position, String carPhotoId, String currentUserId, ToggleButton mLikeButton) {
        CarPhotoAdapterItem currentPhotoItem = mCarPhotos.get(position);
        if (currentUserId == null || carPhotoId == null) return;
        if (currentPhotoItem.isLikeInitialized()) {
            mLikeButton.setChecked(currentPhotoItem.isLike());
            return;
        }

        mLikesReference
                .child(carPhotoId)
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isPhotoLiked = dataSnapshot.exists();
                mLikeButton.setChecked(isPhotoLiked);
                currentPhotoItem.setLikeInitialized(true);
                currentPhotoItem.setLike(isPhotoLiked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                    Crashlytics.log(databaseError.getMessage());
            }
        });
    }

    private View.OnClickListener createListener(int position, ViewHolder holder) {
        CarPhotoAdapterItem currentCarItem = mCarPhotos.get(position);
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int clickedId = view.getId();
                CarPhotos currentCar = currentCarItem.getCarPhoto();
                switch (clickedId) {
                    case R.id.catcher_avatar_iv:
                    case R.id.catcher_username_tv:
                        mListener.onCatcherClick(currentCar.getCatcherId());
                        break;
                    case R.id.catched_avatar_iv:
                        mListener.onCatchedClick(currentCarItem.getCatchedUserInPhoto().getUserId());
                        break;
                    case R.id.catched_username_tv:
                        mListener.onCatchedClick(currentCarItem.getCatchedUserInPhoto().getUserId());
                        break;
                    case R.id.like_iv:
                        mListener
                                .onLikeClick(currentCarItem.getCarPhotoId(),
                                        holder.mLikesCountTextView,
                                        holder.mLikeButton);
                        break;
                    case R.id.car_photo_comments_tv:
                        mListener.onShowCommentsClick(currentCarItem.getCarPhotoId());
                        break;
                    case R.id.comment_iv:
                        mListener.onCommentClick(currentCarItem.getCarPhotoId());
                        break;
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return mCarPhotos.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;

        @BindView(R.id.catcher_avatar_iv)
        CircleImageView mCatcherAvatarImageView;
        @BindView(R.id.catcher_username_tv)
        TextView mCatcherUsernameTextView;
        @BindView(R.id.catched_avatar_iv)
        CircleImageView mCatchedAvatarImageView;
        @BindView(R.id.catched_username_tv)
        TextView mCatchedUsernameTextView;
        @BindView(R.id.car_photo_iv)
        ImageView mCarPhotoImageView;
        @BindView(R.id.like_iv)
        ToggleButton mLikeButton;
        @BindView(R.id.car_photo_likes_tv)
        TextView mLikesCountTextView;
        @BindView(R.id.car_photo_title)
        TextView mCarPhotoTitleTextView;
        @BindView(R.id.car_photo_comments_tv)
        TextView mCarPhotoCommentsTextView;
        @BindView(R.id.comment_iv)
        Button mCommentsButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);

        }
    }
}
