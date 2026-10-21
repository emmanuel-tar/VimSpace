@echo off
setlocal enabledelayedexpansion
set CP=
for /r "C:\Users\KAZEEM\.m2\repository" %%j in (*.jar) do set "CP=!CP!%%j;"
set "CP=%CP%C:\Users\KAZEEM\IdeaProjects\VimSpace\backend-api\target\classes;C:\Users\KAZEEM\IdeaProjects\VimSpace\shared-contracts\target\classes"
"C:\Program Files\Java\jdk-22\bin\java" -cp "%CP%" com.vimspace.api.VimspaceApiApplication > "C:\Users\KAZEEM\IdeaProjects\VimSpace\backend.log" 2>&1
