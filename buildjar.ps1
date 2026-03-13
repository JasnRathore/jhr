$BUILD_DIR = "target"

# Create the build directory if it doesn't exist
New-Item -ItemType Directory -Path $BUILD_DIR -Force | Out-Null

# Compile all Java files with UTF-8 encoding
javac --release 17 -encoding UTF-8 -d $BUILD_DIR *.java

# Check if compilation was successful
if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful. Classes moved to $BUILD_DIR"
    
    # Create executable JAR with Main class
    jar cfe jhr.jar Main -C $BUILD_DIR .
    
    # Clean up class files
    Remove-Item $BUILD_DIR\*.class -Force -ErrorAction SilentlyContinue
    
    # Move JAR to build directory (overwrite if exists)
    Move-Item jhr.jar "$BUILD_DIR\jhr.jar" -Force
    
    Write-Host "JAR created successfully: $BUILD_DIR\jhr.jar"
} else {
    Write-Host "Compilation failed."
    exit 1
}
