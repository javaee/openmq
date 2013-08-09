@echo off
REM
REM  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
REM 
REM  Copyright (c) 2000-2010 Oracle and/or its affiliates. All rights reserved.
REM 
REM  The contents of this file are subject to the terms of either the GNU
REM  General Public License Version 2 only ("GPL") or the Common Development
REM  and Distribution License("CDDL") (collectively, the "License").  You
REM  may not use this file except in compliance with the License.  You can
REM  obtain a copy of the License at
REM  https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
REM  or packager/legal/LICENSE.txt.  See the License for the specific
REM  language governing permissions and limitations under the License.
REM 
REM  When distributing the software, include this License Header Notice in each
REM  file and include the License file at packager/legal/LICENSE.txt.
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

:: Name of this utility
set MQMIGRATE=mqmigrate
:: Default MQ Src Install Dir to copy data FROM
set DEFAULT_MQSRCDIR=c:\Sun\Message_Queue
:: Default MQ Dst Install Dir to copy data TO
:: XXX
set DEFAULT_MQDSTDIR="c:\Program Files\Sun\JES5\Message_Queue"

::
:: Command line options
::
set SRC_FLAG_SHORT=-srcdir
set SRC_FLAG_LONG=--srcdir
set DST_FLAG_SHORT=-dstdir
set DST_FLAG_LONG=--dstdir

:: Initialize MQ src dir to Message_Queue
set MQSRCDIR=c:\Sun\Message_Queue
:: Initialize MQ dst dir to Message_Queue
set MQDSTDIR="c:\Program Files\Sun\JES5\Message_Queue"

:: Build the MQSRCDIR and MQDSTDIR variables
:: on the command line
goto getArgs
:postGetArgs

echo debug: in postgetargs
echo debug: MQSRCDIR is %MQSRCDIR%
echo debug: MQDSTDIR is %MQDSTDIR%

echo %MQMIGRATE%: Source MQ InstallDir: %MQSRCDIR%
echo %MQMIGRATE%:   Dest MQ InstallDir: %MQDSTDIR%
:: Check srcdir and dstdir actually exist along with a
:: var\instances directory.
goto checkSrcDir
goto checkDstDir

goto end

:::::::::::::::::
:: subroutines ::
:::::::::::::::::

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: getArgs
:: Fill the MQSRCDIR and MQDSTDIR variables with the command line args
:: If the -srcdir or the -dstdir options are detected save the next 
:: argument in the MQSRCDIR variable or MQDSTDIR variable. 
:: This goto loop keeps shifiting until the %1 variable is empty
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:getArgs
echo debug: in getargs percent1 is %1
if %1x==x goto postGetArgs
if %1==%SRC_FLAG_SHORT% goto getSrcDir
if %1==%SRC_FLAG_LONG%  goto getSrcDir
if %1==%DST_FLAG_SHORT% goto getDstDir
if %1==%DST_FLAG_LONG%  goto getDstDir
if not %1x==x (echo %MQMIGRATE%: illegal option -- %1& goto usage)
shift
goto getArgs


::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: getSrcDir
:: Set the MQSRCDIR variable to the value after -srcdir.
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:getSrcDir

:: instance flag
:: index to argument after instance flag
shift
echo debug: in getSrcDir
:: set instance name to the next argument or the default instance 
set MQSRCDIR=%1
if %MQSRCDIR%x==x (echo %MQMIGRATE%: No value specified for -srcdir.&  echo %MQMIGRATE%: Using %DEFAULT_MQSRCDIR% for the -srcdir value.& set MQSRCDIR=%DEFAULT_MQSRCDIR%)
shift
echo debug: MQSRCDIR is %MQSRCDIR%
goto getArgs


::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: getDstDir
:: Set the MQDSTDIR variable to the value after -dstdir
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:getDstDir

:: instance flag
:: index to argument after instance flag
shift
echo debug: in getDstDir
:: set instance name to the next argument or the default instance 
set MQDSTDIR=%1
if %MQDSTDIR%x==x (echo %MQMIGRATE%: No value specified for -dstdir.& echo %MQMIGRATE%: Using %DEFAULT_MQDSTDIR% for the -dstdir value.& set MQDSTDIR=%DEFAULT_MQDSTDIR%)
shift
echo debug: MQDSTDIR is %MQDSTDIR%
goto getArgs


::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: checkSrcDir
:: check that MQSRCDIR is set
:: check that the value defined in MQSRCDIR exists on the filesystem
:: check that the value defined in MQSRCDIR\var\instances exists on the filesystem
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:checkSrcDir

echo debug: in checkSrcDir

:: Check MQSRCDIR defined
if %MQSRCDIR%x==x (echo %MQMIGRATE%: Please specify a Message Queue installation to migrate the data from using the -srcdir option.& goto end)

:: Check MQSRDIR exists
if not exist %MQSRCDIR% (echo %MQMIGRATE%: Cannot locate a Message Queue installation in %MQSRCDIR% to migrate the data FROM.& goto end)

:: Check MQSRDIR\var\instances exists
if not exist %MQSRCDIR%\var\instances (echo %MQMIGRATE%: Cannot locate Message Queue data in %MQSRCDIR%\var\instances to migrate data FROM.& goto end)

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: checkDstDir
:: check that MQDSTDIR is set
:: check that the value defined in MQDSTDIR exists on the filesystem
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:checkDstDir

echo debug: in checkDstDir

:: Check MQDSTDIR defined
if %MQDSTDIR%x==x (echo %MQMIGRATE%: Please specify a Message Queue installation to migrate the data from using the -dstdir option.& goto end)

:: Check MQDSTDIR exists
if not exist %MQDSTDIR% (echo %MQMIGRATE%: Cannot locate a Message Queue installation in %MQDSTDIR% to migrate the dat TO.& goto end)

rem :: Check MQDSTDIR\var\instances exists
rem if not exist %MQDSTDIR%\var\instances (echo %MQMIGRATE%: Cannot locate Message Queue data to migrate to in %MQDSTDIR%\var\instances.& goto end)

goto end

::::::::::::
:: Errors ::
::::::::::::

:notAdmin
echo %MQMIGRATE%: You must be a member of the Administrator group to administer the console
goto end


:usage
echo Usage: mqmigrate [-srcdir MQ_Src_InstallDir] [-dstdir MQ_Dst_InstallDir]
echo        (Double quote InstallDirs with spaces.)
goto end


:end
echo debug: in end

