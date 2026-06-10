[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$classesDir = Join-Path $projectRoot "build\package-classes"
$inputDir = Join-Path $projectRoot "build\package-input"
$distDir = Join-Path $projectRoot "dist"
$appDir = Join-Path $distDir "SmartLibrary"
$jarPath = Join-Path $inputDir "SmartLibrary.jar"

foreach ($command in @("javac", "jar", "jpackage")) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "'$command' was not found. Install JDK 17 or newer and add its bin directory to PATH."
    }
}

Remove-Item -LiteralPath $classesDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $inputDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $appDir -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $classesDir, $inputDir -Force | Out-Null

$sources = Get-ChildItem -LiteralPath (Join-Path $projectRoot "src") -Recurse -Filter "*.java" |
    ForEach-Object { $_.FullName }

if (-not $sources) {
    throw "No Java source files were found under the src directory."
}

& javac --release 17 -encoding UTF-8 -d $classesDir $sources
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed."
}

& jar --create --file $jarPath --main-class smartlibrary.Main -C $classesDir .
if ($LASTEXITCODE -ne 0) {
    throw "JAR creation failed."
}

& jpackage `
    --type app-image `
    --name SmartLibrary `
    --input $inputDir `
    --dest $distDir `
    --main-jar "SmartLibrary.jar" `
    --main-class smartlibrary.Main `
    --win-console

if ($LASTEXITCODE -ne 0) {
    throw "Windows application packaging failed."
}

$exePath = Join-Path $appDir "SmartLibrary.exe"
Write-Host ""
Write-Host "Created: $exePath"
Write-Host "Keep the entire SmartLibrary folder together when distributing the application."
