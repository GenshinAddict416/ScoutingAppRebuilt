@echo off
cd /d "%USERPROFILE%\ScoutingCSV
adb devices
adb pull /sdcard/Android/data/com.sotabots.sotabotsscouting/files/scouting_export_tablet1.csv