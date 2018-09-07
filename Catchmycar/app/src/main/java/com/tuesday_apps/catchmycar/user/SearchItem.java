package com.tuesday_apps.catchmycar.user;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchItem implements Parcelable {
    String title;
    String id;
    String type;
    String photoUrl;

    public static final String CAR_SEARCH_ITEM = "car-search-item";
    public static final String USER_SEARCH_ITEM = "user-search-item";

    public SearchItem(String title, String id, String type, String photoUrl) {
        this.title = title;
        this.id = id;
        this.photoUrl = photoUrl;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    protected SearchItem(Parcel in) {
        title = in.readString();
        id = in.readString();
        type = in.readString();
        photoUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(photoUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchItem> CREATOR = new Creator<SearchItem>() {
        @Override
        public SearchItem createFromParcel(Parcel in) {
            return new SearchItem(in);
        }

        @Override
        public SearchItem[] newArray(int size) {
            return new SearchItem[size];
        }
    };
}
