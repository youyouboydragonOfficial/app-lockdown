package com.youyouboydragon.applockdown;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import java.io.DataOutputStream;
import java.util.regex.Pattern;

final class StrongStopper {
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+");

    private StrongStopper() {
    }

    static StopResult stopOrSuspend(Context context, String packageName) {
        if (!isValidPackage(packageName) || packageName.equals(context.getPackageName())) {
            return StopResult.NOT_ALLOWED;
        }
        if (suspendWithDeviceOwner(context, packageName, true)) {
            return StopResult.SUSPENDED_BY_DEVICE_OWNER;
        }
        if (forceStopWithRoot(packageName)) {
            return StopResult.FORCE_STOPPED_BY_ROOT;
        }
        return StopResult.NEEDS_PRIVILEGED_ACCESS;
    }

    static boolean resumePackage(Context context, String packageName) {
        return isValidPackage(packageName) && suspendWithDeviceOwner(context, packageName, false);
    }

    static boolean canUseDeviceOwner(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin = admin(context);
        return dpm != null
                && dpm.isAdminActive(admin)
                && (dpm.isDeviceOwnerApp(context.getPackageName()) || dpm.isProfileOwnerApp(context.getPackageName()));
    }

    private static boolean suspendWithDeviceOwner(Context context, String packageName, boolean suspended) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm == null || !canUseDeviceOwner(context)) {
            return false;
        }
        try {
            String[] failed = dpm.setPackagesSuspended(admin(context), new String[]{packageName}, suspended);
            return failed == null || failed.length == 0;
        } catch (SecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean forceStopWithRoot(String packageName) {
        if (!isValidPackage(packageName)) {
            return false;
        }
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            DataOutputStream output = new DataOutputStream(process.getOutputStream());
            output.writeBytes("am force-stop " + packageName + "\n");
            output.writeBytes("exit\n");
            output.flush();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static ComponentName admin(Context context) {
        return new ComponentName(context, LockdownDeviceAdminReceiver.class);
    }

    private static boolean isValidPackage(String packageName) {
        return packageName != null && PACKAGE_PATTERN.matcher(packageName).matches();
    }

    enum StopResult {
        SUSPENDED_BY_DEVICE_OWNER,
        FORCE_STOPPED_BY_ROOT,
        NEEDS_PRIVILEGED_ACCESS,
        NOT_ALLOWED
    }
}
