@echo off
REM Run Backend with JDK 17

set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java version:
java -version
echo.

echo Building with Maven...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Starting Backend Server...
java -jar target\banhchung-backend-1.0.0.jar

pause
