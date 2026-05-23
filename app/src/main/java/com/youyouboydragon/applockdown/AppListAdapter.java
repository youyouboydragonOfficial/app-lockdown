package com.youyouboydragon.applockdown;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class AppListAdapter extends BaseAdapter {
    interface OnToggleListener {
        void onToggle(AppInfo app, boolean checked);
    }

    private final Context context;
    private final OnToggleListener listener;
    private List<AppInfo> apps = new ArrayList<>();
    private Set<String> checkedPackages;

    AppListAdapter(Context context, Set<String> checkedPackages, OnToggleListener listener) {
        this.context = context;
        this.checkedPackages = checkedPackages;
        this.listener = listener;
    }

    void submit(List<AppInfo> apps, Set<String> checkedPackages) {
        this.apps = apps;
        this.checkedPackages = checkedPackages;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public AppInfo getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Row row;
        if (convertView == null) {
            row = createRow();
            convertView = row.root;
            convertView.setTag(row);
        } else {
            row = (Row) convertView.getTag();
        }

        AppInfo app = getItem(position);
        row.icon.setImageDrawable(app.icon);
        row.title.setText(app.label);
        row.subtitle.setText(app.packageName);
        row.checkBox.setOnCheckedChangeListener(null);
        row.checkBox.setChecked(checkedPackages.contains(app.packageName));
        row.checkBox.setOnCheckedChangeListener((button, checked) -> listener.onToggle(app, checked));
        convertView.setOnClickListener(v -> row.checkBox.setChecked(!row.checkBox.isChecked()));
        return convertView;
    }

    private Row createRow() {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        int padding = dp(14);
        root.setPadding(padding, dp(10), padding, dp(10));
        root.setMinimumHeight(dp(72));

        ImageView icon = new ImageView(context);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        root.addView(icon, iconParams);

        LinearLayout labels = new LinearLayout(context);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(dp(14), 0, dp(10), 0);
        LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        root.addView(labels, labelsParams);

        TextView title = new TextView(context);
        title.setTextSize(16f);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(0xFF172033);
        title.setSingleLine(true);
        labels.addView(title);

        TextView subtitle = new TextView(context);
        subtitle.setTextSize(12f);
        subtitle.setTextColor(0xFF6A7280);
        subtitle.setSingleLine(true);
        labels.addView(subtitle);

        CheckBox checkBox = new CheckBox(context);
        root.addView(checkBox, new LinearLayout.LayoutParams(dp(48), dp(48)));
        return new Row(root, icon, title, subtitle, checkBox);
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private static final class Row {
        final LinearLayout root;
        final ImageView icon;
        final TextView title;
        final TextView subtitle;
        final CheckBox checkBox;

        Row(LinearLayout root, ImageView icon, TextView title, TextView subtitle, CheckBox checkBox) {
            this.root = root;
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.checkBox = checkBox;
        }
    }
}
