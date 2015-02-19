'
' DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
'
' Copyright (c) 2000-2010 Oracle and/or its affiliates. All rights reserved.
'
' The contents of this file are subject to the terms of either the GNU
' General Public License Version 2 only ("GPL") or the Common Development
' and Distribution License("CDDL") (collectively, the "License").  You
' may not use this file except in compliance with the License.  You can
' obtain a copy of the License at
' https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
' or packager/legal/LICENSE.txt.  See the License for the specific
' language governing permissions and limitations under the License.
'
' When distributing the software, include this License Header Notice in each
' file and include the License file at packager/legal/LICENSE.txt.
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

MYDIR=trim(Replace(Wscript.scriptFullName, Wscript.scriptName, ""))

'
' This is the VM/CLASSPATH that was used to run the installer
'
CLASSPATH=wShell.ExpandEnvironmentStrings("%CLASSPATH%")
INSTALLER_JAVA_HOME=wShell.ExpandEnvironmentStrings("%INIT_CONFIG_JAVA_HOME%")
INSTALLER_JAVA_CLASSPATH=wShell.ExpandEnvironmentStrings("%INIT_CONFIG_JAVA_CLASSPATH%")
'INSTALLER_JAVA_CLASSPATH=INSTALLER_JAVA_CLASSPATH & ";C:\Program Files\Sun\MessageQueue\mq\lib\install"
INSTALLER_JAVA_HELPER=wShell.ExpandEnvironmentStrings("%INIT_CONFIG_HELPER_CLASS%")


'INSTALLER_JAVA_HOME="C:\Program Files\Java\jdk1.5.0_11"
'INSTALLER_JAVA_CLASSPATH=gWshEnv.("CLASSPATH")+";"+gWshEnv("INIT_CONFIG_JAVA_CLASSPATH")+";"+gWshEnv("MYDIR")

'
' This is the VM that we want MQ to use. Currently hardcoded. This value/path
' will eventually be obtained from the PH framework.
'
IMQ_DEFAULT_JAVAHOME=""

'
' This is the path to the imqenv.conf file
'
IMQENV_CONF=""

'
' This is the file containing the JDK location that the uninstaller needs
'
UNINSTALL_PROP=""

RESETFLAG="false"
SILENT=""
VALIDATESET=""

CONFIGDATA=""
CONFIGSCHEMA=""

CONFIG_STATUS="SUCCESS"
PLATFORM_ERROR="None"
PRODUCT_ERROR="None"
DOC_REF="None"
NEXT_STEPS="None"

'-------------------------------------------------------------------------------
' perform actual operation for the script: install/uninstall
' input(s):  none
' output(s): instCode
'-------------------------------------------------------------------------------
Function perform

	'Wscript.echo "Reset: " & RESETFLAG
if RESETFLAG = "false" Then
    '
    ' Write set IMQ_DEFAULT_JAVAHOME=jdklocation to imqenv.conf file
    '
    mycmd = """" & INSTALLER_JAVA_HOME & "\bin\javaw.exe" & """" & " -classpath " & """" & INSTALLER_JAVA_CLASSPATH & """" & " SetupJDKHome"  & " -i" & " " & """" & IMQENV_CONF & """" & " -j" & " ""set IMQ_DEFAULT_JAVAHOME=" & IMQ_DEFAULT_JAVAHOME & """"
    set oExec=Wshell.exec(mycmd)
    Do While oExec.Status = 0
	    WScript.Sleep 500
    Loop

    '
    ' Write jdklocation to uninstaller.properties file
    '
    mycmd = """" & INSTALLER_JAVA_HOME & "\bin\javaw.exe" & """" & " -classpath " & """" & INSTALLER_JAVA_CLASSPATH & """" & " SetupJDKHome"  & " -i" & " " & """" & UNINSTALL_PROP & """" & " -j" & " """ & IMQ_DEFAULT_JAVAHOME & """"
    set oExec=Wshell.exec(mycmd)
    Do While oExec.Status = 0
	    WScript.Sleep 500
    Loop

end if

End Function

'-------------------------------------------------------------------------------
' perform validation - no real action performed
' input(s):  none
' output(s): instCode
'-------------------------------------------------------------------------------
Function doValidate

'Wscript.echo "Reset: " & RESETFLAG
if RESETFLAG = "false" Then
    DOC_REF="Validation of MQ configurator performed. No actual configuration done."
else
    DOC_REF="Validation of MQ unconfigurator performed. No actual unconfiguration done."
end if

End Function


Function initVars

if RESETFLAG = "false" Then

    Set filesys = CreateObject("Scripting.FileSystemObject")
    'Set wShell = CreateObject("WScript.Shell")
    'Set gWshEnv = wShell.Environment("PROCESS")

    ' Create temp file to hold values.  The magic value '2' is for the Temp folder.  This is documented
    'at 'http://msdn.microsoft.com/library/default.asp?url=/library/en-us/script56/html/328b505e-6dfd-4f4a-b819-250ca46689a1.asp
    Set tempfolder = filesys.GetSpecialFolder(2)
    tempname = filesys.GetTempName
    tempfilename = filesys.BuildPath(tempfolder,tempname)

	  ' run openInstaller utility to give us back the name/value pairs in a temp file       
    mycmd = chr(34) & INSTALLER_JAVA_HOME & "\bin\java.exe" & chr(34) & " -classpath " & chr(34) & INSTALLER_JAVA_CLASSPATH & chr(34) & " " & chr(34) & INSTALLER_JAVA_HELPER & chr(34) & " -s " & chr(34) & CONFIGSCHEMA & chr(34) & " " & " -d " & chr(34) & CONFIGDATA & chr(34) & " " & " -o DECODE -f " & chr(34) & tempfilename & chr(34) & " 2> c:\\foo.txt"

      set oExec=Wshell.exec(mycmd)
      Do While oExec.Status = 0
	      WScript.Sleep 500
      Loop
	 	 
    ' read the temp file looking for stuff
    Set fIn = filesys.OpenTextFile(tempfilename)
    Do Until fIn.AtEndOfStream
      sLine = fIn.ReadLine
      if (sLine = "IMQ_SELECTED_JDK") then
        ' Found the one we're looking for.  The next line holds the actual value, so read it.
        IMQ_DEFAULT_JAVAHOME = trim(fIn.ReadLine)
      else if (sLine = "IMQ_INSTALL_HOME") then
        ' Found the Install Home
	IMQ_INSTALL_HOME=trim(fIn.readLine)
      end if
      end if
    Loop

	
    ' close and delete temp file
   fIn.close
   filesys.DeleteFile(tempfilename)

    'MsgBox "IMQ_DEFAULT_JAVAHOME is " + IMQ_DEFAULT_JAVAHOME 
    'MsgBox "IMQ_INSTALL_HOME is " + IMQ_INSTALL_HOME 

    IMQENV_CONF=IMQ_INSTALL_HOME & "\etc\mq\imqenv.conf"
    UNINSTALL_PROP=IMQ_INSTALL_HOME & "\var\install\contents\mq\uninstaller.properties"

    INSTALLER_JAVA_CLASSPATH=INSTALLER_JAVA_CLASSPATH & ";" & IMQ_INSTALL_HOME & "\mq\lib\install"
End If

End Function

Function printStatus
    WScript.StdErr.writeline "<resultReport xmlns=""http://openinstaller.org/config/resultreport/V1"">"
    WScript.StdErr.writeline "<configStatus>" & CONFIG_STATUS & "</configStatus>"
    WScript.StdErr.writeline "<platformError>" & PLATFORM_ERROR & "</platformError>"
    WScript.StdErr.writeline "<productError>" & PRODUCT_ERROR & "</productError>"
    WScript.StdErr.writeline "<docReference>" & DOC_REF & "</docReference>"
    WScript.StdErr.writeline "<nextSteps>" & NEXT_STEPS & "</nextSteps>"
    WScript.StdErr.writeline "</resultReport>"
End Function

'-------------------------------------------------------------------------------
' retrieve bundled JVM from Media based on os and platfo${RM}
' input(s):  none
' output(s): JAVAMEDIAPATH
'-------------------------------------------------------------------------------
Function setJvmAndClasspath

  JAVA_HOME=""
  JAVA_CLASSPATH=""

End Function


'-------------------------------------------------------------------------------
' usage only: define what parameters are available here
' input(s):  exitCode
'-------------------------------------------------------------------------------
Function usage
WScript.echo "Test Product Installer based on openInstaller"

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

case "-f"
 if argumentCounter + 1 < args.Length Then
   CONFIGSCHEMA=trim(args.Item(argumentCounter+1))
   argumentCounter=argumentCounter+2
  Else
    usage
  End if

case "-d"
 if argumentCounter + 1 < args.Length Then
   CONFIGDATA=trim(args.Item(argumentCounter+1))
   argumentCounter=argumentCounter+2
  Else
    usage
  End if

case "-h"
 usage
 argumentCounter=argumentCounter+1

case "-r"
  RESETFLAG="true"
  argumentCounter=argumentCounter+1
  
case "-s"
  SILENT="true"
  argumentCounter=argumentCounter+1

case "-v"
  VALIDATESET="true"
  argumentCounter=argumentCounter+1

case Else
 usage

end select

Loop

initVars

setJvmAndClasspath

if VALIDATESET = "" Then
    perform
else
    doValidate
end if

printStatus
