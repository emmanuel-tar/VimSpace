@echo off
rem Maven wrapper for this workspace (avoids PowerShell -D argument mangling).
set "JAVA_HOME=C:\Program Files\Java\jdk-22"
set "MAVEN_HOME=C:\tools\apache-maven-3.9.9"
"%JAVA_HOME%\bin\java.exe" ^
  -classpath "%MAVEN_HOME%\boot\plexus-classworlds-2.8.0.jar" ^
  "-Dclassworlds.conf=%MAVEN_HOME%\bin\m2.conf" ^
  "-Dmaven.home=%MAVEN_HOME%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_HOME%" ^
  "-Dlibrary.jansi.path=%MAVEN_HOME%\lib\jansi-native" ^
  org.codehaus.plexus.classworlds.launcher.Launcher %*
