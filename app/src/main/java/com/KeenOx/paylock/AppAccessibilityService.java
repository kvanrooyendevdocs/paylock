package com.KeenOx.paylock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Set;

public class AppAccessibilityService extends AccessibilityService {

    private static final String TAG = "PAYLOCK";
    private static final long LOCK_COOLDOWN_MS = 1500;

    private static final String ANDROID_SYSTEM_UI = "com.android.systemui";
    private static final String ANDROID_SETTINGS = "com.android.settings";
    private static final String PLAY_STORE = "com.android.vending";

    private String lastForegroundApp = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        String currentApp = event.getPackageName().toString();
        Log.d(TAG, "Current app: " + currentApp);

        Set<String> blockedApps = PrefsHelper.getBlockedApps(this);

        // IMPORTANT:
        // When overlay appears, Android may fire an event for our own package.
        // Ignore that event completely so we do not instantly close the overlay.
        if (currentApp.equals(getPackageName())) {
            Log.d(TAG, "Ignoring PayLock's own overlay/app event");
            return;
        }

        // If user leaves a blocked app, remove temporary allowance and hide overlay
        if (!lastForegroundApp.isEmpty()
                && blockedApps.contains(lastForegroundApp)
                && !lastForegroundApp.equals(currentApp)) {
            PrefsHelper.clearAppAllowance(this, lastForegroundApp);
            Log.d(TAG, "Cleared allowance for: " + lastForegroundApp);

            stopOverlay();
        }

        lastForegroundApp = currentApp;

        // Ignore safe/system apps
        if (shouldIgnoreApp(currentApp)) {
            return;
        }

        // If current app is not blocked, make sure overlay is gone
        if (!blockedApps.contains(currentApp)) {
            stopOverlay();
            return;
        }

        // If app is temporarily allowed, do not lock
        if (PrefsHelper.isAppCurrentlyAllowed(this, currentApp)) {
            Log.d(TAG, "App temporarily allowed: " + currentApp);
            stopOverlay();
            return;
        }

        long lastLockTime = PrefsHelper.getLastLockTime(this, currentApp);
        long now = System.currentTimeMillis();

        if (now - lastLockTime < LOCK_COOLDOWN_MS) {
            Log.d(TAG, "Skipping duplicate lock trigger for: " + currentApp);
            return;
        }

        PrefsHelper.setLastLockTime(this, currentApp, now);

        Log.d(TAG, "Blocked app opened: " + currentApp);

        if (!Settings.canDrawOverlays(this)) {
            Log.d(TAG, "Overlay permission not granted");
            return;
        }

        Intent intent = new Intent(this, OverlayLockService.class);
        intent.putExtra("blocked_app", currentApp);
        startService(intent);
    }

    private void stopOverlay() {
        Intent stopOverlayIntent = new Intent(this, OverlayLockService.class);
        stopService(stopOverlayIntent);
    }

    private boolean shouldIgnoreApp(String packageName) {
        return packageName.equals(ANDROID_SYSTEM_UI)
                || packageName.equals(ANDROID_SETTINGS)
                || packageName.equals(PLAY_STORE);
    }

    @Override
    public void onInterrupt() {
        // Required override
    }
}