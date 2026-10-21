@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-22"
cd /d "C:\Users\KAZEEM\IdeaProjects\VimSpace"
start "VimSpace FrontRetail" "C:\Program Files\Java\jdk-22\bin\javaw.exe" -cp "frontretail-client\target\classes;frontretail-client\target\dependency\*" com.vimspace.foh.FrontRetailApp
