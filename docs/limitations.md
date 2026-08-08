# Limitations

Android and OEM window policies differ. Normal application overlays may not cover every system area. On Android 12 and later, touch-through application overlays are limited to the system's maximum obscuring opacity (normally 80%); higher requested depths require the trusted accessibility overlay. The optional accessibility overlay can improve coverage after the user enables it manually, but cannot guarantee identical behavior across all devices.

Removing Dim Veil from Recents intentionally stops the overlay. A device reboot does not restore it automatically.
