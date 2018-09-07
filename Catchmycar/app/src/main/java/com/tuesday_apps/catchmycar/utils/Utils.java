package com.tuesday_apps.catchmycar.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.zxing.common.StringUtils;
import com.tuesday_apps.catchmycar.car.Car;
import com.tuesday_apps.catchmycar.user.User;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static String getTimeAndDate() {
        //There is more elegant solution in java 8, but it restricted with min API 26
        //this implementation is more universal. That is the purpose of this method:
        //change it in future without additional pain :)
        Calendar c = Calendar.getInstance();
        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        return df.format(c.getTime());
    }

    public static String trimPlateNumber(String plateNum) {
        plateNum = plateNum.replaceAll("[-_/ ]", "");
        return plateNum;
    }

    public static Uri createUriFromString(String url) {
        if (TextUtils.isEmpty(url)) return null;
        return Uri.parse(url).buildUpon().build();
    }

    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    public static long randLong(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max);
    }

    public static boolean isConnected(Context context) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

}
