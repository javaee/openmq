'
' DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
'
' Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
'
' The contents of this file are subject to the terms of either the GNU
' General Public License Version 2 only ("GPL") or the Common Development
' and Distribution License("CDDL") (collectively, the "License").  You
' may not use this file except in compliance with the License.  You can
' obtain a copy of the License at
' https://oss.oracle.com/licenses/CDDL+GPL-1.1
' or LICENSE.txt.  See the License for the specific
' language governing permissions and limitations under the License.
'
' When distributing the software, include this License Header Notice in each
' file and include the License file at LICENSE.txt.
'
' GPL Classpath Exception:
' Oracle designates this particular file as subject to the "Classpath"
' exception as provided by Oracle in the GPL Version 2 section of the License
' file that accompanied this code.
'
' Modifications:
' If applicable, add the following below the License Header, with the fields
' enclosed by brackets [] replaced by your own identifying information:
' "Portions Copyright [year] [name of copyright owner]"
'
' Contributor(s):
' If you wish your version of this file to be governed by only the CDDL or
' only the GPL Version 2, indicate your decision by adding "[Contributor]
' elects to include this software in this distribution under the [CDDL or GPL
' Version 2] license."  If you don't indicate a single choice of license, a
' recipient has the option to distribute your version of this file under
' either the CDDL, the GPL Version 2 or to extend the choice of license to
' its licensees as provided above.  However, if you add GPL Version 2 code
' and therefore, elected the GPL Version 2 license, then the option applies
' only if the new code is made subject to such option by the copyright
' holder.
'

PRODUCTNAME="mq"

Set wShell = CreateObject("WScript.Shell")
gReturnValue = wshell.Run("regsvr32 /s scrrun.dll", 0 ,True)
set gFileSystem = CreateObject("Scripting.FileSystemObject")
Set gWshEnv = wshell.Environment("SYSTEM")

JAVAMEDIAPATH=""
JAVA_HOME=""
MYDIR=trim(Replace(Wscript.scriptFullName, Wscript.scriptName, ""))

ENGINE_DIR=MYDIR+"install"
META_DATA_DIR=MYDIR+"var\install\contents\"+PRODUCTNAME
INIT_CONFIG_DIR=MYDIR+"mq\lib\install"
SILENT=""
INSTALL_PROPS="install.properties"
ST_REG_RELPATH="etc\mq\registry\servicetag.xml"

'LOGDIR=gFileSystem.GetSpecialFolder(2)

'-------------------------------------------------------------------------------
' perform actual operation for the script: install/uninstall
' input(s):  none
' output(s): instCode
'-------------------------------------------------------------------------------
Function perform

REG_CP=MYDIR+"\install-lib\classes"
REG_CP=REG_CP+";"+MYDIR+"\install-lib\registration\commons-codec-1.3.jar"
REG_CP=REG_CP+";"+MYDIR+"\install-lib\registration\registration-api.jar"
REG_CP=REG_CP+";"+MYDIR+"\install-lib\registration\registration-impl.jar"
REG_CP=REG_CP+";"+MYDIR+"\install-lib\registration\sysnet-all.jar"

ENGINE_OPS="-m ""file:///"+META_DATA_DIR+""""
ENGINE_OPS=ENGINE_OPS+" -i ""file:///"+MYDIR +"/Product"""
ENGINE_OPS=ENGINE_OPS+" -p Default-Product-ID="+PRODUCTNAME
ENGINE_OPS=ENGINE_OPS+" -p ""Init-Config-Locations="+PRODUCTNAME+":"+INIT_CONFIG_DIR + """"
ENGINE_OPS=ENGINE_OPS+" -p UI-Options=internalBrowserOnly"
ENGINE_OPS=ENGINE_OPS+" -p Merge-Config-Data=false"
ENGINE_OPS=ENGINE_OPS+" -p No-Upgrade=true"
ENGINE_OPS=ENGINE_OPS+" -p ""Exclude-JVMs=" + MYDIR +"jre"""
ENGINE_OPS=ENGINE_OPS+" -a """+MYDIR +"\"+INSTALL_PROPS+""""
ENGINE_OPS=ENGINE_OPS+" -C """+REG_CP +""""
JAVA_OPTIONS=" -Dmq.install.servicetag.registry.relpath=" + ST_REG_RELPATH

BUNDLED_JAVA_JRE_LOC=MYDIR
  'WScript.Echo "JAVA_HOME: "+JAVA_HOME

if DRYRUN <> "" Then
    ENGINE_OPS=ENGINE_OPS+" -n """+DRYRUN+""""
end if

if ANSWERFILE <> "" Then
    ENGINE_OPS=ENGINE_OPS+" -a """+ANSWERFILE+""""
end if

if SILENT <> "" Then
    ENGINE_OPS=ENGINE_OPS+" -p Display-Mode=SILENT"
end if

if ALTROOT <> "" Then
    ENGINE_OPS=ENGINE_OPS + " -R " + ALTROOT
end if

if LOGLEVEL <> "" Then
    ENGINE_OPS=ENGINE_OPS+" -l "+LOGLEVEL
end if


if LOGDIR <> "" Then
    ENGINE_OPS=ENGINE_OPS+" -p ""Logs-Location="+LOGDIR+""""
end if

if JAVA_HOME <> ""  Then
    ENGINE_OPS=ENGINE_OPS+" -j """+JAVA_HOME+""""
end if

if JAVA_OPTIONS <> "" Then
    ENGINE_OPS=ENGINE_OPS+" -J "+JAVA_OPTIONS
end if

if INSTALLABLES <> "" Then
    ENGINE_OPS=ENGINE_OPS+" -i "+INSTALLABLES
end if

'WScript.echo "wscript //nologo " & chr(34) & ENGINE_DIR & "\bin\engine-wrapper.vbs" & chr(34) & " " &  ENGINE_OPS
wShell.exec "wscript //nologo " & chr(34) & ENGINE_DIR & "\bin\engine-wrapper.vbs" & chr(34) & " " &  ENGINE_OPS


End Function

'-------------------------------------------------------------------------------
' retrieve bundled JVM from Media based on os and platfo${RM}
' input(s):  none
' output(s): JAVAMEDIAPATH
'-------------------------------------------------------------------------------
Function getBundledJvm

  JAVAMEDIAPATH="jre"

End Function

Function useBundledOrDefaultJvm
  getBundledJvm
  'JAVA_HOME=BUNDLED_JAVA_JRE_LOC+"\"+JAVAMEDIAPATH
  JAVA_HOME=MYDIR+"\"+JAVAMEDIAPATH
  if Not gFileSystem.FolderExists(JAVA_HOME) Then
       'try the registry
       javaversion = wShell.RegRead("HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\CurrentVersion")
       JAVA_HOME = wShell.RegRead("HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\"+javaversion+"\JavaHome")
       if SILENT = "" Then
           WScript.echo "Using default java["+javaversion+"] at "+JAVA_HOME
       end if
       if Not gFileSystem.FolderExists(JAVA_HOME) Then
          if SILENT = "" Then
               WScript.Echo JAVA_HOME+" must be the root directory of a valid JVM installation"
               WScript.Echo "Please provide JAVA_HOME as argument with -j option and proceed."
           end if
           WScript.Quit(1)
       end if
  end if
End Function


'-------------------------------------------------------------------------------
' usage only: define what parameters are available here
' input(s):  exitCode
'-------------------------------------------------------------------------------
Function usage
WScript.echo "Usage: " + Wscript.scriptName + " [OPTION]" & VBCr _
	& "" & VBCr _
	& "Options:" & VBCr _
	& "  -a <file>" & VBCr _
	& "  Specify an answer file to be used by installer in non interactive mode." & VBCr _
	& "" & VBCr _
	& "  -h" & VBCr _
	& "  Show usage help" & VBCr _
	& "" & VBCr _
	& "  -j <path>" & VBCr _
	& "  Specify java runtime to use to run the installer" & VBCr _
	& "" & VBCr _
	& "  -r" & VBCr _
	& "  Register product. Does not install anything. Runs the installer's product" &  VBCr _
	& "  registration screens." & VBCr _
	& "" & VBCr _
	& "  -s" & VBCr _
	& "  Silent mode. No output will be displayed. Typically used with an answer file" & VBCr _
	& "  specified with -a" & VBCr _
	& "" & VBCr _
	& "  -n <file>" & VBCr _
	& "  Dry run mode. Does not install anything. Install responses are saved to <file> and " & VBCr _
	& "  can be used with -a to replicate this install. " & VBCr _
	& "" & VBCr _
	& "  Exit status" & VBCr _
	& "  0	Success" & VBCr _
	& "  >0	An error occurred" & VBCr _
	& "" & VBCr _
	& "  Usage examples:" & VBCr _
	& "   Generate answer file" & VBCr _
	& "    installer -n file1" & VBCr _
	& "" & VBCr _
	& "   Using an answer file in silent mode" & VBCr _
	& "    installer -a file1 -s"

WScript.Quit(1)

End Function



'-------------------------------------------------------------------------------
' ****************************** MAIN THREAD ***********************************
'-------------------------------------------------------------------------------

' check arguments

Set args = WScript.Arguments
argumentCounter=0

do while argumentCounter < args.Length
argName=args.Item(argumentCounter)

select case argName

case "-a"
  if argumentCounter + 1 < args.Length Then
    ANSWERFILE=trim(args.Item(argumentCounter+1))
    argumentCounter=argumentCounter+2

	    if Not gFileSystem.FileExists(ANSWERFILE) Then
		WScript.Echo "Cannot read the answer file "+ANSWERFILE
                WScript.Quit(1)
            end if
  Else
    usage
  End if

case "-R"
  if argumentCounter + 1 < args.Length Then
    ALTROOT=trim(args.Item(argumentCounter+1))
    argumentCounter=argumentCounter+2

	    if Not gFileSystem.FolderExists(ALTROOT) Then
		WScript.Echo ALTROOT+" is not a valid alternate root"
                WScript.Quit(1)
            end if
  Else
    usage
  End if

case "-l"
  if argumentCounter + 1 < args.Length Then
    LOGDIR=trim(args.Item(argumentCounter+1))
    argumentCounter=argumentCounter+2

	    if Not gFileSystem.FolderExists(LOGDIR) Then
		WScript.Echo LOGDIR+" is not a directory or is not writable"
                WScript.Quit(1)
	    end if
  Else
    usage
  End if
case "-q"
  LOGLEVEL="WARNING"
  argumentCounter=argumentCounter+1

case "-v"
  LOGLEVEL="FINEST"
  argumentCounter=argumentCounter+1

case "-r"
  INSTALL_PROPS="install_regonly.properties"
  argumentCounter=argumentCounter+1

case "-s"
  SILENT="true"
  argumentCounter=argumentCounter+1

case "-t"
  WScript.Echo "TextUI is not supported for Windows."
  argumentCounter=argumentCounter+1

case "-n"
  if argumentCounter + 1 < args.Length Then
    DRYRUN=trim(args.Item(argumentCounter+1))
    argumentCounter=argumentCounter+2
  Else
    usage
  End if

case "-j"

  if argumentCounter + 1 < args.Length Then
    CMDLINEJAVA_HOME=trim(args.Item(argumentCounter+1))
    argumentCounter=argumentCounter+2

    if Not gFileSystem.FolderExists(CMDLINEJAVA_HOME) Then
	WScript.Echo CMDLINEJAVA_HOME+" must be the root directory of a valid JVM installation"
               WScript.Quit(1)
    end if
  Else
    usage
  End if

case "-J"
  if argumentCounter + 1 < args.Length Then
    JAVAOPTIONS=trim(args.Item(argumentCounter+1))
    argumentCounter=argumentCounter+2
  Else
    usage
  End if

case "-p"
 if argumentCounter + 1 < args.Length Then
   INSTALL_PROPS=INSTALL_PROPS+" -p "+trim(args.Item(argumentCounter+1))
   argumentCounter=argumentCounter+2
  Else
    usage
  End if

case "-h"
 usage
 argumentCounter=argumentCounter+1

case Else
 usage

end select

Loop
      
if SILENT <> "" Then
    if ANSWERFILE = "" Then
        'yeah - we aren't silent but this will just fail
        WScript.Echo "-s argument requires an answer file"
        WScript.Quit(1)
    end if
end if

' overwrite check if user specify javahome to use
if CMDLINEJAVA_HOME = "" Then
    useBundledOrDefaultJvm
else
  JAVA_HOME=CMDLINEJAVA_HOME
  if SILENT = "" Then
      WScript.Echo "Using the user specified JAVA_HOME "+CMDLINEJAVA_HOME
  end if
end if

perform


