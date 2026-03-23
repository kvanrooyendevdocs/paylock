package com.KeenOx.paylock;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends AppCompatActivity {

    ListView listViewApps;
    Button btnSaveApps;

    ArrayList<String> appNames = new ArrayList<>();
    ArrayList<String> packageNames = new ArrayList<>();

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        listViewApps = findViewById(R.id.listViewApps);
        btnSaveApps = findViewById(R.id.btnSaveApps);

        prefs = getSharedPreferences("PayLockPrefs", MODE_PRIVATE);

        loadInstalledApps();

        btnSaveApps.setOnClickListener(v -> saveSelectedApps());
    }

    private void loadInstalledApps() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            String appName = packageManager.getApplicationLabel(app).toString();
            String packageName = app.packageName;

            if (packageManager.getLaunchIntentForPackage(packageName) != null
                    && !packageName.equals(getPackageName())) {
                appNames.add(appName + "\n" + packageName);
                packageNames.add(packageName);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                appNames
        );

        listViewApps.setAdapter(adapter);
        listViewApps.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        loadPreviouslySelectedApps();
    }

    private void loadPreviouslySelectedApps() {
        Set<String> savedApps = prefs.getStringSet("blocked_apps", new HashSet<>());

        for (int i = 0; i < packageNames.size(); i++) {
            if (savedApps.contains(packageNames.get(i))) {
                listViewApps.setItemChecked(i, true);
            }
        }
    }

    private void saveSelectedApps() {
        Set<String> selectedApps = new HashSet<>();

        for (int i = 0; i < packageNames.size(); i++) {
            if (listViewApps.isItemChecked(i)) {
                selectedApps.add(packageNames.get(i));
            }
        }

        prefs.edit().putStringSet("blocked_apps", selectedApps).apply();

        Toast.makeText(this, "Blocked apps saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}