package com.KeenOx.paylock;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class PrefsHelper {

    private static final String PREFS_NAME = "PayLockPrefs";
    private static final String KEY_BLOCKED_APPS = "blocked_apps";
    private static final String KEY_LAST_LOCK_TIME_PREFIX = "last_lock_time_";
    private static final String KEY_CREDITS = "credits";
    private static final String KEY_ALLOWED_UNTIL_PREFIX = "allowed_until_";

    public static Set<String> getBlockedApps(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>()));
    }

    public static void setBlockedApps(Context context, Set<String> blockedApps) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putStringSet(KEY_BLOCKED_APPS, new HashSet<>(blockedApps)).apply();
    }

    public static void clearBlockedApps(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_BLOCKED_APPS).apply();
    }

    public static void allowAppUntil(Context context, String packageName, long allowedUntil) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_ALLOWED_UNTIL_PREFIX + packageName, allowedUntil).apply();
    }

    public static long getAllowedUntil(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_ALLOWED_UNTIL_PREFIX + packageName, 0);
    }

    public static boolean isAppCurrentlyAllowed(Context context, String packageName) {
        long allowedUntil = getAllowedUntil(context, packageName);
        return System.currentTimeMillis() < allowedUntil;
    }

    public static void clearAppAllowance(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_ALLOWED_UNTIL_PREFIX + packageName).apply();
    }

    public static void setLastLockTime(Context context, String packageName, long time) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_LAST_LOCK_TIME_PREFIX + packageName, time).apply();
    }

    public static long getLastLockTime(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_LOCK_TIME_PREFIX + packageName, 0);
    }

    public static void initializeCreditsIfNeeded(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (!prefs.contains(KEY_CREDITS)) {
            prefs.edit().putInt(KEY_CREDITS, 10).apply();
        }
    }

    public static int getCredits(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_CREDITS, 10);
    }

    public static void setCredits(Context context, int credits) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_CREDITS, credits).apply();
    }

    public static boolean spendCredit(Context context) {
        int credits = getCredits(context);

        if (credits <= 0) {
            return false;
        }

        setCredits(context, credits - 1);
        return true;
    }
}