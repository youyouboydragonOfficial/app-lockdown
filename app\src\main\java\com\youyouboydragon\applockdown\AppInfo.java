package com.youyouboydragon.applockdown;

import android.graphics.drawable.Drawable;

final class AppInfo {
    final String label;
    final String packageName;
    final Drawable icon;

    AppInfo(String label, String packageName, Drawable icon) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
    }
}
