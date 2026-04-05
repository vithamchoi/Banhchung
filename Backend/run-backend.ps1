# Run Backend with JDK 21

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Using Java version:" -ForegroundColor Green
& "$env:JAVA_HOME\bin\java.exe" -version
Write-Host ""

Write-Host "Building with Maven..." -ForegroundColor Cyan
& mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Starting Backend Server..." -ForegroundColor Green
& "$env:JAVA_HOME\bin\java.exe" -jar target\banhchung-backend-1.0.0.jar
