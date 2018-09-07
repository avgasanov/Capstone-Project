package com.tuesday_apps.catchmycar.car;

import android.os.Parcel;
import android.os.Parcelable;

public class CarPhotoAdapterItem implements Parcelable{
    public static final Object THUMB_PREFIX = "thumb_";
    private CarPhotos carPhoto;
    private String carPhotoId;
    private UserInPhoto catchedUserInPhoto;
    private String carThumbnail;

    private boolean likeInitialized;

    private boolean like;
    public CarPhotoAdapterItem(CarPhotos carPhoto, String carPhotoId, UserInPhoto catchedUserInPhoto) {
        this.carPhoto = carPhoto;
        this.carPhotoId = carPhotoId;
        this.catchedUserInPhoto = catchedUserInPhoto;
        likeInitialized = false;
        like = false;

    }


    protected CarPhotoAdapterItem(Parcel in) {
        carPhoto = in.readParcelable(CarPhotos.class.getClassLoader());
        carPhotoId = in.readString();
        catchedUserInPhoto = in.readParcelable(UserInPhoto.class.getClassLoader());
        carThumbnail = in.readString();
        likeInitialized = in.readByte() != 0;
        like = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(carPhoto, flags);
        dest.writeString(carPhotoId);
        dest.writeParcelable(catchedUserInPhoto, flags);
        dest.writeString(carThumbnail);
        dest.writeByte((byte) (likeInitialized ? 1 : 0));
        dest.writeByte((byte) (like ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CarPhotoAdapterItem> CREATOR = new Creator<CarPhotoAdapterItem>() {
        @Override
        public CarPhotoAdapterItem createFromParcel(Parcel in) {
            return new CarPhotoAdapterItem(in);
        }

        @Override
        public CarPhotoAdapterItem[] newArray(int size) {
            return new CarPhotoAdapterItem[size];
        }
    };

    public void setLikeInitialized(boolean likeInitialized) {
        this.likeInitialized = likeInitialized;
    }

    public void setLike(boolean like) {
        this.like = like;
    }

    public CarPhotos getCarPhoto() {
        return carPhoto;
    }

    public String getCarPhotoId() {
        return carPhotoId;
    }

    public UserInPhoto getCatchedUserInPhoto() {
        return catchedUserInPhoto;
    }

    public boolean isLikeInitialized() {
        return likeInitialized;
    }

    public boolean isLike() {
        return like;
    }

    public String getCarThumbnail() {
        return carThumbnail;
    }

    public void setCarThumbnail(String carThumbnail) {
        this.carThumbnail = carThumbnail;
    }
}
