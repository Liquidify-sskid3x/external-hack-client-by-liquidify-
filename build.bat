@echo off
echo Building Liquidify...
rd /s /q out 2>nul
javac --release 21 -d out -cp . src\com\liquidify\*.java src\com\liquidify\sdk\*.java src\com\liquidify\modules\*.java src\com\liquidify\gui\*.java
if %errorlevel% neq 0 (
    echo [!] Compilation failed!
    pause
    exit /b 1
)
del liquidify.jar 2>nul
jar cvfm liquidify.jar META-INF\MANIFEST.MF -C out .
if %errorlevel% neq 0 (
    echo [!] JAR creation failed - is the file locked? Close Minecraft and try again.
    pause
    exit /b 1
)
attrib +h liquidify.jar
attrib +h inject.bat
echo [+] Build successful: liquidify.jar
pause
