@echo off
if "%~1"=="invisible" goto :run
echo CreateObject("Wscript.Shell").Run """" ^& WScript.Arguments(0) ^& """" ^& " invisible", 0, False > "%temp%\run_liquidify.vbs"
cscript //nologo "%temp%\run_liquidify.vbs" "%~f0"
del "%temp%\run_liquidify.vbs"
exit /b

:run
javaw -cp liquidify.jar --add-exports jdk.attach/sun.tools.attach=ALL-UNNAMED com.liquidify.Injector
