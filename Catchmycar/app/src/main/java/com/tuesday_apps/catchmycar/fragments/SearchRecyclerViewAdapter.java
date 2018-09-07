package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.user.SearchItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder> {

    private static final String INSTANCE_SEARCH_ITEMS = "instance-search-items";
    private static final String INSTANCE_INSERTION_INDEX = "instance-insertion-index";
    private static final String INSTANCE_USER_IDS = "instance-user-ids";
    private static final String INSTANCE_CAR_IDS = "instance-car-ids";
    private SearchInterface mListener;
    private Context mContext;

    private int mArrayInsertionIndex;
    private ArrayList<SearchItem> mSearchItems;
    private ArrayList<String> mCarIds;
    private ArrayList<String> mUserIds;

    public SearchRecyclerViewAdapter(Context context, SearchInterface listener) {
        mContext = context;
        mListener = listener;

        initializeLocalVariables();
    }

    private void initializeLocalVariables() {
        mSearchItems = new ArrayList<>();
        mArrayInsertionIndex = 0;
        mCarIds = new ArrayList<>();
        mUserIds = new ArrayList<>();
    }

    public Bundle saveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(INSTANCE_SEARCH_ITEMS, mSearchItems);
        outState.putInt(INSTANCE_INSERTION_INDEX, mArrayInsertionIndex);
        outState.putStringArrayList(INSTANCE_CAR_IDS, mCarIds);
        outState.putStringArrayList(INSTANCE_USER_IDS, mUserIds);
        return outState;
    }

    public void restoreInstanceState(Bundle inState) {
        mArrayInsertionIndex = inState.getInt(INSTANCE_INSERTION_INDEX);
        mSearchItems = inState.getParcelableArrayList(INSTANCE_SEARCH_ITEMS);
        mUserIds = inState.getStringArrayList(INSTANCE_USER_IDS);
        mCarIds = inState.getStringArrayList(INSTANCE_CAR_IDS);
        if (mSearchItems == null) {
            initializeLocalVariables();
        }
    }

    public String getLastUserId() {
        if (mUserIds.size() == 0) return null;
        return mUserIds.get(mUserIds.size() - 1);
    }

    public String getLastCarId() {
        if (mCarIds.size() == 0) return null;
        return mCarIds.get(mCarIds.size() - 1);
    }

    public void nextPage() {
        mArrayInsertionIndex = getItemCount();
    }

    public void addSearchItem(String itemKey, SearchItem item) {
        insertItem(itemKey, item, mArrayInsertionIndex);
    }

    private void insertItem(@NonNull String itemKey, @NonNull SearchItem item, int position) {
        if (!mUserIds.contains(itemKey)
                && !mCarIds.contains(itemKey))
        {
            if(item.getType().equals(SearchItem.CAR_SEARCH_ITEM)) {
                mCarIds.add(itemKey);
            } else {
                mUserIds.add(itemKey);
            }
            mSearchItems
                    .add(position, item);
            notifyDataSetChanged();
        }
    }

    public void cleanAdapter() {
        initializeLocalVariables();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_basepaging, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SearchItem currentItem = mSearchItems.get(position);
       String title = currentItem.getTitle();
       String imageUrl = currentItem.getPhotoUrl();
       String type = currentItem.getType();
       String id = currentItem.getId();

       holder.mSearchItemTextView.setText(title);
       Glide.with(mContext).load(imageUrl).into(holder.mSearchItemImage);

       holder.mView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(type.equals(SearchItem.CAR_SEARCH_ITEM)) {
                   mListener.onCarClick(id);
               } else {
                   mListener.onUserClick(id);
               }
           }
       });
        if(position == getItemCount() - 1) {
            mListener.loadNext();
        }
    }

    @Override
    public int getItemCount() {
        return mSearchItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @BindView(R.id.base_paging_title_tv)
        TextView mSearchItemTextView;
        @BindView(R.id.base_paging_image_civ)
        CircleImageView mSearchItemImage;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this,view);
        }
    }
}
