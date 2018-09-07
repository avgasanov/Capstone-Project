package com.tuesday_apps.catchmycar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.car.Car;
import com.tuesday_apps.catchmycar.car.CarPhotos;
import com.tuesday_apps.catchmycar.user.User;
import com.tuesday_apps.catchmycar.utils.BitmapUtils;
import com.tuesday_apps.catchmycar.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.CARS_REFERENCE;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.CAR_PHOTOS_REFERENCE;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.SEARCH_INDEX_REFERENCE;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.SEARCH_PLATENUM_REFERENCE;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.STORAGE_CARPHOTOS;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.STORAGE_CARPROFILE_AVATAR;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.USERS_REFERENCE;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.USER_CARS_REFERENCE;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PHOTO_FILENAME = "photo-filename";
    private static final String PHOTO_HEIGHT = "photo-height";
    private static final String PHOTO_WIDTH = "photo-width";
    private static final String INSTANCE_PLATENUM = "instance-platenum";
    private static final String INSTANCE_COMMENT = "instance-comment";
    @BindView(R.id.catched_car_post_iv)
    ImageView mCarImageView;
    @BindView(R.id.platenum_post_et)
    EditText mPlatenumEditText;
    @BindView(R.id.post_carched_btn)
    Button mPostCatchedButton;
    @BindView(R.id.comment_post_et)
    EditText mCommentEditText;

    private String mImageFilename;
    private int mImageHeight;
    private int mImageWidth;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFirebaseStorageRef;

    public static Intent createIntent(Context packageContext,
                                      String imageFilename,
                                      int imageHeiht,
                                      int imageWidth) {
        Intent intent = new Intent(packageContext, PostActivity.class);
        intent.putExtra(PHOTO_FILENAME, imageFilename);
        intent.putExtra(PHOTO_HEIGHT, imageHeiht);
        intent.putExtra(PHOTO_WIDTH, imageWidth);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            mImageFilename = getIntent().getStringExtra(PHOTO_FILENAME);
            mImageHeight = getIntent().getIntExtra(PHOTO_HEIGHT, 400);
            mImageWidth = getIntent().getIntExtra(PHOTO_HEIGHT, 400);
        } else {
            mImageFilename = savedInstanceState.getString(PHOTO_FILENAME);
            mImageHeight = savedInstanceState.getInt(PHOTO_HEIGHT);
            mImageWidth = savedInstanceState.getInt(PHOTO_WIDTH);
            mPlatenumEditText.setText(savedInstanceState.getString(INSTANCE_PLATENUM));
            mCommentEditText.setText(savedInstanceState.getString(INSTANCE_COMMENT));
        }

        Glide.with(this).load(mImageFilename).into(mCarImageView);
        mPostCatchedButton.setOnClickListener(this);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseStorageRef = mFirebaseStorage.getReference().child(STORAGE_CARPHOTOS);

        getWindow().setExitTransition(new Fade());
        getWindow().setEnterTransition(new Fade());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        onSaveInstanceStateCommonCode(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        onSaveInstanceStateCommonCode(outState);
    }

    private void onSaveInstanceStateCommonCode(Bundle outState) {
        outState.putString(PHOTO_FILENAME, mImageFilename);
        outState.putString(INSTANCE_PLATENUM, String.valueOf(mPlatenumEditText.getText()));
        outState.putString(INSTANCE_COMMENT, String.valueOf(mCommentEditText.getText()));
        outState.putInt(PHOTO_WIDTH, mImageWidth);
        outState.putInt(PHOTO_HEIGHT, mImageHeight);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.post_carched_btn) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = database.getReference(getString(R.string.database_ref));
            String platenum = String.valueOf(mPlatenumEditText.getText());
            String photoTitle = String.valueOf(mCommentEditText.getText());
            String photoId = databaseReference
                    .child(FirebaseHelper.CAR_PHOTOS_REFERENCE)
                    .push().getKey();

            Uri imageUri = Utils.createUriFromString(mImageFilename);
            if (imageUri != null && photoId != null) {
                StorageReference imageRef = mFirebaseStorageRef.child(photoId);
                imageRef
                        .putFile(imageUri)
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Crashlytics.log(getString(R.string.unable_to_upload));
                            return null;
                        }
                        return imageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri imageUri = task.getResult();
                            String imageUrl = imageUri.toString();
                            String userId = FirebaseAuth.getInstance().getUid();
                            FirebaseUser firebaseUser = FirebaseAuth
                                    .getInstance()
                                    .getCurrentUser();
                            if (firebaseUser == null) {
                                Snackbar
                                        .make(findViewById(android.R.id.content),
                                                R.string.auth_err,
                                                Snackbar.LENGTH_SHORT)
                                        .show();
                                Crashlytics.log(getString(R.string.auth_err));
                            } else {
                                String photoUrl = firebaseUser.getPhotoUrl() == null ?
                                        "" : firebaseUser.getPhotoUrl().toString();
                                CarPhotos carPhoto = new CarPhotos(imageUrl,
                                        mImageHeight,
                                        mImageWidth,
                                        photoTitle,
                                        userId,
                                        firebaseUser
                                                .getDisplayName(),
                                        photoUrl,
                                        platenum);

                                databaseReference
                                        .child(CAR_PHOTOS_REFERENCE)
                                        .child(photoId)
                                        .setValue(carPhoto);
                            }
                        }
                    }

                });

                finish();
            }
        }
    }
}
