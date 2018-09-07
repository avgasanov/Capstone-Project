package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.car.CarPhotos;
import com.tuesday_apps.catchmycar.car.UserInPhoto;
import com.tuesday_apps.catchmycar.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HomeFragment extends Fragment implements homePhotoCardListItemListener, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final int PHOTOS_PER_PAGE = 5;
    private static final String INSTANCE_ITEM_ID = "instance-item-id";
    public static final String ARG_ITEM_ID = "arg-item-id";
    private static final String INSTANCE_SCROLL_STATE = "instance-scroll-state";


    private String mItemId;

    private FirebaseDatabase mFirebaseDatabase;
    protected DatabaseReference mDatabaseReference;
    private ValueEventListener mPhotosValueEventListener;
    private ValueEventListener mRefreshPhotosEventListener;

    @BindView(R.id.home_rv)
    RecyclerView mRecyclerView;

    @BindView(R.id.refreshHome)
    SwipeRefreshLayout mSwipeRefresh;

    private HomeRecyclerViewAdapter mRecyclerViewAdapter;
    private StaggeredGridLayoutManager mLayoutManager;

    private FragmentNavigation mFragmentNavigation;

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String itemId) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = generateArgs(itemId);
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle generateArgs(String itemId) {
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, itemId);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle args = getArguments();
            mItemId = args.getString(ARG_ITEM_ID);
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference(getString(R.string.database_ref));

    }

    protected String getItemId() {
        return mItemId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_list, container, false);
        ButterKnife.bind(this, view);

            Context context = view.getContext();

            mLayoutManager =
                    new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerViewAdapter = new HomeRecyclerViewAdapter(this, context);
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            mSwipeRefresh.setOnRefreshListener(this);
            loadPhotos(null, getPhotosReference());

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INSTANCE_ITEM_ID, mItemId);
        if (mRecyclerView != null) {
            mRecyclerViewAdapter.saveState(outState);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;
        mRecyclerViewAdapter.restoreState(savedInstanceState);
    }

    @Override
    public void onRefresh() {
        if (!Utils.isConnected(getContext())) {
            mFragmentNavigation
                    .loadCompletedError(getString(R.string.no_internet));
            mSwipeRefresh.setRefreshing(false);
            return;
        }
        if (mRecyclerViewAdapter.isEmpty()) {
            loadNext();
        } else {
            loadNewPhotos(mRecyclerViewAdapter.getFirstPhotoId(), getPhotosReference());
        }
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
        mFragmentNavigation = null;
    }

    private void loadPhotos(String startCarPhotoId, DatabaseReference photoReference) {
        Query query;

        if (mPhotosValueEventListener == null) {
            mPhotosValueEventListener = createSingleEventPhotoListener();
        }

        if (startCarPhotoId == null) {
            query = photoReference
                    .orderByKey()
                    .limitToLast(PHOTOS_PER_PAGE);
        } else {
            query = photoReference
                    .orderByKey()
                    .endAt(startCarPhotoId)
                    .limitToLast(PHOTOS_PER_PAGE);
        }
        query.addListenerForSingleValueEvent(mPhotosValueEventListener);
    }

    private void loadNewPhotos(String endCarPhotoId, DatabaseReference photosReference) {
        if (endCarPhotoId == null) return;

        if(mRefreshPhotosEventListener == null) {
            mRefreshPhotosEventListener = createSingleEventRefreshPhotoListener();
        }

        photosReference
                .orderByKey()
                .startAt(endCarPhotoId)
                .addListenerForSingleValueEvent(mRefreshPhotosEventListener);
    }

    private ValueEventListener createSingleEventPhotoListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mRecyclerViewAdapter.nextPage();
                    processValueEventListenerChange(dataSnapshot, false);
                } else {
                    mFragmentNavigation
                            .loadCompletedError(getString(R.string.no_photos_found));
                }
                mSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Crashlytics.log(databaseError.getMessage());
                mSwipeRefresh.setRefreshing(false);
                mFragmentNavigation
                        .loadCompletedError(
                                getString(R.string.internet_conn_err),
                                HomeFragment.this,
                                getString(R.string.refresh_fragment));
            }
        };
    }

    private ValueEventListener createSingleEventRefreshPhotoListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                processValueEventListenerChange(dataSnapshot, true);
                mSwipeRefresh.setRefreshing(false);
                mFragmentNavigation.loadCompleted();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Crashlytics.log(databaseError.getMessage());
                mSwipeRefresh.setRefreshing(false);
                mFragmentNavigation.loadCompleted();
            }
        };
    }

    protected void processValueEventListenerChange(DataSnapshot dataSnapshot, boolean isRefreshing) {
        for( DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String carPhotoKey = snapshot.getKey();;
            CarPhotos carPhoto = snapshot.getValue(CarPhotos.class);
            if (carPhoto == null || carPhotoKey == null) continue;
            UserInPhoto randomCatchedUser = createRandomCatchedUser(carPhoto);
            addPhotoToAdapterHelper(isRefreshing, carPhoto, carPhotoKey, randomCatchedUser);
        }
    }

    protected UserInPhoto createRandomCatchedUser(CarPhotos carPhoto) {
        UserInPhoto randomCatchedUser = null;
        Map<String,UserInPhoto> catchedUserIds = carPhoto.getCatchedUserIds();
        if (catchedUserIds != null) {
            int catchedUsersCount = catchedUserIds.size();
            int randUser = Utils.randInt(0, catchedUsersCount);
            List<Object> valuesList = new ArrayList<Object>(catchedUserIds.keySet());
            String randUserKey = (String) valuesList.get(randUser);
            randomCatchedUser = catchedUserIds.get(randUserKey);
            randomCatchedUser.setUserId(randUserKey);
        }
        return randomCatchedUser;
    }

    protected void addPhotoToAdapterHelper(boolean isRefreshing,
                                           CarPhotos carPhoto,
                                           String carPhotoKey,
                                           UserInPhoto randomCatchedUser) {
        if(!isRefreshing) {
            mRecyclerViewAdapter.addPhoto(carPhoto, carPhotoKey, randomCatchedUser);
        } else {
            mRecyclerViewAdapter.addPhotoToTop(carPhoto, carPhotoKey, randomCatchedUser);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onShowCommentsClick(String photoId) {
        commentClick(photoId);
    }

    @Override
    public void onCommentClick(String photoId) {
        commentClick(photoId);
    }

    private void commentClick(String photoId) {
        CommentsFragment newCommentsFragment = CommentsFragment.newInstance(photoId);
        mFragmentNavigation.pushFragment(newCommentsFragment);
    }

    @Override
    public void onCatcherClick(String catcherId) {
        pushUserProfileFragment(catcherId);
    }

    @Override
    public void onCatchedClick(String catchedId) {
        pushUserProfileFragment(catchedId);

    }

    public void pushUserProfileFragment(String userId) {
        UserProfileFragment newUserProfileFragment = UserProfileFragment.newInstance(userId);
        mFragmentNavigation.pushFragment(newUserProfileFragment);
    }

    @Override
    public void onLikeClick(String PhotoId, TextView likesCount, ToggleButton likeButton) {
        boolean like = likeButton.isChecked();
        likeUnlike(PhotoId, like).addOnCompleteListener(new OnCompleteListener<Object>() {
            @Override
            public void onComplete(@NonNull Task<Object> task) {
                if (!task.isSuccessful()) {
                    Exception e = task.getException();
                    Log.w(TAG, "Like failure ", e);
                    Toast
                            .makeText(getContext(),R.string.likeError, Toast.LENGTH_LONG)
                            .show();
                    likeButton.setChecked(!like);
                    return;
                }
                task.getResult();

                try {
                    HashMap<String, Integer> result = (HashMap<String, Integer>) task.getResult();
                    likesCount.setText(String.valueOf(result.get("likeCount")));
                } catch (ClassCastException e) {
                    Crashlytics.log(e.getMessage());
                }

            }
        });
    }

    private Task<Object> likeUnlike(String carPhotoId, boolean like) {
        Map<String, Object> data = new HashMap<>();
        data.put(FirebaseHelper.CLOUDFUNC_LIKEUNLIKE_CARPHOTOID_KEY, carPhotoId);
        data.put(FirebaseHelper.CLOUDFUNC_LIKEUNLIKE_LIKEBOOLEAN_KEY, like);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            Uri profilePictureUri = firebaseUser.getPhotoUrl();
            if (profilePictureUri != null) {
                data.
                        put(FirebaseHelper.CLOUDFUNC_LIKEUNLIKE_USERPROFILEPICTURE_KEY,
                                profilePictureUri.toString());
            }

            data
                    .put(FirebaseHelper.CLOUDFUNC_LIKEUNLIKE_USERNAME_KEY,
                            firebaseUser.getDisplayName());
        }

        return FirebaseFunctions
                .getInstance(FirebaseHelper.EUROPE_WEST)
                .getHttpsCallable(FirebaseHelper.LIKEUNLIKE_FUNCTION)
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Object>() {
                    @Override
                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return task.getResult().getData();
                    }
                });
    }

    @Override
    public void loadNext() {
        DatabaseReference photoReference = getPhotosReference();
        loadPhotos(mRecyclerViewAdapter.getLastPhotoId(), photoReference);
    }

    protected DatabaseReference getPhotosReference() {
        return mDatabaseReference
                .child(FirebaseHelper.CAR_PHOTOS_REFERENCE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == android.R.id.content) {
            loadNext();
        }
    }
}

interface homePhotoCardListItemListener {
    void onShowCommentsClick(String photoId);
    void onCommentClick(String photoId);
    void onCatcherClick(String catcherId);
    void onCatchedClick(String catchedId);
    void onLikeClick(String PhotoId, TextView likesCount, ToggleButton likeButton);
    void loadNext();
};

