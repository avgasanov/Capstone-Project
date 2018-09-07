package com.tuesday_apps.catchmycar.car;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarPhotos implements Parcelable{
    String photoUrl;
    int photoHeight;
    int photoWidth;
    String photoTitle;
    int likeCount;
    String carId;
    String catcherId;
    String catcherName;
    String catcherAvatar;
    Map<String, UserInPhoto> catchedUserIds;
    String plateNum;

    public CarPhotos(String photoUrl,
                     String photoTitle,
                     int likeCount,
                     String carId,
                     String catcherId,
                     String catcherName,
                     String catcherAvatar,
                     Map<String, UserInPhoto> catchedUserIds,
                     String plateNum) {
        this.photoUrl = photoUrl;
        this.photoTitle = photoTitle;
        this.likeCount = likeCount;
        this.carId = carId;
        this.catcherId = catcherId;
        this.catcherName = catcherName;
        this.catcherAvatar = catcherAvatar;
        this.catchedUserIds = catchedUserIds;
        this.plateNum = plateNum;
    }

    public CarPhotos(String photoUrl,
                     int photoHeight,
                     int photoWidth,
                     String photoTitle,
                     String catcherId,
                     String catcherName,
                     String catcherAvatar,
                     String plateNum) {
        this.photoUrl = photoUrl;
        this.photoHeight = photoHeight;
        this.photoWidth = photoWidth;
        this.photoTitle = photoTitle;
        this.catcherId = catcherId;
        this.catcherName = catcherName;
        this.catcherAvatar = catcherAvatar;
        this.plateNum = plateNum;
        this.likeCount = 0;
    }

    /**
     * default constructor for firebase RDB
     */
    public CarPhotos() {

    }

    protected CarPhotos(Parcel in) {
        photoUrl = in.readString();
        photoHeight = in.readInt();
        photoWidth = in.readInt();
        photoTitle = in.readString();
        likeCount = in.readInt();
        carId = in.readString();
        catcherId = in.readString();
        catcherName = in.readString();
        catcherAvatar = in.readString();
        plateNum = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(photoUrl);
        dest.writeInt(photoHeight);
        dest.writeInt(photoWidth);
        dest.writeString(photoTitle);
        dest.writeInt(likeCount);
        dest.writeString(carId);
        dest.writeString(catcherId);
        dest.writeString(catcherName);
        dest.writeString(catcherAvatar);
        dest.writeString(plateNum);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CarPhotos> CREATOR = new Creator<CarPhotos>() {
        @Override
        public CarPhotos createFromParcel(Parcel in) {
            return new CarPhotos(in);
        }

        @Override
        public CarPhotos[] newArray(int size) {
            return new CarPhotos[size];
        }
    };

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("photoUrl", photoUrl);
        result.put("photoHeight", photoHeight);
        result.put("photoWidth", photoWidth);
        result.put("photoTitle", photoTitle);
        result.put("likeCount", likeCount);
        result.put("carId", carId);
        result.put("catcherId", catcherId);
        result.put("catcherName", catcherName);
        result.put("catcherAvatar", catcherAvatar);
        result.put("catchedUserIds", catchedUserIds);
        result.put("plateNum", plateNum);
        return result;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getPhotoHeight() {
        return photoHeight;
    }

    public void setPhotoHeight(int photoHeight) {
        this.photoHeight = photoHeight;
    }

    public int getPhotoWidth() {
        return photoWidth;
    }

    public void setPhotoWidth(int photoWidth) {
        this.photoWidth = photoWidth;
    }

    public String getPhotoTitle() {
        return photoTitle;
    }

    public void setPhotoTitle(String photoTitle) {
        this.photoTitle = photoTitle;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getCatcherId() {
        return catcherId;
    }

    public void setCatcherId(String catcherId) {
        this.catcherId = catcherId;
    }

    public Map<String, UserInPhoto> getCatchedUserIds() {
        return catchedUserIds;
    }

    public void setCatchedUserIds(Map<String, UserInPhoto> catchedUserIds) {
        this.catchedUserIds = catchedUserIds;
    }

    public String getPlateNum() {
        return plateNum;
    }

    public void setPlateNum(String plateNum) {
        this.plateNum = plateNum;
    }

    public String getCatcherName() {
        return catcherName;
    }

    public void setCatcherName(String catcherName) {
        this.catcherName = catcherName;
    }

    public String getCatcherAvatar() {
        return catcherAvatar;
    }

    public void setCatcherAvatar(String catcherAvatar) {
        this.catcherAvatar = catcherAvatar;
    }

}
