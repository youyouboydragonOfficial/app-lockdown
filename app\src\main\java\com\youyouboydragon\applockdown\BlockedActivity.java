package com.youyouboydragon.applockdown;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BlockedActivity extends android.app.Activity {
    static final String EXTRA_PACKAGE_NAME = "package_name";
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageName = getIntent().getStringExtra(EXTRA_PACKAGE_NAME);
        setContentView(buildUi());
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(28), dp(28), dp(28), dp(28));
        root.setBackgroundColor(0xFF172033);

        TextView mark = new TextView(this);
        mark.setText("LOCKED");
        mark.setTextColor(0xFF8FB3FF);
        mark.setTextSize(13f);
        mark.setGravity(Gravity.CENTER);
        root.addView(mark);

        TextView title = new TextView(this);
        title.setText(getAppLabel() + " is blocked");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(26f);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(16), 0, dp(10));
        root.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView body = new TextView(this);
        body.setText("This app is on the block list. Device owner mode or root is required for full background force-stop.");
        body.setTextColor(0xFFD5DCEC);
        body.setTextSize(15f);
        body.setGravity(Gravity.CENTER);
        body.setPadding(0, 0, 0, dp(24));
        root.addView(body);

        Button home = button("Back to Home", 0xFF0F5FFF);
        home.setOnClickListener(v -> goHome());
        root.addView(home, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        Button pause = button("Allow for 5 minutes", 0xFF2B3448);
        pause.setOnClickListener(v -> {
            BlockerPrefs.allowForMinutes(this, packageName, 5);
            finish();
        });
        LinearLayout.LayoutParams pauseParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        pauseParams.setMargins(0, dp(12), 0, 0);
        root.addView(pause, pauseParams);

        return root;
    }

    private String getAppLabel() {
        if (packageName == null) {
            return "This app";
        }
        try {
            PackageManager pm = getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private void goHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private Button button(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(15f);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundColor(color);
        return button;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
