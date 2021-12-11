@echo off

::set MYBERRY_HOME=D:\app

if not exist "%MYBERRY_HOME%\bin\runserver.cmd" echo Please set the MYBERRY_HOME variable in your environment! & EXIT /B 1

call "%MYBERRY_HOME%\bin\runserver.cmd" org.myberry.server.ServerStartup %*