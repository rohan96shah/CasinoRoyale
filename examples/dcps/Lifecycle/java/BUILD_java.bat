@echo off
echo call standalone/BUILD_saj_lifecycle_types.bat %*
call standalone/BUILD_saj_lifecycle_types.bat %*
IF NOT %ERRORLEVEL% == 0 (
ECHO:
ECHO *** Error building standalone/BUILD_saj_lifecycle_types.bat
ECHO:
GOTO error
)
cd %~dp0
echo call standalone/BUILD_saj_lifecycle_pub.bat %*
call standalone/BUILD_saj_lifecycle_pub.bat %*
IF NOT %ERRORLEVEL% == 0 (
ECHO:
ECHO *** Error building standalone/BUILD_saj_lifecycle_pub.bat
ECHO:
GOTO error
)
cd %~dp0
echo call standalone/BUILD_saj_lifecycle_sub.bat %*
call standalone/BUILD_saj_lifecycle_sub.bat %*
IF NOT %ERRORLEVEL% == 0 (
ECHO:
ECHO *** Error building standalone/BUILD_saj_lifecycle_sub.bat
ECHO:
GOTO error
)
cd %~dp0
GOTO end
:error
ECHO An error occurred, exiting now
:end
