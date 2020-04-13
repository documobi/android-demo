# android-demo
Android BrandActif demo

## Description
Android native app for the BrandActif scan demo written in Java. It is meant for demoing the capabilities of the BrandActif API from a native iOS app.

## Features
* Image scan
* Radio scan
* TV scan
* Video scan
* Settings page (TBD)

## Installation
Install the app with Android Studio 3.6 and above.

## Adding client customization
1. Assume the new client is called "Acme".
2. Add a new subfolder named "acme" in "/app/src/", i.e. "/app/src/acme".
3. Create a res subfolder in "app/src/acme".
4. Create the app icon (ic_launcher.png) and logo (logo.png) in the mipmap folders in /app/src/acme/res.
5. Copy the colors.xml and strings.xml files from other client subfolders into /app/src/acme/res/values.
6. Adjust the app color scheme in colors.xml
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="colorButton">#002561</color>
    <color name="colorButtonTitle">#ffffff</color>
    <color name="colorBackground">#ffffff</color>
</resources>
```
7. Configurate the app settings in strings.xml
```
<resources>
    <string name="app_name">Acme</string>
    <string name="tv_uuid">b5823bd3-aaf3-4031-a6a3-a7331c835e52</string>
    <string name="radio_uuid">b5823bd3-aaf3-4031-a6a3-a7331c835e52</string>
    <string name="video_uuid">b5823bd3-aaf3-4031-a6a3-a7331c835e52</string>
    <string name="default_scan_type">scanner</string>
    <string name="settings_enabled">true</string>
</resources>
```
8. In the application build.gradle file, add a new product flavor called "acme", e.g.

```
acme {
    applicationIdSuffix ".acme"
}
```
