@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-22"
cd /d "C:\Users\KAZEEM\IdeaProjects\VimSpace"
start "VimSpace VimManager" "C:\Program Files\Java\jdk-22\bin\javaw.exe" -cp "vim-manager-client\target\classes;vim-manager-client\target\dependency\*" com.vimspace.boh.VimManagerApp
