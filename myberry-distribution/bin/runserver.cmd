@echo off

rem ===========================================================================================
rem Java Environment Setting
rem ===========================================================================================

if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"

setlocal enabledelayedexpansion

set BASE_DIR=%~dp0
set BASE_DIR=%BASE_DIR:~0,-1%
for %%d in (%BASE_DIR%) do set BASE_DIR=%%~dpd

rem ===========================================================================================
rem  JVM Configuration
rem ===========================================================================================

set "JAVA_OPT=%JAVA_OPT% -server -Xmx128m -Xms128m -XX:MetaspaceSize=64m -XX:MaxMetaspaceSize=64m"
set "JAVA_OPT=%JAVA_OPT% -cp .;%BASE_DIR%conf;%BASE_DIR%lib\*"

"%JAVA%" %JAVA_OPT% %*