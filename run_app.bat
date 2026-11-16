@echo off
echo ====================================================
echo   AURA FINANCE - 1-CLICK BUILD AND DEPLOY
echo ====================================================

set "JAVA_HOME=%TEMP%\jdk-17\jdk17.0.20_12"
set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
set "ANDROID_SDK_ROOT=%LOCALAPPDATA%\Android\Sdk"
set "PATH=%TEMP%\gradle-8.2\gradle-8.2\bin;%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%PATH%"

echo.
echo [1/4] Syncing index.html and offline_styles.css to Android build folder...
copy /Y "A:\AI_Project_work\money_tracking\index.html" "A:\AI_Project_work\money_tracking\android\app\src\main\assets\index.html"
copy /Y "A:\AI_Project_work\money_tracking\offline_styles.css" "A:\AI_Project_work\money_tracking\android\app\src\main\assets\offline_styles.css"

echo.
echo [2/4] Compiling Android APK using Gradle 8.2 and JDK 17...
cd /d "A:\AI_Project_work\money_tracking\android"
call gradle assembleDebug --no-daemon

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Gradle build failed! Check logs above.
    exit /b %ERRORLEVEL%
)

echo.
echo [3/4] Checking connected Android simulator or device...
adb devices

echo.
echo [4/4] Installing APK and launching Aura Finance...
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
adb shell pm grant com.aura.finance android.permission.READ_SMS
adb shell pm grant com.aura.finance android.permission.RECEIVE_SMS
adb shell am start -n com.aura.finance/.MainActivity

echo.
echo ====================================================
echo   SUCCESS! Aura Finance is running on your simulator.
echo ====================================================
