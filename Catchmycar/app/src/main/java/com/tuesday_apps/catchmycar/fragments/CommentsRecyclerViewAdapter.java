package com.tuesday_apps.catchmycar.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.car.Comment;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentsRecyclerViewAdapter.ViewHolder> {

    private static final String INSTANCE_COMMENT_ITEMS = "instance-comment-items";
    private static final String INSTANCE_COMMENT_IDS = "instance_comment_ids";
    private static final String INSTANCE_INSERTION_INDEX = "instance-insertion-index";
    private final CommentsInterface mListener;
    private final Context mContext;

    private ArrayList<String> mCommentsIds;
    private ArrayList<Comment> mComments;
    private int mArrayInsertionIndex;

    public CommentsRecyclerViewAdapter(CommentsInterface listener, Context context) {
        mListener = listener;
        mContext = context;

        initializeLocalVariables();
    }

    private void initializeLocalVariables() {
        mCommentsIds = new ArrayList<>();
        mComments = new ArrayList<>();
    }

    public Bundle saveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(INSTANCE_COMMENT_ITEMS, mComments);
        outState.putStringArrayList(INSTANCE_COMMENT_IDS, mCommentsIds);
        outState.putInt(INSTANCE_INSERTION_INDEX, mArrayInsertionIndex);
        return outState;
    }

    public void restoreInstanceState(Bundle inState) {
        mComments = inState.getParcelableArrayList(INSTANCE_COMMENT_ITEMS);
        mCommentsIds = inState.getStringArrayList(INSTANCE_COMMENT_IDS);
        mArrayInsertionIndex = inState.getInt(INSTANCE_INSERTION_INDEX);
        if (mComments == null || mCommentsIds == null) {
            initializeLocalVariables();
        }
    }

    public String getLastCommentId() {
        if (mCommentsIds == null || this.getItemCount() == 0) {
            return null;
        }
        return mCommentsIds.get(this.getItemCount() - 1);
    }

    public String getFirstCommentId() {
        if (mCommentsIds == null || this.getItemCount() == 0) {
            return null;
        }
        return mCommentsIds.get(0);
    }

    public void nextPage() {
        mArrayInsertionIndex = this.getItemCount();
    }

    public void addComment(String commentKey, Comment comment) {
        insertComment(commentKey, comment, mArrayInsertionIndex);
    }

    public void addCommentToTop(String commentKey, Comment comment) {
        insertComment(commentKey, comment, 0);
    }

    private void insertComment(String commentKey, Comment comment, int position) {
        if (commentKey != null
                && comment != null
                && !mCommentsIds.contains(commentKey)) {
            mComments
                    .add(position, comment);
            mCommentsIds.add(position, commentKey);
            notifyDataSetChanged();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_comments, parent, false);
        return new ViewHolder(view);
    }

    //helped with spannable text:
    //https://stackoverflow.com/questions/10696986/how-to-set-the-part-of-the-text-view-is-clickable
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Comment currentComment = mComments.get(position);
        if (currentComment == null) return;

        String profilePicture = currentComment.getProfilePicture();
        String commentAuthor = currentComment.getUsername();
        String commentItself = currentComment.getComment();
        String commentAuthorUserId = currentComment.getUserId();
        String resultComment = commentAuthor + " " + commentItself;

        SpannableString spannableComment = new SpannableString(resultComment);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                mListener.onCommentAuthorClick(commentAuthorUserId);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        spannableComment
                .setSpan(clickableSpan,
                        0,
                        commentAuthor.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        Glide.with(mContext).load(profilePicture).into(holder.mCommentAuthorCircleImage);

        holder.mUsernameAndCommentTextView.setText(spannableComment);
        holder.mUsernameAndCommentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        holder.mUsernameAndCommentTextView.setHighlightColor(Color.TRANSPARENT);

        holder.mCommentAuthorCircleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCommentAuthorClick(mComments.get(position).getUserId());
            }
        });

        if(position == getItemCount() - 1) {
            mListener.loadNext();
        }
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        @BindView(R.id.comments_author_iv)
        CircleImageView mCommentAuthorCircleImage;
        @BindView(R.id.username_and_comment_tv)
        TextView mUsernameAndCommentTextView;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            ButterKnife.bind(this, view);
        }

    }
}
