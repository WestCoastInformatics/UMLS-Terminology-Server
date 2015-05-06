@echo off
REM Copyright 2015 West Coast Informatics, LLC
REM This script is used to load terminology server data for the development
REM environment.  This data can be found in the config/data folder of the
REM distribution.
REM

REM
REM Set environment variables at system level
REM
set UMLS_CODE="C:/workspace/UMLS-Terminology-Server"
set UMLS_DATA="C:/umlsserver/data"
set UMLS_CONFIG="C:/umlsserver/config/config.properties"
set SERVER=false

echo ------------------------------------------------
echo Starting ...%date% %time%
echo ------------------------------------------------
if DEFINED UMLS_CODE (echo UMLS_CODE = %UMLS_CODE%) else (echo UMLS_CODE must be defined
goto trailer)
if DEFINED UMLS_DATA (echo UMLS_DATA= %UMLS_DATA%) else (echo UMLS_DATA must be defined
goto trailer)
if DEFINED UMLS_CONFIG (echo UMLS_CONFIG = %UMLS_CONFIG%) else (echo UMLS_CONFIG must be defined
goto trailer)
if DEFINED SERVER (echo UMLS = %SERVER%) else (echo SERVER must be defined
goto trailer)
set error=0
pause

echo     Run Createdb ...%date% %time%
cd %UMLS_CODE%/admin/db
call mvn install -PCreatedb -Drun.config.umls=%UMLS_CONFIG% 1> mvn.log
IF %ERRORLEVEL% NEQ 0 (set error=1
goto trailer)
del /Q mvn.log

echo     Clear indexes ...%date% %time%
cd %UMLS_CODE%/admin/lucene
call mvn install -PReindex -Drun.config.umls=%UMLS_CONFIG% -Dserver=%SERVER% 1> mvn.log
IF %ERRORLEVEL% NEQ 0 (set error=1
goto trailer)
del /Q mvn.log

echo     Load UMLS ...%date% %time%
cd %UMLS_CODE%/admin/loader
call mvn install -PRRF-umls -Drun.config.umls=%UMLS_CONFIG% -Dserver=%SERVER% -Dterminology=UMLS  -Dinput.dir=%UMLS_DATA%/SCTMSH_2014AB 1> mvn.log
IF %ERRORLEVEL% NEQ 0 (set error=1
goto trailer)
del /Q mvn.log

echo     Add UMLS project ...%date% %time%
cd %UMLS_CODE%/admin/loader
call mvn install -PProject -Drun.config.umls=%UMLS_CONFIG% -Dserver=%SERVER% -Dname="Sample Project" -Ddescription="Sample project." -Dterminology=UMLS -Dversion=latest -Dscope.concepts=138875005 -Dscope.descendants.flag=true -Dadmin.user=admin 1> mvn.log
IF %ERRORLEVEL% NEQ 0 (set error=1
goto trailer)
del /Q mvn.log

echo     Start UMLS editing ...%date% %time%
cd %UMLS_CODE%/admin/release
call mvn install -PStartEditingCycle -Drelease.version=2015AA -Dserver=%SERVER% -Dterminology=UMLS -Dversion=latest -Drun.config.umls=%UMLS_CONFIG% 1> mvn.log
IF %ERRORLEVEL% NEQ 0 (set error=1
goto trailer)
del /Q mvn.log

:trailer
echo ------------------------------------------------
IF %error% NEQ 0 (
echo There were one or more errors.  Please reference the mvn.log file for details. 
set retval=-1
) else (
echo Completed without errors.
set retval=0
)
echo Starting ...%date% %time%
echo ------------------------------------------------

pause


