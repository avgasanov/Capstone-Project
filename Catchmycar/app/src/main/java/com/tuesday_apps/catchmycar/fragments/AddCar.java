package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import com.bumptech.glide.Glide;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.common.StringUtils;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.car.Car;
import com.tuesday_apps.catchmycar.user.User;
import com.tuesday_apps.catchmycar.utils.BitmapUtils;
import com.tuesday_apps.catchmycar.utils.Utils;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.*;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * create an instance of this fragment.
 */
public class AddCar extends Fragment implements View.OnClickListener {

    public static final String KEY_PHOTO_URI = "photo-uri-key";
    public static final String KEY_CARNAME = "carname-key";
    public static final String KEY_PLATENUM = "platenum-key";
    public static final String KEY_USERID = "userid-key";
    public static final String KEY_USERNAME = "username-key";

    private static final String ARG_USER_ID = "user-id";
    private static final String ARG_USERNAME = "username";
    private static final int ACTION_IMAGE_PICKER = 400;

    private String mUserId;
    private String mUsername;
    private String mImageUri;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFirebaseStorageRef;
    private static String DATABASE_REFERENCE;


    private FragmentNavigation mFragmentNavigation;

    @BindView(R.id.add_car_iv)
    ImageView mAddCarImageView;
    @BindView(R.id.add_car_intent_iv)
    ImageView mAddCarIntentImageView;
    @BindView(R.id.carname_tv)
    EditText mCarNameEditText;
    @BindView(R.id.platenum_tv)
    EditText mPlateNumEditText;
    @BindView(R.id.add_car_btn)
    Button mAddCarButton;


    public AddCar() {
        // Required empty public constructor
    }

    public static AddCar newInstance(String userId, String username) {
        AddCar fragment = new AddCar();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_USERNAME, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserId = getArguments().getString(ARG_USER_ID);
            mUsername = getArguments().getString(ARG_USERNAME);
        }

        DATABASE_REFERENCE = getString(R.string.database_ref);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child(DATABASE_REFERENCE);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseStorageRef = mFirebaseStorage.getReference().child(STORAGE_CARPROFILE_AVATAR);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_car, container, false);
        ButterKnife.bind(this, view);
        mAddCarButton.setOnClickListener(this);
        mAddCarIntentImageView.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mUsername = savedInstanceState.getString(KEY_USERNAME);
            mUserId = savedInstanceState.getString(KEY_USERID);
            mImageUri = savedInstanceState.getString(KEY_PHOTO_URI);

            mCarNameEditText.setText(savedInstanceState.getString(KEY_CARNAME));
            mPlateNumEditText.setText(savedInstanceState.getString(KEY_PLATENUM));

        }

        if (mImageUri != null) {
            Uri imageUri = Utils.createUriFromString(mImageUri);
            Glide.with(this).load(imageUri).into(mAddCarImageView);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_USERNAME, mUsername);
        outState.putString(KEY_USERID, mUserId);
        outState.putString(KEY_CARNAME, String.valueOf(mCarNameEditText.getText()));
        outState.putString(KEY_PLATENUM, String.valueOf(mPlateNumEditText.getText()));
        outState.putString(KEY_PHOTO_URI, mImageUri);
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
    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.add_car_intent_iv:
                startIntent();
                break;
            case R.id.add_car_btn:
                processAddCar();
                break;
                default:
                    break;
        }
    }

    private void processAddCar() {
        Uri imageUri = Utils.createUriFromString(mImageUri);
        if (imageUri != null) {
            StorageReference imageRef = mFirebaseStorageRef.child(imageUri.getLastPathSegment());
            imageRef
                    .putFile(imageUri)
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Crashlytics.log(getString(R.string.car_add_error));
                        return null;
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        mDatabaseReference
                                .child(USERS_REFERENCE)
                                .child(mUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User currentUser = dataSnapshot.getValue(User.class);
                                if (currentUser == null) {
                                    Crashlytics.log(getString(R.string.auth_err));
                                    return;
                                }
                                boolean isSearchable = currentUser.isSearchable();
                                String plateNum = String.valueOf(mPlateNumEditText.getText());
                                String carname = String.valueOf(mCarNameEditText.getText());
                                Uri imageUri = task.getResult();
                                String imageUrl = imageUri.toString();
                                Car car = new Car(imageUrl,
                                        carname,
                                        plateNum,
                                        isSearchable,
                                        mUserId,
                                        null);
                                String carKey = mDatabaseReference
                                            .child(USERS_REFERENCE)
                                            .child(mUserId)
                                            .child(USER_CARS_REFERENCE).push().getKey();
                                if (carKey == null) {
                                    Crashlytics.log(getString(R.string.car_not_found));
                                    return;
                                }
                                mDatabaseReference
                                        .child(USERS_REFERENCE)
                                        .child(mUserId)
                                        .child(USER_CARS_REFERENCE).child(carKey).setValue(true);

                                mDatabaseReference
                                        .child(SEARCH_INDEX_REFERENCE)
                                        .child(SEARCH_PLATENUM_REFERENCE)
                                        .child(plateNum)
                                        .child(carKey)
                                        .setValue(true);

                                mDatabaseReference
                                        .child(CARS_REFERENCE)
                                        .child(carKey)
                                        .setValue(car);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Crashlytics.log(databaseError.getMessage());
                            }
                        });
                    }
                }
            });
        }
        mFragmentNavigation.popFragment();
    }

    private void startIntent() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, ACTION_IMAGE_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_IMAGE_PICKER && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                mImageUri = imageUri.toString();
            }
            Glide.with(this).load(imageUri).into(mAddCarImageView);
        }
    }
}
