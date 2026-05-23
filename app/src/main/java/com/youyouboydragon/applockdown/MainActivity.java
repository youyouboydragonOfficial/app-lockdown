package com.youyouboydragon.applockdown;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends android.app.Activity {
    private enum Mode {
        BLOCKED, EXCLUDED
    }

    private final List<AppInfo> allApps = new ArrayList<>();
    private AppListAdapter adapter;
    private EditText searchInput;
    private TextView countText;
    private Mode mode = Mode.BLOCKED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
        loadApps();
        refreshList();
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF6F8FC);
        root.setPadding(dp(18), dp(18), dp(18), 0);

        TextView title = new TextView(this);
        title.setText("App Lockdown");
        title.setTextSize(28f);
        title.setTextColor(0xFF172033);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Block selected apps, including privileged full-stop when available");
        subtitle.setTextSize(14f);
        subtitle.setTextColor(0xFF5E6675);
        subtitle.setPadding(0, dp(4), 0, dp(14));
        root.addView(subtitle);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setGravity(Gravity.CENTER);
        root.addView(actions, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button accessibility = actionButton("Accessibility");
        accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        actions.addView(accessibility, new LinearLayout.LayoutParams(0, dp(46), 1f));

        Button deviceAdmin = actionButton("Device admin");
        deviceAdmin.setOnClickListener(v -> openDeviceAdmin());
        LinearLayout.LayoutParams secondParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
        secondParams.setMargins(dp(10), 0, 0, 0);
        actions.addView(deviceAdmin, secondParams);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, dp(14), 0, dp(8));
        root.addView(tabs);

        Button blockedTab = actionButton("Block list");
        Button excludedTab = actionButton("Exclusion list");
        blockedTab.setOnClickListener(v -> {
            mode = Mode.BLOCKED;
            refreshList();
        });
        excludedTab.setOnClickListener(v -> {
            mode = Mode.EXCLUDED;
            refreshList();
        });
        tabs.addView(blockedTab, new LinearLayout.LayoutParams(0, dp(44), 1f));
        LinearLayout.LayoutParams excludedParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        excludedParams.setMargins(dp(10), 0, 0, 0);
        tabs.addView(excludedTab, excludedParams);

        searchInput = new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setHint("Search app name or package");
        searchInput.setTextSize(15f);
        searchInput.setPadding(dp(12), 0, dp(12), 0);
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshList(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
        root.addView(searchInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        countText = new TextView(this);
        countText.setTextSize(13f);
        countText.setTextColor(0xFF5E6675);
        countText.setPadding(0, dp(10), 0, dp(8));
        root.addView(countText);

        adapter = new AppListAdapter(this, selectedSet(), (app, checked) -> {
            if (mode == Mode.BLOCKED) {
                BlockerPrefs.setBlocked(this, app.packageName, checked);
                if (checked) {
                    StrongStopper.stopOrSuspend(this, app.packageName);
                } else {
                    StrongStopper.resumePackage(this, app.packageName);
                }
            } else {
                BlockerPrefs.setExcluded(this, app.packageName, checked);
                if (checked) {
                    StrongStopper.resumePackage(this, app.packageName);
                }
            }
            refreshList();
        });

        ListView list = new ListView(this);
        list.setDividerHeight(1);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        Button stopNow = actionButton("Stop blocked apps now");
        stopNow.setOnClickListener(v -> stopBlockedAppsNow());
        root.addView(stopNow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));

        return root;
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent launchIntent = new Intent(Intent.ACTION_MAIN, null);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> launchables = pm.queryIntentActivities(launchIntent, 0);
        Collator collator = Collator.getInstance(Locale.getDefault());
        allApps.clear();

        for (android.content.pm.ResolveInfo info : launchables) {
            ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
            String packageName = applicationInfo.packageName;
            if (packageName.equals(getPackageName())) {
                continue;
            }
            String label = info.loadLabel(pm).toString();
            allApps.add(new AppInfo(label, packageName, info.loadIcon(pm)));
        }

        allApps.sort(Comparator.comparing(app -> app.label, collator));
    }

    private void refreshList() {
        if (adapter == null) {
            return;
        }
        String query = searchInput == null ? "" : searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<AppInfo> filtered = new ArrayList<>();
        for (AppInfo app : allApps) {
            String haystack = (app.label + " " + app.packageName).toLowerCase(Locale.ROOT);
            if (query.isEmpty() || haystack.contains(query)) {
                filtered.add(app);
            }
        }
        Set<String> selected = selectedSet();
        adapter.submit(filtered, selected);
        String label = mode == Mode.BLOCKED ? "Blocked" : "Excluded";
        String owner = StrongStopper.canUseDeviceOwner(this) ? "owner enabled" : "owner not enabled";
        countText.setText(label + ": " + selected.size() + " / Visible: " + filtered.size() + " / " + owner);
    }

    private Set<String> selectedSet() {
        return mode == Mode.BLOCKED ? BlockerPrefs.getBlocked(this) : BlockerPrefs.getExcluded(this);
    }

    private void stopBlockedAppsNow() {
        Set<String> blocked = BlockerPrefs.getBlocked(this);
        if (blocked.isEmpty()) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
            return;
        }
        int privilegedStops = 0;
        for (String packageName : blocked) {
            if (BlockerPrefs.getExcluded(this).contains(packageName)) {
                continue;
            }
            StrongStopper.StopResult result = StrongStopper.stopOrSuspend(this, packageName);
            if (result == StrongStopper.StopResult.SUSPENDED_BY_DEVICE_OWNER || result == StrongStopper.StopResult.FORCE_STOPPED_BY_ROOT) {
                privilegedStops++;
            }
        }
        if (privilegedStops == 0) {
            String packageName = blocked.iterator().next();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        } else {
            refreshList();
        }
    }

    private void openDeviceAdmin() {
        ComponentName admin = new ComponentName(this, LockdownDeviceAdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Device owner or profile owner mode can suspend blocked packages.");
        startActivity(intent);
    }

    private Button actionButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(13f);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundColor(0xFF0F5FFF);
        return button;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
