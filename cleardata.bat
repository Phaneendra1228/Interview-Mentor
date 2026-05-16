@echo off
echo ============================================
echo   InterviewMentor - Clear All Data
echo ============================================
echo.
echo This will clear all quiz history, performance
echo data, streaks, and bookmarks.
echo Questions will be kept intact.
echo.

java -cp "target/InterviewMentor-1.0-SNAPSHOT.jar" ClearData.java
echo.
pause
