package com.KeenOx.paylock;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectionActivity extends AppCompatActivity {

    ListView listViewApps;
    Button btnSaveApps;

    ArrayList<AppItem> appItems = new ArrayList<>();

    SharedPreferences prefs;
    AppListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);

        listViewApps = findViewById(R.id.listViewApps);
        btnSaveApps = findViewById(R.id.btnSaveApps);

        prefs = getSharedPreferences("PayLockPrefs", MODE_PRIVATE);

        loadInstalledApps();

        listViewApps.setOnItemClickListener((parent, view, position, id) -> adapter.notifyDataSetChanged());

        btnSaveApps.setOnClickListener(v -> saveSelectedApps());
    }

    private void loadInstalledApps() {
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo app : apps) {
            String packageName = app.packageName;

            if (packageManager.getLaunchIntentForPackage(packageName) != null
                    && !packageName.equals(getPackageName())) {

                String appName = packageManager.getApplicationLabel(app).toString();
                Drawable appIcon = packageManager.getApplicationIcon(app);
                appItems.add(new AppItem(appName, packageName, appIcon));
            }
        }

        Collator collator = Collator.getInstance();
        Collections.sort(appItems, Comparator.comparing(AppItem::getAppName, collator));

        adapter = new AppListAdapter(this, appItems);
        listViewApps.setAdapter(adapter);
        listViewApps.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        loadPreviouslySelectedApps();
        adapter.notifyDataSetChanged();
    }

    private void loadPreviouslySelectedApps() {
        Set<String> savedApps = prefs.getStringSet("blocked_apps", new HashSet<>());

        for (int i = 0; i < appItems.size(); i++) {
            if (savedApps.contains(appItems.get(i).getPackageName())) {
                listViewApps.setItemChecked(i, true);
            }
        }
    }

    private void saveSelectedApps() {
        Set<String> selectedApps = new HashSet<>();

        for (int i = 0; i < appItems.size(); i++) {
            if (listViewApps.isItemChecked(i)) {
                selectedApps.add(appItems.get(i).getPackageName());
            }
        }

        prefs.edit().putStringSet("blocked_apps", selectedApps).apply();

        Toast.makeText(this, "Blocked apps saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}