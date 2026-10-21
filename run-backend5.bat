@echo off
REM Set Java 22
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%PATH%

echo ==========================================
echo Running VimSpace Backend API
echo ==========================================
echo.

cd /d C:\Users\KAZEEM\IdeaProjects\VimSpace\backend-api

echo Building with Maven...
echo.

REM Run Maven to package
"C:\Tools\apache-maven-3.9.9\bin\mvn.cmd" package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful. Now running...
    echo.
    "C:\Program Files\Java\jdk-22\bin\java.exe" -jar target\backend-api-1.0.0-SNAPSHOT.jar
) else (
    echo.
    echo Build failed!
    echo.
)
