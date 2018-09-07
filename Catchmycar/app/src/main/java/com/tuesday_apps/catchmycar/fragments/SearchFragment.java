package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.car.Car;
import com.tuesday_apps.catchmycar.car.CarPhotos;
import com.tuesday_apps.catchmycar.user.SearchItem;
import com.tuesday_apps.catchmycar.user.User;
import com.tuesday_apps.catchmycar.utils.Utils;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchFragment extends Fragment implements SearchInterface {

    private static final int ITEMS_LIMIT = 10;
    private FragmentNavigation mFragmentNavigation;
    private SearchRecyclerViewAdapter mSearchAdapter;

    @BindView(R.id.search_rv)
    RecyclerView mSearchRecyclerView;
    @BindView(R.id.search_sv)
    SearchView mSearchView;
    private boolean mCarsRefreshingCurrently;
    private boolean mUsersRefreshingCurrently;

    private DatabaseReference mUsersSearchReference;
    private DatabaseReference mCarsSearchReference;

    private String mSearchCondition;
    private ValueEventListener mCarsEventValueListener;
    private ValueEventListener mUsersValueEventListener;
    private DatabaseReference mCarsReference;
    private DatabaseReference mUsersReference;


    public SearchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference(getString(R.string.database_ref));
        mCarsSearchReference = databaseReference
                .child(FirebaseHelper.SEARCH_INDEX_REFERENCE)
                .child(FirebaseHelper.SEARCH_PLATENUM_REFERENCE);
        mUsersSearchReference = databaseReference
                .child(FirebaseHelper.SEARCH_INDEX_REFERENCE)
                .child(FirebaseHelper.USERS_REFERENCE);
        mCarsReference = databaseReference
                .child(FirebaseHelper.CARS_REFERENCE);
        mUsersReference = databaseReference
                .child(FirebaseHelper.USERS_REFERENCE);


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSearchAdapter != null) {
            outState = mSearchAdapter.saveInstanceState(outState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_list, container, false);
        ButterKnife.bind(this, view);

        Context context = view.getContext();
        mSearchRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mSearchAdapter = new SearchRecyclerViewAdapter(context, this);

        if (savedInstanceState != null) {
            mSearchAdapter.restoreInstanceState(savedInstanceState);
        }
        mSearchRecyclerView.setAdapter(mSearchAdapter);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchCondition = query;
                mSearchAdapter.cleanAdapter();
                loadNext();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return view;
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

    @Override
    public void onUserClick(String userId) {
        if (userId == null) return;
        UserProfileFragment userProfileFragment =
                UserProfileFragment.newInstance(userId);
        mFragmentNavigation.pushFragment(userProfileFragment);
    }

    @Override
    public void onCarClick(String carId) {
        if (carId == null) return;
        CarPhotoItemsFragment carPhotoItemsFragment =
                CarPhotoItemsFragment.newInstance(carId);
        mFragmentNavigation.pushFragment(carPhotoItemsFragment);
    }

    @Override
    public void loadNext() {
        if (mCarsRefreshingCurrently || mUsersRefreshingCurrently) return;
        mCarsRefreshingCurrently = true;
        mUsersRefreshingCurrently = true;
        loadItems(mSearchAdapter.getLastCarId(),
                mSearchAdapter.getLastUserId(),
                mSearchCondition);
    }

    private void loadItems(String startCarId, String startUserId, String searchCondition) {
        if (mSearchCondition == null) return;

        Query carsQuery;
        Query usersQuery;

        createSingleEventCarsListener();
        createSingleEventUsersListener();

        if (startCarId == null) {
            carsQuery = mCarsSearchReference
                    .child(searchCondition.toUpperCase())
                    .orderByKey()
                    .limitToLast(ITEMS_LIMIT);
        } else {
            carsQuery = mCarsSearchReference
                    .child(searchCondition.toUpperCase())
                    .orderByKey()
                    .endAt(startCarId)
                    .limitToLast(ITEMS_LIMIT);
        }

        if (startUserId == null) {
            usersQuery = mUsersSearchReference
                    .child(searchCondition.toUpperCase())
                    .orderByKey()
                    .limitToLast(ITEMS_LIMIT);
        } else {
            usersQuery = mUsersSearchReference
                    .child(searchCondition)
                    .orderByKey()
                    .endAt(startUserId)
                    .limitToLast(ITEMS_LIMIT);
        }

        carsQuery.addListenerForSingleValueEvent(mCarsEventValueListener);
        usersQuery.addListenerForSingleValueEvent(mUsersValueEventListener);

    }

    private void createSingleEventCarsListener() {
        if (mCarsEventValueListener == null) {
            mCarsEventValueListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    processCars(dataSnapshot);
                    mCarsRefreshingCurrently = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Crashlytics.log(databaseError.getMessage());
                    mCarsRefreshingCurrently = false;
                }
            };
        }
    }

    private void createSingleEventUsersListener() {
        if (mUsersValueEventListener == null) {
            mUsersValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    processUsers(dataSnapshot);
                    mUsersRefreshingCurrently = false;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Crashlytics.log(databaseError.getMessage());
                    mUsersRefreshingCurrently = false;
                    mFragmentNavigation.popFragment();
                }
            };
        }
    }

    private void processCars(DataSnapshot dataSnapshot) {
        Map<String,Boolean> item;
        try{
            item = (Map<String,Boolean>) dataSnapshot.getValue();
            if (item == null) return;
            for (String key : item.keySet()) {
                mCarsReference
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Car currentCar = dataSnapshot.getValue(Car.class);
                        if (currentCar == null) return;
                        String carName = currentCar.getCarName();
                        String photoUrl = currentCar.getCarAvatar();
                        String carId = dataSnapshot.getKey();
                        String type = SearchItem.CAR_SEARCH_ITEM;
                        SearchItem searchItem =
                                new SearchItem(carName, carId, type, photoUrl);
                        mSearchAdapter.addSearchItem(carId, searchItem);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            } catch (Exception e) {
                Crashlytics.log(e.getMessage());
            }
    }

    private void processUsers(DataSnapshot dataSnapshot) {
        Map<String,Boolean> item;
        try{
            item = (Map<String,Boolean>) dataSnapshot.getValue();
            if (item == null) return;
            for (String key : item.keySet()) {
                mUsersReference
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser == null) return;
                        String username = currentUser.getUsername();
                        String photoUrl = currentUser.getProfilePicture();
                        String userId = dataSnapshot.getKey();
                        String type = SearchItem.USER_SEARCH_ITEM;
                        SearchItem searchItem =
                                new SearchItem(username, userId, type, photoUrl);
                        mSearchAdapter.addSearchItem(userId, searchItem);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        } catch (Exception e) {
            Crashlytics.log(e.getMessage());
        }
    }
}

interface SearchInterface {
    void onUserClick(String userId);
    void onCarClick(String carId);
    void loadNext();
}
