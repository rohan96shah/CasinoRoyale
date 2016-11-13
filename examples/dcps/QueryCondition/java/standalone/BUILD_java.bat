@echo off
echo call BUILD_saj_querycondition_types.bat %*
call BUILD_saj_querycondition_types.bat %*
IF NOT %ERRORLEVEL% == 0 (
ECHO:
ECHO *** Error building BUILD_saj_querycondition_types.bat
ECHO:
GOTO error
)
cd %~dp0
echo call BUILD_saj_querycondition_pub.bat %*
call BUILD_saj_querycondition_pub.bat %*
IF NOT %ERRORLEVEL% == 0 (
ECHO:
ECHO *** Error building BUILD_saj_querycondition_pub.bat
ECHO:
GOTO error
)
cd %~dp0
echo call BUILD_saj_querycondition_sub.bat %*
call BUILD_saj_querycondition_sub.bat %*
IF NOT %ERRORLEVEL% == 0 (
ECHO:
ECHO *** Error building BUILD_saj_querycondition_sub.bat
ECHO:
GOTO error
)
cd %~dp0
GOTO end
:error
ECHO An error occurred, exiting now
:end
