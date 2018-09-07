package com.tuesday_apps.catchmycar.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ncapdevi.fragnav.FragNavController;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.car.Car;
import com.tuesday_apps.catchmycar.user.User;
import com.tuesday_apps.catchmycar.utils.BitmapUtils;
import com.tuesday_apps.catchmycar.utils.Utils;

import static android.app.Activity.RESULT_OK;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileFragment extends Fragment implements View.OnClickListener,
        UserProfileFragmentListener{


    private static final String ARG_USER_ID = "user-id";
    private static final int ACTION_IMAGE_PICKER = 100;

    private static final String INSTANCE_ISCURRENTUSER = "instance-is-current-user";
    private static final String INSTANCE_USERID = "instance-user-id";
    private static final String INSTANCE_PROFILE_IMAGE = "instance-profile-image";
    private static final String INSTANCE_USER_ITEM = "instance-user-item";

    private User mUser;
    private UserProfileFragmentListener mListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mUsersReference;
    private String DATABASE_REFERENCE;
    private String CARS_REFERENCE;

    private UserProfileRecyclerViewAdapter mCarsAdapter;

    private boolean isCurrentUserProfile;
    private String mUserId;
    private String mImageUri;

    private FragmentNavigation mFragmentNavigation;

    @BindView(R.id.profile_avatar_iv)
    CircleImageView mProfileAvatarView;
    @BindView(R.id.profile_username_tv)
    TextView mProfileUsername;
    @BindView(R.id.car_list_rv)
    RecyclerView mCarsRecyclerView;
    @BindView(R.id.add_new_car_fab)
    FloatingActionButton mFabButton;
    private ValueEventListener mFriendshipsValueEventListener;
    private ChildEventListener mCarsChildEventListener;

    private ProgressDialog mProgressDialog;
    private ValueEventListener mSingleValueEventListener;


    public UserProfileFragment() {
    }

    public static UserProfileFragment newInstance(String userId) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DATABASE_REFERENCE = getString(R.string.database_ref);
        CARS_REFERENCE = getString(R.string.cars_ref);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child(DATABASE_REFERENCE);
        mUsersReference = mDatabaseReference.child(FirebaseHelper.USERS_REFERENCE);

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_USERID)) {
            mUserId = savedInstanceState.getString(INSTANCE_USERID);
            mImageUri = savedInstanceState.getString(INSTANCE_PROFILE_IMAGE);
            isCurrentUserProfile = savedInstanceState.getBoolean(INSTANCE_ISCURRENTUSER);
            mUser = savedInstanceState.getParcelable(INSTANCE_USER_ITEM);
        }
        else if (getArguments() != null) {
            mUserId = getArguments().getString(ARG_USER_ID);
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                isCurrentUserProfile = mUserId
                        .equals(firebaseUser
                                .getUid());
            }
        }

        mCarsAdapter = new UserProfileRecyclerViewAdapter(this, getContext());
        if (savedInstanceState != null) {
            mCarsAdapter.restoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSTANCE_ISCURRENTUSER, isCurrentUserProfile);
        outState.putString(INSTANCE_PROFILE_IMAGE, mImageUri);
        outState.putString(INSTANCE_USERID, mUserId);
        outState.putParcelable(INSTANCE_USER_ITEM, mUser);
        mCarsAdapter.saveInstanceState(outState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userprofile_list, container, false);
        ButterKnife.bind(this, view);

        Context context = view.getContext();

        mCarsRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        mCarsRecyclerView.setAdapter(mCarsAdapter);


        if(!isCurrentUserProfile) {
            mFabButton.setVisibility(View.GONE);
        }

        if (mUser == null) {
            loadUserFromDatabase();
        } else {
            initializeViews();
        }

        return view;
    }

    private void loadUserFromDatabase() {
        createSingleValueEventListener();
        mUsersReference
                .child(mUserId)
                .addListenerForSingleValueEvent(mSingleValueEventListener);
    }

    private void createSingleValueEventListener() {
        if (mSingleValueEventListener != null) return;

        mSingleValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mUser = dataSnapshot.getValue(User.class);
                    if (mUser != null && mUser.getUsername() != null) {
                        initializeViews();
                    } else {
                        mFragmentNavigation.loadCompletedError(getString(R.string.username_error));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Crashlytics.log(databaseError.getMessage());
                mFragmentNavigation
                        .loadCompletedError(getString(R.string.internet_conn_err),
                                UserProfileFragment.this,
                                getString(R.string.refresh_fragment));
            }
        };
    }

    private void initializeViews() {
        if (mUser == null) {
            Crashlytics.log(getString(R.string.auth_err));
            return;
        }

        Glide
                .with(UserProfileFragment.this)
                .load(mUser.getProfilePicture())
                .into(mProfileAvatarView);

        mProfileUsername.setText(mUser.getUsername());
        mFabButton.setOnClickListener(UserProfileFragment.this);
        mProfileAvatarView.setOnClickListener(UserProfileFragment.this);

        mFragmentNavigation.loadCompleted();
    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setTitle(R.string.avatar_loading_title);
        mProgressDialog.setMessage(getString(R.string.avatar_loading_message));
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_IMAGE_PICKER && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri == null) {
                //LOG userprofile error
                return;
            }
            mImageUri = imageUri.toString();
            Glide.with(this).load(imageUri).into(mProfileAvatarView);
            loadImageToDatabase();
        }
    }

    private void loadImageToDatabase() {
        showProgressDialog();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference userAvatarRef =
                firebaseStorage
                        .getReference(FirebaseHelper.STORAGE_USER_AVATAR)
                        .child(mUserId);
        Uri imageUri = Utils.createUriFromString(mImageUri);
        imageUri = BitmapUtils.compressBitmap(imageUri, getContext());
        if (imageUri == null) return;
        userAvatarRef
                .putFile(imageUri)
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    Crashlytics.log(getString(R.string.user_avatar_load_error));
                    mProgressDialog.dismiss();
                    return null;
                }

                return firebaseStorage
                        .getReference(FirebaseHelper.STORAGE_USER_AVATAR)
                        .child(mUserId)
                        .getDownloadUrl();

            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri imageUri = task.getResult();
                if (imageUri == null) {
                    Crashlytics.log(getString(R.string.user_avatar_load_error));
                    mProgressDialog.dismiss();
                } else {
                    mDatabaseReference
                            .child(FirebaseHelper.USERS_REFERENCE)
                            .child(mUserId)
                            .child(FirebaseHelper.PROFILEPICTURE_USER_REFERENCE)
                            .setValue(imageUri.toString());
                    mProgressDialog.dismiss();
                }
         }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentNavigation) {
            mFragmentNavigation = (FragmentNavigation) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void attachCarChildEventListener() {
        if (mCarsChildEventListener == null) {
            mCarsChildEventListener =  new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.exists()) {
                        String carKey = dataSnapshot.getKey();
                        if (carKey == null) {
                            Crashlytics.log(getString(R.string.car_not_found));
                            return;
                        }
                        mDatabaseReference
                                .child(CARS_REFERENCE)
                                .child(carKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Car currentCar = dataSnapshot.getValue(Car.class);
                                    mCarsAdapter.addCar(carKey, currentCar);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Crashlytics.log(databaseError.getMessage());
                            }
                        });
                    }

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    String carKey = dataSnapshot.getKey();
                    mCarsAdapter.removeCar(carKey);
                }


                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mDatabaseReference
                    .child(USERS_REFERENCE)
                    .child(mUserId)
                    .child(USER_CARS_REFERENCE)
                    .addChildEventListener(mCarsChildEventListener);
        }
    }

    private void detachCarsChildEventListener() {
        mDatabaseReference
                .child(USERS_REFERENCE)
                .child(USER_CARS_REFERENCE)
                .removeEventListener(mCarsChildEventListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        detachCarsChildEventListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        attachCarChildEventListener();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_new_car_fab) {
            fabClick(v);
        } else if (v.getId() == R.id.profile_avatar_iv) {
            profileAvatarClick(v);
        } else if (v.getId() == android.R.id.content) {
            loadUserFromDatabase();
        }
    }

    public void fabClick(View view) {
        if (!mUserId.equals(FirebaseAuth.getInstance().getUid())) {
            return;
        }
        Fragment addCarFragment = AddCar.newInstance(mUserId, mUser.getUsername());
        mFragmentNavigation.pushFragment(addCarFragment);
    }
    private void profileAvatarClick(View v) {
        if (!mUserId.equals(FirebaseAuth.getInstance().getUid())) {
            return;
        }
        startIntent();
    }

    private void startIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, ACTION_IMAGE_PICKER);
    }

    @Override
    public void onUserProfileFragmentListClick(String carId) {
        CarPhotoItemsFragment carPhotoItemsFragment = CarPhotoItemsFragment.newInstance(carId);
        mFragmentNavigation.pushFragment(carPhotoItemsFragment);
    }
}

interface UserProfileFragmentListener {
    void onUserProfileFragmentListClick(String carId);
}
