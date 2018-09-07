package com.tuesday_apps.catchmycar.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;



abstract public class BasePagingFragment<ENTITY extends Parcelable>
        extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, BasePagingInterface {

    private static final int ITEMS_PER_PAGE = 5;
    private FragmentNavigation mFragmentNavigation;
    private DatabaseReference mBasePagingReference;
    private BasePagingRecyclerViewAdapter mBasePagingAdapter;

    @BindView(R.id.base_paging_rv)
    RecyclerView mBasePagingRecyclerView;
    @BindView(R.id.base_paging_sr)
    SwipeRefreshLayout mBasePagingSwipeRefresh;
    private boolean mAdapterRefreshingCurrently;

    private ValueEventListener mNotificationsValueEventListener;
    private ValueEventListener mRefreshNotificationsEventListener;

    public BasePagingFragment() {
        FirebaseUser firebaseUser = FirebaseAuth
                .getInstance()
                .getCurrentUser();
        if (firebaseUser == null) {
            Crashlytics.log(getString(R.string.auth_err));
            return;
        }

        setLocalVariables();
    }

    abstract protected void setLocalVariables();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String databaseName = getString(R.string.database_ref);

        mBasePagingReference = getPagingReference(databaseName);
    }

    protected abstract DatabaseReference getPagingReference(String databaseName);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_basepaging_list,
                container,
                false);
        ButterKnife.bind(this, view);

        Context context = view.getContext();

        mBasePagingSwipeRefresh.setOnRefreshListener(this);

        mBasePagingAdapter = getBaseAdapter(context);
        mBasePagingRecyclerView
                .setLayoutManager(
                        new StaggeredGridLayoutManager(
                                1,
                                StaggeredGridLayoutManager.VERTICAL));
        mBasePagingRecyclerView.setAdapter(mBasePagingAdapter);

        if (savedInstanceState != null) {
            mBasePagingAdapter.restoreInstanceState(savedInstanceState);
        }

        loadNext();
        return view;
    }

    protected abstract BasePagingRecyclerViewAdapter getBaseAdapter(Context context);

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mBasePagingAdapter != null) {
            outState = mBasePagingAdapter.saveInstanceState(outState);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentNavigation) {
            mFragmentNavigation = (FragmentNavigation) context;
        } else {
            Crashlytics.log(getString(R.string.activity_must_impl_nav));
            Activity activity = getActivity();
            if (activity != null)
                activity
                        .getFragmentManager()
                        .popBackStack();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentNavigation = null;
    }

    private void loadNewItems(String endNotificationId) {
        if (mAdapterRefreshingCurrently) return;

        if (endNotificationId == null) loadNext();

        mAdapterRefreshingCurrently = true;

        createSingleEventRefreshNotificationsListener();

        mBasePagingReference
                .orderByKey()
                .startAt(endNotificationId)
                .addListenerForSingleValueEvent(mRefreshNotificationsEventListener);
    }

    protected void createSingleEventCommentsListener() {
        if (mNotificationsValueEventListener == null) {
            mNotificationsValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mBasePagingAdapter.nextPage();
                    processValueEventListenerChange(dataSnapshot, false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Crashlytics.log(databaseError.getMessage());
                    mBasePagingSwipeRefresh.setRefreshing(false);
                    mFragmentNavigation.popFragment();
                }
            };
        }
    }

    protected void createSingleEventRefreshNotificationsListener() {
        if (mRefreshNotificationsEventListener == null) {
            mRefreshNotificationsEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    processValueEventListenerChange(dataSnapshot, true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Crashlytics.log(databaseError.getMessage());
                    mBasePagingSwipeRefresh.setRefreshing(false);
                    mAdapterRefreshingCurrently = false;
                }
            };
        }
    }

    private void processValueEventListenerChange(DataSnapshot dataSnapshot, boolean isRefreshing) {
        for( DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String itemKey = snapshot.getKey();;
            ENTITY currentItem = getEntityVal(snapshot);

            if (itemKey == null || currentItem == null) continue;

            if(!isRefreshing) {
                mBasePagingAdapter.addItem(itemKey, currentItem);
            } else {
                mBasePagingAdapter.addItemToTop(itemKey, currentItem);
            }
        }
        mBasePagingSwipeRefresh.setRefreshing(false);
        mAdapterRefreshingCurrently = false;
    }

    protected abstract ENTITY getEntityVal(DataSnapshot snapshot);

    @Override
    public void onRefresh() {
        if(mBasePagingAdapter.isEmpty()) {
            loadNext();
        } else {
            loadNewItems(mBasePagingAdapter.getFirstItemId());
        }
    }

    @Override
    abstract public void openDetailItem(String itemId);

    @Override
    public void loadNext() {
        if (mAdapterRefreshingCurrently) return;
        mBasePagingSwipeRefresh.setRefreshing(true);
        mAdapterRefreshingCurrently = true;
        loadItems(mBasePagingAdapter.getLastItemId());
    }

    protected void loadItems(String startNotifId) {
        Query query;

        createSingleEventCommentsListener();

        if (startNotifId == null) {
            query = mBasePagingReference
                    .orderByKey()
                    .limitToLast(ITEMS_PER_PAGE);
        } else {
            query = mBasePagingReference
                    .orderByKey()
                    .endAt(startNotifId)
                    .limitToLast(ITEMS_PER_PAGE);
        }

        query.addListenerForSingleValueEvent(mNotificationsValueEventListener);

    }

    public void pushFragment(Fragment fragment) {
        mFragmentNavigation.pushFragment(fragment);
    }
}

interface BasePagingInterface {
    void openDetailItem(String itemId);
    void loadNext();
}
