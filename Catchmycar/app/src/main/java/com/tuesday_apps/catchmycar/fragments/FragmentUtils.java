package com.tuesday_apps.catchmycar.fragments;

import android.support.v4.app.Fragment;

import com.tuesday_apps.catchmycar.R;

import java.util.ArrayList;

import static com.ncapdevi.fragnav.FragNavController.TAB1;
import static com.ncapdevi.fragnav.FragNavController.TAB2;
import static com.ncapdevi.fragnav.FragNavController.TAB3;
import static com.ncapdevi.fragnav.FragNavController.TAB4;

public class FragmentUtils {

    public static int getNavigationItemId(int position) {
        switch (position) {
            case TAB1:
                return R.id.navigation_home;
            case TAB2:
                return R.id.navigation_search;
            case TAB3:
                return R.id.navigation_notifications;
            case TAB4:
                return R.id.navigation_profile;
                default: return -1;
        }

    }
}
