package com.tuesday_apps.catchmycar.car;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable{
    String userId;
    String username;
    String profilePicture;
    Object timestamp;
    String comment;

    public Comment() {
    }

    public Comment(String userId, String username, String profilePicture, String comment) {
        this.userId = userId;
        this.username = username;
        this.profilePicture = profilePicture;
        this.comment = comment;
    }

    protected Comment(Parcel in) {
        userId = in.readString();
        username = in.readString();
        profilePicture = in.readString();
        comment = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(username);
        dest.writeString(profilePicture);
        dest.writeString(comment);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
