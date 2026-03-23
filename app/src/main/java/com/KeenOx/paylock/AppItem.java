package com.KeenOx.paylock;

import android.graphics.drawable.Drawable;

public class AppItem {
    private final String appName;
    private final String packageName;
    private final Drawable appIcon;

    public AppItem(String appName, String packageName, Drawable appIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    @Override
    public String toString() {
        return appName;
    }
}