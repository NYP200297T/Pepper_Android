## Installing ADB

# Windows
You can download the standalone ADB package from [Google Developers](https://developer.android.com/studio/releases/platform-tools), and choose Windows

To add ADB to path:
- right click `This PC` in File Explorer
- Click on `Properties`
- On the sidebar of Windows Settings, click on `Advanced System Settings`
- Click on `Environment Variables` on the popup (do not change tabs)
- Select Path and click `Edit...` (must be path)
- Copy the filepath of the folder containing `adb.exe` and click `OK`
- Test by opening a new CMD window and type `adb devices`

If `adb devices` doesn't return a command not found, congrats.

# macOS
Install [homebrew](https://brew.sh/), then install adb via `brew install adb`