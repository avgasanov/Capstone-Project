package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tuesday_apps.catchmycar.FirebaseUtils.FirebaseHelper;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.user.CCNotification;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

abstract public class BasePagingRecyclerViewAdapter<ENTITY extends Parcelable>
        extends RecyclerView.Adapter<BasePagingRecyclerViewAdapter.ViewHolder> {

    private static final String INSTANCE_ITEMS = "instance-items";
    private static final String INSTANCE_ITEM_IDS = "instance-item-ids";
    private static final String INSTANCE_INSERTION_INDEX = "instance-array-insertion-index";


    private Context mContext;
    private final BasePagingInterface mListener;

    private ArrayList<ENTITY> mItems;
    private ArrayList<String> mItemIds;
    private int mInsertionIndex;

    public BasePagingRecyclerViewAdapter(BasePagingInterface listener, Context context) {
        mContext = context;
        mListener = listener;

        initializeLocalVariables();
    }

    private void initializeLocalVariables() {
        mItems = new ArrayList<ENTITY>();
        mItemIds = new ArrayList<>();
        mInsertionIndex = 0;
    }

    public Bundle saveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(INSTANCE_ITEMS, mItems);
        outState.putStringArrayList(INSTANCE_ITEM_IDS, mItemIds);
        outState.putInt(INSTANCE_INSERTION_INDEX, mInsertionIndex);
        return outState;
    }

    public void restoreInstanceState(Bundle inState) {
        mItems = inState.getParcelableArrayList(INSTANCE_ITEMS);
        mItemIds = inState.getStringArrayList(INSTANCE_ITEM_IDS);
        mInsertionIndex = inState.getInt(INSTANCE_INSERTION_INDEX);
        if (mItems == null || mItemIds == null) {
            initializeLocalVariables();
        }
    }

    public String getLastItemId() {
        if (mItemIds == null || this.getItemCount() == 0) {
            return null;
        }
        return mItemIds.get(this.getItemCount() - 1);
    }

    public String getFirstItemId() {
        if (mItemIds == null || this.getItemCount() == 0) {
            return null;
        }
        return mItemIds.get(0);
    }

    public void nextPage() {
        mInsertionIndex = this.getItemCount();
    }

    public void addItem(String itemKey, ENTITY item) {
        insertItem(itemKey, item, mInsertionIndex);
    }

    public void addItemToTop(String itemKey, ENTITY item) {
        insertItem(itemKey, item, 0);
    }

    private void insertItem(String itemKey, ENTITY item, int position) {
        if (itemKey != null
                && mItems != null
                && !mItemIds.contains(itemKey)) {
            mItems
                    .add(position, item);
            mItemIds.add(position, itemKey);
            notifyDataSetChanged();
        }
    }

    public ArrayList<ENTITY> getItems() {
        return mItems;
    }

    public ArrayList<String> getItemIds() {
        return mItemIds;
    }

    public BasePagingInterface getListener() {
        return mListener;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_basepaging, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BasePagingRecyclerViewAdapter.ViewHolder holder, int position) {
        String title = getTitle(position);
        String imageUrl = getImageUrl(position);
        String detailItemId = getDetailItemId(position);

        Glide.with(getContext()).load(imageUrl).into(holder.mItemCircleImage);
        holder.mItemTitleTextView.setText(title);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getListener().openDetailItem(detailItemId);
            }
        });

        if(position == getItemCount() - 1) {
            getListener().loadNext();
        }
    }

    protected abstract String getDetailItemId(int position);

    protected abstract String getImageUrl(int position);

    protected abstract String getTitle(int position);

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        @BindView(R.id.base_paging_image_civ)
        CircleImageView mItemCircleImage;
        @BindView(R.id.base_paging_title_tv)
        TextView mItemTitleTextView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }
    }
}

