package com.tuesday_apps.catchmycar.car;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Car implements Parcelable {
    String carAvatar;
    String carName;
    String carPlate;
    Boolean searchable;
    String userId;

    Map<String, Boolean> photos;


    /*
    * default constructor for firebase RDB
     */
    public Car() { }

    public Car(String carAvatar, String carName, String carPlate, Boolean searchable, String userId, Map<String, Boolean> photos) {
        this.carAvatar = carAvatar;
        this.carName = carName;
        this.carPlate = carPlate;
        this.searchable = searchable;
        this.userId = userId;
        this.photos = photos;
    }

    protected Car(Parcel in) {
        carAvatar = in.readString();
        carName = in.readString();
        carPlate = in.readString();
        byte tmpSearchable = in.readByte();
        searchable = tmpSearchable == 0 ? null : tmpSearchable == 1;
        userId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(carAvatar);
        dest.writeString(carName);
        dest.writeString(carPlate);
        dest.writeByte((byte) (searchable == null ? 0 : searchable ? 1 : 2));
        dest.writeString(userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Car> CREATOR = new Creator<Car>() {
        @Override
        public Car createFromParcel(Parcel in) {
            return new Car(in);
        }

        @Override
        public Car[] newArray(int size) {
            return new Car[size];
        }
    };

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCarPlate() {
        return carPlate;
    }

    public void setCarPlate(String carPlate) {
        this.carPlate = carPlate;
    }

    public Boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Boolean> getPhotos() {
        return photos;
    }

    public void setPhotos(Map<String, Boolean> photos) {
        this.photos = photos;
    }

    public String getCarAvatar() {
        return carAvatar;
    }

    public void setCarAvatar(String carAvatar) {
        this.carAvatar = carAvatar;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("carAvatar", carAvatar);
        result.put("carName", carName);
        result.put("carPlate", carPlate);
        result.put("searchable", searchable);
        result.put("userId", userId);
        result.put("photos", photos);
        return result;
    }
}
