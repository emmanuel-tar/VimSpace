$cp = @()
$jars = Get-ChildItem -Path "C:\Users\KAZEEM\.m2\repository" -Recurse -Filter "*.jar"
Write-Host "Found $($jars.Count) jars"
$jars | ForEach-Object { $cp += $_.FullName }
$cp += "C:\Users\KAZEEM\IdeaProjects\VimSpace\backend-api\target\classes"
$cp += "C:\Users\KAZEEM\IdeaProjects\VimSpace\shared-contracts\target\classes"
$fullCp = ($cp -join ';')
Write-Host "Classpath entries: $($cp.Count)"
Write-Host "CP length: $($fullCp.Length)"
# Verify spring-boot jar is in classpath
Write-Host "Contains spring-boot: $($fullCp -like '*spring-boot*')"
"C:\Program Files\Java\jdk-22\bin\java" -cp "$fullCp" com.vimspace.api.VimspaceApiApplication 2>&1 | Tee-Object -FilePath "C:\Users\KAZEEM\IdeaProjects\VimSpace\backend.log"
