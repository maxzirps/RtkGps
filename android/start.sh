#!/bin/sh

./gradlew :app:installDebug --daemon
adb shell am start -n 'gpsplus.rtkgps/gpsplus.rtkgps.MainActivity' -a android.intent.action.MAIN -c android.intent.category.LAUNCHER