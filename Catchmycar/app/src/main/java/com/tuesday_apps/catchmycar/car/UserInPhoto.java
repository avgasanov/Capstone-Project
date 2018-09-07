package com.tuesday_apps.catchmycar.car;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

public class UserInPhoto implements Parcelable{
    String profilePicture;
    String username;
    String userId;

    public UserInPhoto(String profilePicture, String username, String userId) {
        this.profilePicture = profilePicture;
        this.username = username;
        this.userId = userId;
    }

    public UserInPhoto() {
    }

    protected UserInPhoto(Parcel in) {
        profilePicture = in.readString();
        username = in.readString();
        userId = in.readString();
    }

    public static final Creator<UserInPhoto> CREATOR = new Creator<UserInPhoto>() {
        @Override
        public UserInPhoto createFromParcel(Parcel in) {
            return new UserInPhoto(in);
        }

        @Override
        public UserInPhoto[] newArray(int size) {
            return new UserInPhoto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(profilePicture);
        dest.writeString(username);
        dest.writeString(userId);
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    @Exclude
    public String getUsername() {
        return username;
    }

    @Exclude
    public void setUsername(String userName) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
