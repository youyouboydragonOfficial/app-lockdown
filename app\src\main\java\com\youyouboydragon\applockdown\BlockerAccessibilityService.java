package com.youyouboydragon.applockdown;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

public class BlockerAccessibilityService extends AccessibilityService {
    private String lastPackage = "";
    private long lastBlockedAt = 0L;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        CharSequence packageText = event.getPackageName();
        if (packageText == null) {
            return;
        }
        String packageName = packageText.toString();
        long now = System.currentTimeMillis();
        if (packageName.equals(lastPackage) && now - lastBlockedAt < 900L) {
            return;
        }
        if (BlockerPrefs.isBlocked(this, packageName)) {
            lastPackage = packageName;
            lastBlockedAt = now;
            StrongStopper.stopOrSuspend(this, packageName);
            Intent intent = new Intent(this, BlockedActivity.class);
            intent.putExtra(BlockedActivity.EXTRA_PACKAGE_NAME, packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    @Override
    public void onInterrupt() {
    }
}
