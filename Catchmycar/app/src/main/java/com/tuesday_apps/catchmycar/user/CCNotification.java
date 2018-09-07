package com.tuesday_apps.catchmycar.user;

import android.os.Parcel;
import android.os.Parcelable;

public class CCNotification implements Parcelable {
    String carId;
    String carPhotoId;
    String catcherId;
    String catcherName;
    String photoUrl;
    long timestamp;
    String type;

    public CCNotification() {
    }

    public CCNotification(String carId,
                          String carPhotoId,
                          String catcherId,
                          String catcherName,
                          String photoUrl,
                          long timestamp,
                          String type) {
        this.carId = carId;
        this.carPhotoId = carPhotoId;
        this.catcherId = catcherId;
        this.catcherName = catcherName;
        this.photoUrl = photoUrl;
        this.timestamp = timestamp;
        this.type = type;
    }

    protected CCNotification(Parcel in) {
        carId = in.readString();
        carPhotoId = in.readString();
        catcherId = in.readString();
        catcherName = in.readString();
        photoUrl = in.readString();
        timestamp = in.readLong();
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(carId);
        dest.writeString(carPhotoId);
        dest.writeString(catcherId);
        dest.writeString(catcherName);
        dest.writeString(photoUrl);
        dest.writeLong(timestamp);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CCNotification> CREATOR = new Creator<CCNotification>() {
        @Override
        public CCNotification createFromParcel(Parcel in) {
            return new CCNotification(in);
        }

        @Override
        public CCNotification[] newArray(int size) {
            return new CCNotification[size];
        }
    };

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCatcherName() {
        return catcherName;
    }

    public void setCatcherName(String catcherName) {
        this.catcherName = catcherName;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getCarPhotoId() {
        return carPhotoId;
    }

    public void setCarPhotoId(String carPhotoId) {
        this.carPhotoId = carPhotoId;
    }

    public String getCatcherId() {
        return catcherId;
    }

    public void setCatcherId(String catcherId) {
        this.catcherId = catcherId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
