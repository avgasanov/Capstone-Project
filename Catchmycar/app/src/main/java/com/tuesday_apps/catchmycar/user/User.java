package com.tuesday_apps.catchmycar.user;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable, Parcelable {

    Map<String, Boolean> cars;
    String username;
    String firstName;
    String lastName;
    String profilePicture;
    boolean searchable;
    String token;

    protected User(Parcel in) {
        username = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        profilePicture = in.readString();
        searchable = in.readByte() != 0;
        token = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(profilePicture);
        dest.writeByte((byte) (searchable ? 1 : 0));
        dest.writeString(token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User() {}

    public User(String username,
                String profilePicture)
    {
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public Map<String, Boolean> getCars() {
        return cars;
    }

    public void setCars(Map<String, Boolean> cars) {
        this.cars = cars;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("cars", cars);
        result.put("username", username);
        result.put("firstName", firstName);
        result.put("lastName", lastName);
        result.put("profilePicture", profilePicture);
        result.put("searchable", searchable);
        result.put("token", token);
        return result;
    }

    public String extractUsername() {
        String username = getFirstName() + getLastName();
        if (username.isEmpty()) {
            return getUsername();
        } else {
            return username;
        }
    }
}
