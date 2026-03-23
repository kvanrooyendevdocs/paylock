package com.KeenOx.paylock;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnEnableAccessibility;
    Button btnSelectApps;
    TextView tvStatus;
    TextView tvCredits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PrefsHelper.initializeCreditsIfNeeded(this);

        btnEnableAccessibility = findViewById(R.id.btnEnableAccessibility);
        btnSelectApps = findViewById(R.id.btnSelectApps);
        tvStatus = findViewById(R.id.tvStatus);
        tvCredits = findViewById(R.id.tvCredits);

        btnEnableAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        btnSelectApps.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AppSelectionActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAccessibilityStatus();
        updateCredits();
    }

    private void updateCredits() {
        int credits = PrefsHelper.getCredits(this);
        tvCredits.setText(getString(R.string.credits_format, credits));
    }

    private void updateAccessibilityStatus() {
        if (isAccessibilityServiceEnabled()) {
            tvStatus.setText(R.string.status_enabled);
        } else {
            tvStatus.setText(R.string.status_not_enabled);
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        int accessibilityEnabled = 0;
        final String serviceName = new ComponentName(this, AppAccessibilityService.class).flattenToString();

        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException e) {
            android.util.Log.e("PAYLOCK", "Accessibility setting not found", e);
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );

            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
                splitter.setString(settingValue);

                while (splitter.hasNext()) {
                    String enabledService = splitter.next();

                    if (enabledService.equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}