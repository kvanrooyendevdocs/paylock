package com.KeenOx.paylock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Set;

public class AppAccessibilityService extends AccessibilityService {

    private static final String TAG = "PAYLOCK";
    private static final long LOCK_COOLDOWN_MS = 1500;

    private String lastForegroundApp = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        CharSequence packageNameCs = event.getPackageName();
        if (packageNameCs == null) return;

        String currentApp = packageNameCs.toString();
        Log.d(TAG, "Current app: " + currentApp);

        Set<String> blockedApps = PrefsHelper.getBlockedApps(this);

        // If user leaves a blocked app, remove its temporary allowance
        if (!lastForegroundApp.isEmpty()
                && blockedApps.contains(lastForegroundApp)
                && !lastForegroundApp.equals(currentApp)) {
            PrefsHelper.clearAppAllowance(this, lastForegroundApp);
            Log.d(TAG, "Cleared allowance for: " + lastForegroundApp);
        }

        lastForegroundApp = currentApp;

        if (currentApp.equals(getPackageName())) {
            return;
        }

        if (currentApp.equals("com.android.systemui")) {
            return;
        }

        if (!blockedApps.contains(currentApp)) {
            return;
        }

        if (PrefsHelper.isAppCurrentlyAllowed(this, currentApp)) {
            Log.d(TAG, "App temporarily allowed: " + currentApp);
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

        Intent intent = new Intent(this, LockScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("blocked_app", currentApp);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {
        // Required
    }
}