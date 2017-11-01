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

REM # This is a developer edition for internal use.
REM # ikeytool is a wrapper script around JDK keytool and is used to
REM # generate the keypair for SSL
REM #
REM # To generate keystore and self signed certificate for the broker
REM # usage: imqkeytool [-broker]
REM #
REM # To generate keystore and a self-signed certificate for the HTTPS
REM # tunnel servlet
REM # usage: imqkeytool -servlet <keystore location>
REM #
REM #

if not "%OS%"=="Windows_NT" goto notNT
setlocal

REM Specify additional arguments to the JVM here
set JVM_ARGS=

if "%IMQ_HOME%" == "" set IMQ_HOME=..


if "%1" == "-javahome" goto setjavahome
:resume

if "%JAVA_HOME%" == "" (echo Please set the JAVA_HOME environment variable
or use -javahome. & goto end)


if "%1" == "-servlet" goto servlet
if "%1" == "-broker" goto broker
if "%1" == "" goto broker
goto usage
:broker
REM
REM generate keystore and certificate for the broker
REM
echo "Generating keystore for the broker ..."
set _KEYSTORE=%IMQ_HOME%\etc\keystore

echo Keystore=%_KEYSTORE%

"%JAVAHOME%\bin\keytool" -v -genkey -keyalg "RSA" -alias imq -keystore "%_KEYSTORE%"

goto end
:servlet
REM
REM generate keystore and certificate for the HTTPS tunntel servlet
REM
if "%2" == "" goto nopath
set _KEYSTORE=%2
echo "Generating keystore for the HTTPS tunnel servlet ..."
echo Keystore=%_KEYSTORE%
"%JAVAHOME%\bin\keytool" -v -genkey -keyalg "RSA" -alias imqservlet -keystore "%_KEYSTORE%"
if %ERRORLEVEL% == 0 (echo Make sure the keystore is accessible and readable by the HTTPS tunnel servlet.)
goto end

:setjavahome
set JAVA_HOME=%2
shift
shift
goto resume

:nopath
echo Please specify keystore location for the -servlet option
goto usage
:usage
(echo usage:)
(echo imqkeytool [-broker])
(echo    generates a keystore and self-signed certificate for the broker)
(echo imqkeytool -sevlet keystore_location)
(echo    generates a keystore and self-signed certificate for the HTTPS)
(echo    tunnel servlet, keystore_location specifies the name and location)
(echo    of the keystore file)
goto end
:notNT
echo The iMQ keytool requires Windows NT or Windows 2000

:end
if "%OS%"=="Windows_NT" endlocal
