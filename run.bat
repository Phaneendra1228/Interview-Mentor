@echo off
echo ============================================
echo   InterviewMentor - Technical Interview
echo   Preparation Trainer
echo ============================================
echo.

:: Set JAVA_HOME
set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"
set "MVN_CMD=%~dp0.maven\apache-maven-3.9.9\bin\mvn.cmd"

:: Check if Java is available
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH.
    pause
    exit /b 1
)

:: Build Backend if JAR doesn't exist
IF NOT EXIST "%~dp0backend\target\backend-0.0.1-SNAPSHOT.jar" (
    echo [INFO] Building backend API for the first time...
    cd "%~dp0backend"
    call "%~dp0backend\mvnw.cmd" clean package -DskipTests -q
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Backend Build failed!
        pause
        exit /b 1
    )
    cd "%~dp0"
    echo [INFO] Backend Build successful!
    echo.
)

:: Build Frontend if JAR doesn't exist
IF NOT EXIST "%~dp0target\InterviewMentor-1.0-SNAPSHOT.jar" (
    echo [INFO] Building project for the first time...
    call "%MVN_CMD%" clean package -DskipTests -q
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed!
        pause
        exit /b 1
    )
    echo [INFO] Build successful!
    echo.
)

echo [INFO] Launching InterviewMentor Backend API (Port 8080)...
start "InterviewMentor Backend API" cmd /c "java -jar %~dp0backend\target\backend-0.0.1-SNAPSHOT.jar"

echo [INFO] Launching InterviewMentor Desktop Client...
java -jar "%~dp0target\InterviewMentor-1.0-SNAPSHOT.jar"

echo [INFO] Closing Application...
taskkill /FI "WindowTitle eq InterviewMentor Backend API*" /T /F >nul 2>nul
pause
