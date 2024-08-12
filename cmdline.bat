@echo off

if (%1)==() GOTO Call

set mem=-Xmx512m
set dummy=%1
if NOT %dummy:~0,4% == -Xmx GOTO Call
set mem=%dummy%

:Call

java -cp %~dps0lib\procope-1.2.jar %mem% procope.userinterface.cmdline.CommandLine %* 