package com.tuesday_apps.catchmycar.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

public interface FragmentNavigation {
    public void pushFragment(Fragment fragment);
    public void popFragment();
    public void loadCompleted();
    public void loadCompletedError(String errorMessage);
    public void loadCompletedError(String errorMessage,
                                   View.OnClickListener listener,
                                   String actionName);
    public void loadStarted();
}
