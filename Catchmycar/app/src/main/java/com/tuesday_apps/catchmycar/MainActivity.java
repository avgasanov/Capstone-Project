package com.tuesday_apps.catchmycar;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.FragNavTransactionOptions;
import com.tuesday_apps.catchmycar.fragments.FragmentNavigation;
import com.tuesday_apps.catchmycar.fragments.HomeFragment;
import com.tuesday_apps.catchmycar.fragments.NotificationsFragment;
import com.tuesday_apps.catchmycar.fragments.SearchFragment;
import com.tuesday_apps.catchmycar.fragments.UserProfileFragment;
import com.tuesday_apps.catchmycar.user.User;
import com.tuesday_apps.catchmycar.utils.BitmapUtils;
import com.tuesday_apps.catchmycar.utils.Utils;
import com.tuesday_apps.catchmycar.widget.CatchWidget;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.TOKENS_REFERENCE;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.USERS_REFERENCE;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.addUserToDatabase;
import static com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper.onNewToken;


public class MainActivity extends AppCompatActivity implements
        FragNavController.RootFragmentListener,
        FragNavController.TransactionListener,
        FragmentNavigation{


    private static final String KEY_TOOLBAR_TITLE = "toolbar-title-key";

    private static final String CAR_PHOTO_FRAGMENT_EXTRA = "car-fragment-extra";

    private static final String FILE_PROVIDER_AUTHORITY =
            "com.tuesday_apps.catchmycar.fileprovider";
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ACTIVITY_PAGECOUNT = 4;
    private String DATABASE_REFERENCE;
    private String mImagePath;
    private Uri mImageUri;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private User mUser;

    private static final int RC_SIGN_IN = 123;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PHOTO_ACTIVITY_RESULT = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 200;

    private FirebaseUser mFirebaseUser;

    @BindView(R.id.main_fragments_container)
    FrameLayout mMainFragmentContainer;
    @BindView(R.id.main_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation)
    BottomNavigationView mBottomNavigationView;
    @BindView(R.id.main_activity_pb)
    ProgressBar mProgressBar;


    private FragNavController mNavController;
    private FragNavController.Builder mNavControllerBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        checkConnectivityAndInit(savedInstanceState);


        getWindow().setExitTransition(new Fade());
        getWindow().setEnterTransition(new Fade());

    }

    private void checkConnectivityAndInit(Bundle savedInstanceState) {
        if (!Utils.isConnected(this)) {
            loadCompletedError(getString(R.string.no_internet),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkConnectivityAndInit(savedInstanceState);
                        }
                    },
                    getString(R.string.refresh_fragment));
        } else {
            initActivity(savedInstanceState);
        }
    }

    private void initActivity(Bundle savedInstanceState) {
        mProgressBar.setVisibility(View.VISIBLE);

        mBottomNavigationView.setActivated(false);

        DATABASE_REFERENCE = getString(R.string.database_ref); // since there is no production yet
        // i keep this database both for release
        // to change database there could be added
        // a string to the release string resource
        // free firebase alows only one actual instance
        // for database;

        mBottomNavigationView
                .setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFirebaseAuth = FirebaseAuth.getInstance();


        mFirebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    mFirebaseAuth = firebaseAuth;
                    onSignedInInitialize(firebaseUser);
                } else {
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build()//,
                            //  new AuthUI.IdpConfig.FacebookBuilder().build()
                    ); // This require additional setup of facebook
                    // and i can't keep API keys for my facebook app :(
                    // since Facebook auth is not requirement for
                    // this project. I've just commented this out :)

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        initToolbar();

        restoreState(savedInstanceState);
        initNavController(savedInstanceState);

        if (mFirebaseUser == null) {
            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        }

        if (mNavController == null) {
            initNavController(savedInstanceState);
            mNavController = mNavControllerBuilder.build();
        }
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;

        if (getSupportActionBar() != null) {
            getSupportActionBar()
                    .setTitle(savedInstanceState.getString(KEY_TOOLBAR_TITLE));
            updateToolbar();
        }

        initNavController(savedInstanceState);
        if (mNavControllerBuilder != null) {
            mNavController = mNavControllerBuilder.build();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState,
                                    PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        onSaveInstanceStateCommonCode(outState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        onSaveInstanceStateCommonCode(outState);
    }

    private void onSaveInstanceStateCommonCode(Bundle outState) {
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }

        if (getSupportActionBar() != null) {
            outState
                    .putString(KEY_TOOLBAR_TITLE,
                            String.valueOf(getSupportActionBar().getTitle()));
        }
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
    }


    private void initNavController(Bundle savedInstanceState) {
        mNavControllerBuilder = FragNavController
                .newBuilder(savedInstanceState,
                        getSupportFragmentManager(),
                        R.id.main_fragments_container)
                .transactionListener(this)
                .rootFragmentListener(this, ACTIVITY_PAGECOUNT);

    }

    private BottomNavigationView
                .OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                    = new BottomNavigationView
                    .OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mNavController.switchTab(FragNavController.TAB1);
                    updateToolbarTitle(getString(R.string.title_home));
                    return true;
                case R.id.navigation_search:
                    mNavController.switchTab(FragNavController.TAB2);
                    updateToolbarTitle(getString(R.string.title_search));
                    return true;
                case R.id.navigation_camera:
                    cameraItemResolver();
                    return true;
                case R.id.navigation_notifications:
                    mNavController.switchTab(FragNavController.TAB3);
                    updateToolbarTitle(getString(R.string.title_notifications));
                    return true;
                case R.id.navigation_profile:
                    mNavController.switchTab(FragNavController.TAB4);
                    updateToolbarTitle(getString(R.string.title_user_profile));
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_sign_out:
                onSignedOutCleanup();
                mFirebaseAuth.signOut();
                return true;
                default:
                    return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    //I rely very hard on Advanced Android Course emojify application
    //to implement taking picture part
    private void cameraItemResolver() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    Toast
                            .makeText(
                                    this,
                                    R.string.permission_denied,
                                    Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.camera_not_found, Toast.LENGTH_LONG).show();
            return;
        }

        File file = null;
        try {
            file = BitmapUtils.createTempImageFile(this);
        } catch (IOException e) {
            Crashlytics.log(getString(R.string.unable_to_create) + e.getMessage());
        }

        if (file == null) {
            Toast
                    .makeText(this, R.string.file_create_error, Toast.LENGTH_LONG)
                    .show();
            Crashlytics.log(getString(R.string.file_not_created));
            return;
        }

        mImagePath = file.getAbsolutePath();
        mImageUri = FileProvider
                .getUriForFile(this, FILE_PROVIDER_AUTHORITY, file);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_CANCELED){
                finish();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, PhotoActivity.class);
            intent.putExtra(PhotoActivity.IMAGE_EXTRA, mImagePath);
            intent.putExtra(PhotoActivity.IMAGE_URI_EXTRA, mImageUri);
            startActivityForResult(intent, PHOTO_ACTIVITY_RESULT);
        } else if (requestCode == PHOTO_ACTIVITY_RESULT && resultCode == RESULT_OK) {
            String photo = PhotoActivity.getPhotoActivityResultUri(data);
            int photoHeight = PhotoActivity.getPhotoActivityResultHeight(data);
            int photoWidth = PhotoActivity.getPhotoActivityResultWidth(data);

            Intent postIntent = PostActivity
                    .createIntent(
                            this,
                            photo, photoHeight,
                            photoWidth);

            startActivity(postIntent);
        }
    }

    private void onSignedOutCleanup() {
        //Remove Token
        mDatabaseReference
                .child(TOKENS_REFERENCE)
                .child(mFirebaseUser.getUid())
                .setValue(null);
        //END Remove Token
        mFirebaseAuth.removeAuthStateListener(mFirebaseAuthListener);
        finish();
    }

    private void onSignedInInitialize(FirebaseUser firebaseUser) {
        initializeUser(firebaseUser);

        loadCompleted();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child(DATABASE_REFERENCE);

        mFirebaseUser = firebaseUser;

        mProgressBar.setVisibility(View.GONE);

        mBottomNavigationView.setSelectedItemId(0);
        mBottomNavigationView.setActivated(true);

        FirebaseInstanceId
                .getInstance()
                .getInstanceId()
                .addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceToken = instanceIdResult.getToken();
                onNewToken(deviceToken, getBaseContext());
            }
        });

        mDatabaseReference
                .child(USERS_REFERENCE)
                .child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    dataSnapshot.getValue(User.class);
                    Log.v(TAG, "data exist log " + firebaseUser.getUid());
                } else {
                    addUserToDatabase(getString(R.string.database_ref),
                            mFirebaseUser, mFirebaseDatabase);
                }
                loadCompleted();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Crashlytics.log(getString(R.string.user_read_error));
                loadCompletedError(getString(R.string.internet_conn_err));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFirebaseAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFirebaseAuthListener != null) {
            mFirebaseAuth.addAuthStateListener(mFirebaseAuthListener);
            CatchWidget.update(getApplication(), this);
        }

    }

    public void initializeUser(FirebaseUser user) {

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseUser = user;

        mBottomNavigationView
                .setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mBottomNavigationView.dispatchSetActivated(true);

    }


    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {

            case 0:
                loadStarted();
                return
                        HomeFragment
                                .newInstance(mFirebaseAuth.getUid());
            case 1:
                return
                        new SearchFragment();
            case 2:
                return
                        new NotificationsFragment();
            case 3:
                loadStarted();
                return
                        UserProfileFragment
                                .newInstance(mFirebaseAuth.getUid());
                default:
                    return null;
        }
    }

    @Override
    public void onTabTransaction(@Nullable Fragment fragment, int i) {
        updateToolbar();
    }

    @Override
    public void onFragmentTransaction(Fragment fragment,
                                      FragNavController.TransactionType transactionType) {
        updateToolbar();
    }

    private void updateToolbar() {
        if (mNavController == null || getSupportActionBar() == null) return;
        getSupportActionBar()
                .setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        getSupportActionBar()
                .setDisplayShowHomeEnabled(!mNavController.isRootFragment());
        getSupportActionBar()
                .setHomeAsUpIndicator(R.drawable.ic_arrow_up);
    }

    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            mNavController.pushFragment(fragment);
        }
    }

    @Override
    public void popFragment() {
        if (mNavController != null) {
            mNavController.popFragment();
        }
    }

    @Override
    public void loadCompleted() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void loadStarted() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadCompletedError(String errorMessage) {
        mProgressBar.setVisibility(View.GONE);
        Snackbar
                .make(findViewById(android.R.id.content),
                        errorMessage,
                        Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void loadCompletedError(String errorMessage,
                                   View.OnClickListener listener,
                                   String actionName) {
        mProgressBar.setVisibility(View.GONE);
        Snackbar
        .make(findViewById(android.R.id.content),
                errorMessage,
                Snackbar.LENGTH_INDEFINITE)
            .setAction(actionName, listener)
            .show();
    }

    public void updateToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavController == null) return;
        if (!mNavController.isRootFragment()) {
            mNavController.popFragment();
        } else {
            super.onBackPressed();
        }
    }

    public static Intent generateCarPhotoIntent(Context context, String carPhotoId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(CAR_PHOTO_FRAGMENT_EXTRA, carPhotoId);
        return intent;
    }
}
