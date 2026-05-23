# App Lockdown

App Lockdown is an Android app for selecting apps from the phone and blocking or stopping them with a block list and an exclusion list.

## Features

- Select installed launchable apps from the phone.
- Maintain a block list.
- Maintain an exclusion list that overrides the block list.
- Block foreground launches with an Accessibility Service.
- Attempt full app stopping in privileged modes:
  - Device owner or profile owner: suspends blocked packages with `DevicePolicyManager.setPackagesSuspended`.
  - Rooted device: runs `am force-stop <package>`.
- Open Android app settings when privileged stopping is not available.

## Important Android limitation

Android does not allow a normal third-party app to force stop other apps in the background. Full background stop requires one of these:

- The app is installed as device owner/profile owner.
- The device is rooted and grants `su`.
- The app is a privileged system app signed for the device image.

Without those privileges, App Lockdown still blocks use by detecting a blocked app when it opens and immediately showing the lock screen.

## Device owner setup for testing

On a test device after installing the app:

```sh
adb shell dpm set-device-owner com.youyouboydragon.applockdown/.LockdownDeviceAdminReceiver
```

Device owner setup may require a fresh or managed test device. Use a non-personal test device.

## Build

Open this folder in Android Studio and run the `app` configuration.

Enable:

- Settings -> Accessibility -> App Lockdown blocker
- Device owner/profile owner mode, or root, for full background stopping
