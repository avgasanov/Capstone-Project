package com.tuesday_apps.catchmycar.FirebaseUtils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.tuesday_apps.catchmycar.R;
import com.tuesday_apps.catchmycar.user.User;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    public static final String SEARCH_INDEX_REFERENCE = "searchIndex";
    public static final String USERS_REFERENCE = "users";
    public static final String CARS_REFERENCE = "cars";
    public static final String FRIENDSHIPS_REFERENCE = "friendships";
    public static final String RDB_STORAGE_REF = "catchcar";
    public static final String USER_CARS_REFERENCE = "cars";
    public static final String SEARCH_PLATENUM_REFERENCE = "plateNums";
    public static final String CAR_PHOTOS_REFERENCE = "carPhotos";
    public static final String LIKES_REFERENCE = "likes";
    public static final String PROFILEPICTURE_USER_REFERENCE = "profilePicture";
    public static final String SEARCH_USER_PHOTOS_REFERENCE = "userPhotos";

    public static final String STORAGE_CARPROFILE_AVATAR = "car_profile";
    public static final String STORAGE_CARPHOTOS = "car_photos";
    public static final String STORAGE_USER_AVATAR = "user_profile";
    public static final String CATCHED_USERS_REFERENCE = "catchedUserIds";
    public static final String LIKEUNLIKE_FUNCTION = "likeUnlike";

    public static final String CLOUDFUNC_LIKEUNLIKE_CARPHOTOID_KEY = "carPhotoId";
    public static final String CLOUDFUNC_LIKEUNLIKE_LIKEBOOLEAN_KEY = "like";
    public static final String CLOUDFUNC_LIKEUNLIKE_USERPROFILEPICTURE_KEY = "profilePicture";
    public static final String CLOUDFUNC_LIKEUNLIKE_USERNAME_KEY = "username";
    public static final String COMMMENTS_REFERENCE = "comments";
    public static final String PHOTOS_IN_CARS_REFERENCE = "photos";

    public static final String THUMB_PREFIX = "thumb_";
    public static final String EUROPE_WEST = "europe-west1";
    public static final String CATCHED_NOTIF = "catched";

    public static final String NOTIFICATIONS_REF = "notifications";
    public static final String PLATENUM_IN_CARS_REF = "carPlate";

    public static final String TOKENS_REFERENCE = "tokens";

    public static final String AVATAR_PLACEHOLDER_URL = "https://firebasestorage.googleapis.com/v0/b/catch-my-car.appspot.com/o/avatar_placeholder.png?alt=media&token=411aa2f1-810f-487a-b31b-5f250fb3cbf5";
    private static final String USERNAME_REFERENCE = "username";
    public static final String NOTIFICATION_KEY_CARID = "carKey";
    public static final String NOTIFICATION_TITLE = "title";
    public static final String NOTIFICATION_BODY = "body";
    public static final int NOTIFICATION_MAX_CHARACTERS = 100;
    public static final String PHOTO_URL_IN_CARPHOTOS_REFERENCE = "photoUrl";
    public static int NOTIFICATION_TITLE_MAX_CHARACTERS = 30;

    public static final void addUserToDatabase(String database,
                                               FirebaseUser user,
                                               FirebaseDatabase firebaseDatabase) {
        String userUid = user.getUid();
        String username = user.getDisplayName();


        if (username == null) {
            username = userUid.substring(0, 10);
        }

        String profilePicture = null;
        if(user.getPhotoUrl() != null) {
            profilePicture = user.getPhotoUrl().toString();
        }
        if (profilePicture ==  null) {
            profilePicture = AVATAR_PLACEHOLDER_URL;
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(PROFILEPICTURE_USER_REFERENCE))
                    .build();
            user.updateProfile(profileUpdates);
        }

//        databaseReference
//                .child(SEARCH_INDEX_REFERENCE)
//                .child(USERS_REFERENCE)
//                .child(username)
//                .child(userUid)
//                .setValue(true);
//        databaseReference
//                .child(USERS_REFERENCE)
//                .child(userUid)
//                .child(PROFILEPICTURE_USER_REFERENCE)
//                .setValue(profilePicture);
//        databaseReference
//                .child(USERS_REFERENCE)
//                .child(userUid)
//                .child(USERNAME_REFERENCE)
//                .setValue(username);
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/" + database + "/"
                + SEARCH_INDEX_REFERENCE + "/"
                + USERS_REFERENCE + "/"
                + username.toUpperCase() + "/"
                + userUid, true);
        childUpdates.put("/" + database + "/"
                + USERS_REFERENCE + "/"
                + userUid + "/"
                + PROFILEPICTURE_USER_REFERENCE, profilePicture);
        childUpdates.put("/" + database + "/"
                + USERS_REFERENCE + "/"
                + userUid + "/"
                + USERNAME_REFERENCE, username);

        firebaseDatabase.getReference().updateChildren(childUpdates);
    }


    public static void onNewToken(String token, Context context) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getUid();
        if (userId == null) return;
        String databaseRef = context.getString(R.string.database_ref);
        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference(databaseRef);
        databaseReference
                .child(FirebaseHelper.TOKENS_REFERENCE)
                .child(userId).setValue(token);
    }
}
