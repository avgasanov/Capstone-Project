package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.crashlytics.android.Crashlytics;
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
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.car.Comment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentsFragment extends Fragment implements View.OnClickListener,
        CommentsInterface,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_CARPHOTOID = "car-photo-id";
    private static final String INSTANCE_CAR_PHOTO_ID = "instance_car_photo_id";

    private static final int COMMENTS_PER_PAGE = 5;
    private static final int DEFAULT_COMMENT_LENGTH_LIMIT = 140;
    private String mCarPhotoId;

    @BindView(R.id.comments_rv)
    RecyclerView mCommentsRecyclerView;
    @BindView(R.id.comments_send_btn)
    Button mCommentsSendButton;
    @BindView(R.id.comments_sr)
    SwipeRefreshLayout mCommentsSwipeRefresh;
    @BindView(R.id.comments_et)
    EditText mCommentsEditText;

    private DatabaseReference mCommentsReference;


    private ValueEventListener mCommentsValueEventListener;
    private ValueEventListener mRefreshCommentsEventListener;

    boolean mAdapterRefreshingCurrently;

    private CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;
    private FragmentNavigation mFragmentNavigation;

    public CommentsFragment() {
    }

    public static CommentsFragment newInstance(String carPhotoId) {
        CommentsFragment fragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CARPHOTOID, carPhotoId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null
                && savedInstanceState.containsKey(INSTANCE_CAR_PHOTO_ID)){
            mCarPhotoId = savedInstanceState.getString(INSTANCE_CAR_PHOTO_ID);
        }
        else if (getArguments() != null) {
            mCarPhotoId = getArguments().getString(ARG_CARPHOTOID);
        }

        String databaseName = getString(R.string.database_ref);

        mCommentsReference = FirebaseDatabase
                .getInstance()
                .getReference(databaseName)
                .child(FirebaseHelper.COMMMENTS_REFERENCE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments_list, container, false);

        ButterKnife.bind(this, view);

        Context context = view.getContext();

        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(this, context);

        mCommentsRecyclerView
                 .setLayoutManager(
                         new StaggeredGridLayoutManager(1,
                                 StaggeredGridLayoutManager.VERTICAL));
        mCommentsRecyclerView
                 .setAdapter(mCommentsRecyclerViewAdapter);

        if (savedInstanceState != null) {
            mCommentsRecyclerViewAdapter.restoreInstanceState(savedInstanceState);
        }

        mCommentsSendButton.setOnClickListener(this);
        mCommentsSwipeRefresh.setOnRefreshListener(this);

        //During the extracurricular we have been implemented chat app
        //Almost the same code from chat app
        //START
        mCommentsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mCommentsSendButton.setEnabled(true);
                } else {
                    mCommentsSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mCommentsEditText
                .setFilters(
                        new InputFilter[]{new InputFilter
                                .LengthFilter(DEFAULT_COMMENT_LENGTH_LIMIT)});
        //END

        loadNext();
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mCommentsRecyclerViewAdapter.saveInstanceState(outState);
        outState.putString(INSTANCE_CAR_PHOTO_ID, mCarPhotoId);
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
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.comments_send_btn:
                sendComment();
                mCommentsEditText.setText("");
                break;
                default:
                    break;
        }
    }

    private void sendComment() {
        if (mCarPhotoId == null) return;

        String commentKey = mCommentsReference
                .child(mCarPhotoId)
                .push()
                .getKey();

        if (commentKey == null) return;

        FirebaseUser firebaseUser = FirebaseAuth
                .getInstance()
                .getCurrentUser();

        if (firebaseUser == null) {
            Crashlytics.log(getString(R.string.auth_err));
            return;
        }
        String userId = firebaseUser.getUid();
        String username = firebaseUser.getDisplayName();
        Uri photoUrl = firebaseUser.getPhotoUrl();
        String profilePicture = null;
        if (photoUrl != null) {
            profilePicture = firebaseUser.getPhotoUrl().toString();
        }
        String commentText = String.valueOf(mCommentsEditText.getText());

        Comment comment = new Comment(userId, username, profilePicture, commentText);

        mCommentsReference
                .child(mCarPhotoId)
                .child(commentKey)
                .setValue(comment)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Crashlytics.log(getString(R.string.comment_send_error));
                } else {
                    onRefresh();
                }
            }
        });
    }

    @Override
    public void onCommentAuthorClick(String commentsAuthorId) {
        UserProfileFragment userProfileFragment = UserProfileFragment.newInstance(commentsAuthorId);
        mFragmentNavigation.pushFragment(userProfileFragment);
    }

    @Override
    public void onRefresh() {
        if(!mCommentsRecyclerViewAdapter.isEmpty()) {
            loadNewComments(mCommentsRecyclerViewAdapter.getFirstCommentId());
        } else {
            loadNext();
        }
    }


    private void loadNewComments(String endCommentId) {
        if (endCommentId == null
                || mCarPhotoId == null
                || mAdapterRefreshingCurrently) return;

        mAdapterRefreshingCurrently = true;

        createSingleEventRefreshCommentsListener();

        mCommentsReference
                .child(mCarPhotoId)
                .orderByKey()
                .startAt(endCommentId)
                .addListenerForSingleValueEvent(mRefreshCommentsEventListener);
    }

    @Override
    public void loadNext() {
        if (mAdapterRefreshingCurrently) return;
        mCommentsSwipeRefresh.setRefreshing(true);
        mAdapterRefreshingCurrently = true;
        loadComments(mCommentsRecyclerViewAdapter.getLastCommentId());
    }


    private void loadComments(String startCommentId) {
        if (mCarPhotoId == null) return;

        Query query;

        createSingleEventCommentsListener();

        if (startCommentId == null) {
            query = mCommentsReference
                    .child(mCarPhotoId)
                    .orderByKey()
                    .limitToLast(COMMENTS_PER_PAGE);
        } else {
            query = mCommentsReference
                    .child(mCarPhotoId)
                    .orderByKey()
                    .endAt(startCommentId)
                    .limitToLast(COMMENTS_PER_PAGE);
        }

        query.addListenerForSingleValueEvent(mCommentsValueEventListener);

    }

    private void createSingleEventCommentsListener() {
        if (mCommentsValueEventListener == null) {
            mCommentsValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mCommentsRecyclerViewAdapter.nextPage();
                    processValueEventListenerChange(dataSnapshot, false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Crashlytics.log(databaseError.getMessage());
                    mCommentsSwipeRefresh.setRefreshing(false);
                    mFragmentNavigation.popFragment();
                }
            };
        }
    }

    private void createSingleEventRefreshCommentsListener() {
        if (mRefreshCommentsEventListener == null) {
            mRefreshCommentsEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    processValueEventListenerChange(dataSnapshot, true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Crashlytics.log(databaseError.getMessage());
                    mCommentsSwipeRefresh.setRefreshing(false);
                    mAdapterRefreshingCurrently = false;
                    mFragmentNavigation.popFragment();
                }
            };
        }
    }

    private void processValueEventListenerChange(DataSnapshot dataSnapshot, boolean isRefreshing) {
        for( DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String commentKey = snapshot.getKey();;
            Comment currentComment = snapshot.getValue(Comment.class);

            if (commentKey == null || currentComment == null) continue;

            if(!isRefreshing) {
                mCommentsRecyclerViewAdapter.addComment(commentKey, currentComment);
            } else {
                mCommentsRecyclerViewAdapter.addCommentToTop(commentKey, currentComment);
            }
        }
        mCommentsSwipeRefresh.setRefreshing(false);
        mAdapterRefreshingCurrently = false;
    }
}

interface CommentsInterface {
    void onCommentAuthorClick(String commentAuthorId);
    void loadNext();
}
