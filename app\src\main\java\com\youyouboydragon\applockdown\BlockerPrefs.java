package com.youyouboydragon.applockdown;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

final class BlockerPrefs {
    private static final String FILE = "blocker_prefs";
    private static final String BLOCKED = "blocked_packages";
    private static final String EXCLUDED = "excluded_packages";
    private static final String TEMP_PREFIX = "temp_allow_until_";

    private BlockerPrefs() {
    }

    static Set<String> getBlocked(Context context) {
        return getSet(context, BLOCKED);
    }

    static Set<String> getExcluded(Context context) {
        return getSet(context, EXCLUDED);
    }

    static void setBlocked(Context context, String packageName, boolean blocked) {
        updateSet(context, BLOCKED, packageName, blocked);
    }

    static void setExcluded(Context context, String packageName, boolean excluded) {
        updateSet(context, EXCLUDED, packageName, excluded);
    }

    static boolean isBlocked(Context context, String packageName) {
        if (packageName == null || packageName.equals(context.getPackageName())) {
            return false;
        }
        if (getExcluded(context).contains(packageName)) {
            return false;
        }
        if (isTemporarilyAllowed(context, packageName)) {
            return false;
        }
        return getBlocked(context).contains(packageName);
    }

    static void allowForMinutes(Context context, String packageName, int minutes) {
        long until = System.currentTimeMillis() + minutes * 60_000L;
        prefs(context).edit().putLong(TEMP_PREFIX + packageName, until).apply();
    }

    private static boolean isTemporarilyAllowed(Context context, String packageName) {
        long until = prefs(context).getLong(TEMP_PREFIX + packageName, 0L);
        return until > System.currentTimeMillis();
    }

    private static Set<String> getSet(Context context, String key) {
        return new HashSet<>(prefs(context).getStringSet(key, new HashSet<>()));
    }

    private static void updateSet(Context context, String key, String packageName, boolean present) {
        Set<String> values = getSet(context, key);
        if (present) {
            values.add(packageName);
        } else {
            values.remove(packageName);
        }
        prefs(context).edit().putStringSet(key, values).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }
}
