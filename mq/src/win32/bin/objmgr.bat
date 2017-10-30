@echo off
REM
REM  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
REM 
REM  Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
REM 
REM  The contents of this file are subject to the terms of either the GNU
REM  General Public License Version 2 only ("GPL") or the Common Development
REM  and Distribution License("CDDL") (collectively, the "License").  You
REM  may not use this file except in compliance with the License.  You can
REM  obtain a copy of the License at
REM  https://oss.oracle.com/licenses/CDDL+GPL-1.1
REM  or LICENSE.txt.  See the License for the specific
REM  language governing permissions and limitations under the License.
REM 
REM  When distributing the software, include this License Header Notice in each
REM  file and include the License file at LICENSE.txt.
REM 
REM  GPL Classpath Exception:
REM  Oracle designates this particular file as subject to the "Classpath"
REM  exception as provided by Oracle in the GPL Version 2 section of the License
REM  file that accompanied this code.
REM 
REM  Modifications:
REM  If applicable, add the following below the License Header, with the fields
REM  enclosed by brackets [] replaced by your own identifying information:
REM  "Portions Copyright [year] [name of copyright owner]"
REM 
REM  Contributor(s):
REM  If you wish your version of this file to be governed by only the CDDL or
REM  only the GPL Version 2, indicate your decision by adding "[Contributor]
REM  elects to include this software in this distribution under the [CDDL or GPL
REM  Version 2] license."  If you don't indicate a single choice of license, a
REM  recipient has the option to distribute your version of this file under
REM  either the CDDL, the GPL Version 2 or to extend the choice of license to
REM  its licensees as provided above.  However, if you add GPL Version 2 code
REM  and therefore, elected the GPL Version 2 license, then the option applies
REM  only if the new code is made subject to such option by the copyright
REM  holder.
REM

setlocal ENABLEDELAYEDEXPANSION
set _mainclass=com.sun.messaging.jmq.admin.apps.objmgr.ObjMgr

for /f %%i in ("%0") do set curdir=%%~dpi
set IMQ_HOME=%curdir%\..\
set DEPENDLIBS=..\..\..\share\opt\depend
set IMQ_EXTERNAL=%IMQ_HOME%\lib\ext
set JVM_ARGS=
set BKR_ARGS=
set args_list=%*

:processArgs
FOR /f "tokens=1,2* delims= " %%a IN ("%args_list%") DO (

  set arg=%%a
  if "%%a" == "-jmqhome"	( set IMQ_HOME=%%b&set args_list=%%c&goto :processArgs )
  if "%%a" == "-imqhome"	( set IMQ_HOME=%%b&set args_list=%%c&goto :processArgs )
  if "%%a" == "-javahome"	( set JAVA_HOME=%%b&set args_list=%%c&goto :processArgs )
  if "%%a" == "-jrehome"	( set JAVA_HOME=%%b&set args_list=%%c&goto :processArgs )
  if "%%a" == "-varhome"	( set JVM_ARGS=%JVM_ARGS% -Dimq.varhome=%%b&set args_list=%%c&goto :processArgs )
  if "%%a" == "-imqvarhome"	( set JVM_ARGS=%JVM_ARGS% -Dimq.varhome=%%b&set args_list=%%c&goto :processArgs )
  if "%%a" == "-vmargs"		( set JVM_ARGS=%JVM_ARGS% %%b&set args_list=%%c&goto :processArgs )
  if "!arg:~0,2!" == "-D"	( set JVM_ARGS=%JVM_ARGS% %%a&set args_list=%%b %%c&goto :processArgs )

  set BKR_ARGS=%BKR_ARGS% %%a
  set args_list=%%b %%c
  goto :processArgs

)
:exitProcessArgs

if "%JAVA_HOME%" == "" (echo "Please set the JAVA_HOME environment variable or use -javahome" & goto end)

set JVM_ARGS=%JVM_ARGS% -Dimq.home=%IMQ_HOME%
set _classes=%DEPENDLIBS%\javax.jms-api.jar;%IMQ_HOME%\..\..\share\opt\classes;%DEPENDLIBS%\glassfish-api.jar;%DEPENDLIBS%\grizzly-portunif.jar;%DEPENDLIBS%\hk2.jar;%DEPENDLIBS%\hk2-api.jar;%DEPENDLIBS%\jhall.jar;%DEPENDLIBS%\jta.jar;%DEPENDLIBS%\fscontext.jar;%DEPENDLIBS%\audit.jar;%DEPENDLIBS%\bdb_je.jar;%DEPENDLIBS%\grizzly-framework.jar;%IMQ_EXTERNAL%\*

"%JAVA_HOME%\bin\java" -cp %_classes% %JVM_ARGS% %_mainclass% %BKR_ARGS%

endlocal

EXIT /b %ERRORLEVEL%

if "%OS%" == "Windows_NT" setlocal

REM Specify additional arguments to the JVM here
set JVM_ARGS=
if "%IMQ_EXTERNAL%" == "" set IMQ_EXTERNAL=q:\jpgserv\export\jmq\external

set _IMQ_HOME=..
set _DEPENDLIBS=..\..\..\..\main\packager\target\artifacts\jars

if "%1" == "-javahome" goto setjavahome
:resume

if "%JAVA_HOME%" == "" (echo Please set the JAVA_HOME environment variable or use -javahome. & goto end)

set JVM_ARGS=%JVM_ARGS% -Dimq.home=%_IMQ_HOME%

if "%CLASSPATH%" == "" goto noclasspath

REM
REM Append CLASSPATH to _classes if it is set
REM

goto resume2

:noclasspath
set _classes=%_IMQ_HOME%\..\..\share\opt\classes;%_DEPENDLIBS%\glassfish-api.jar;%_DEPENDLIBS%\grizzly-portunif.jar;%_DEPENDLIBS%\hk2.jar;%_DEPENDLIBS%\hk2-api.jar;%_DEPENDLIBS%\jhall.jar;%_DEPENDLIBS%\jta.jar;%_DEPENDLIBS%\fscontext.jar;%_DEPENDLIBS%\audit.jar;%_DEPENDLIBS%\bdb_je.jar;%_DEPENDLIBS%\grizzly-framework.jar;%CLASSPATH%
set _classes=%IMQ_HOME%\..\..\share\opt\classes;%DEPENDLIBS%\grizzly-framework.jar;%DEPENDLIBS%\grizzly-portunif.jar;%DEPENDLIBS%\glassfish-api.jar;%DEPENDLIBS%\hk2-api.jar;%DEPENDLIBS%\jhall.jar;%DEPENDLIBS%\javax.transaction-api.jar;%DEPENDLIBS%\fscontext.jar;%DEPENDLIBS%\audit.jar;%DEPENDLIBS%\bdb_je.jar;%IMQ_EXTERNAL%\*

:resume2


set _mainclass=com.sun.messaging.jmq.admin.apps.objmgr.ObjMgr

REM 
REM  Use %* on NT, use %1 %2 .. %9 on win98
REM
if "%OS%" == "Windows_NT" goto winnt

:win98
"%JAVA_HOME%"\bin\java -cp %_classes% %JVM_ARGS% %_mainclass% %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end

:winnt
"%JAVA_HOME%"\bin\java -cp %_classes% %JVM_ARGS% %_mainclass% %*
goto end

:setjavahome
set JAVA_HOME=%2
goto resume

:end
if "%OS%" == "Windows_NT" endlocal
