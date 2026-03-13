rm release -Recurse -Force
jpackage --type app-image --input target --name jhr --main-jar jhr.jar --main-class Main --win-console
Rename-Item -Path "jhr" -NewName "release"
