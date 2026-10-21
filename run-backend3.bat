@echo off
REM Set Java 22
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%PATH%

echo ==========================================
echo Running VimSpace Backend API
echo ==========================================
echo.

cd /d C:\Users\KAZEEM\IdeaProjects\VimSpace\backend-api

echo Building and running with Maven...
echo.

REM Run Maven to package and run
"C:\Tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run -DskipTests

echo.
echo Done.
