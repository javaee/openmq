/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)mqapp.h	1.7 07/19/07
 */ 

/*
 * Header file for MQ C applications
 */

#include <windows.h>

/*
 * Structure containing environment settings needed to run the MQ App.
 * The information here will be used to construct the command line.
 */
typedef struct {
    char *main_class;
    char *application_argv[128];
    int application_argc;
    int jvm_argc;
    char **jvm_argv;
    char imqhome[MAX_PATH];
    char imqvarhome[MAX_PATH];
    char imqlibhome[MAX_PATH];
    char imqetchome[MAX_PATH];
    char imqextjars[4096];
    char jrehome[MAX_PATH];
    char classpath[65536];
} MqEnv;

/*
 * Declarations for some functions used my MQ Apps. These are defined
 * in util.c
 */

/*
 * Do some initialization. This basically locates IMQ, the JDK, determines
 * the run classpath (for -cp later).
 *
 * The following fields in the MqEnv are updated.
 *  imqhome
 *  imqlibhome
 *  imqvarhome
 *  imqetchome
 *  imqextjars
 *  jrehome
 *  classpath
 *
 * All the fields above need to point to pre-allocated space.
 *
 * Params:
 *  me			ptr to MqEnv structure, must not be NULL.
 *  classpath_entries	Array containing entries relative to IMQ_HOME/lib that
 *			need to be added to the run classpath. This must not
 *			be NULL.
 *  nclasspath_entries  Number of classpath entries in classpath_entries.
 *  append_libjars	Boolean flag - if true the jars/zips in IMQ_HOME/lib/ext
 *			will be appended to the run classpath.
 *  append_classpath	Boolean flag - if true the value of CLASSPATH will
 *			be appended to the run classpath.
 */
extern int MqAppInitialize(MqEnv *me, char *classpath_entries[], int nclasspath_entries,
			BOOL append_libjars, BOOL append_classpath);

/*
 * Do only MQ related initialization - no java initialization. This basically locates IMQ
 * and sets the relevant pieces of info in the MqEnv structure.
 *
 * The following fields in the MqEnv are updated.
 *  imqhome
 *  imqlibhome
 *  imqvarhome
 *  imqetchome
 *  imqextjars
 *
 * All the fields above need to point to pre-allocated space.
 *
 * Params:
 *  me			ptr to MqEnv structure, must not be NULL.
 */
extern int MqAppInitializeNoJava(MqEnv *me);

/*
 * Parse generic application arguments, setting the relevant
 * fields in the MqEnv structure.
 *
 * The following fields in the MqEnv are updated.
 *  jrehome
 *  application_argv
 *  application_argc
 *
 * The application_argv field needs to point to pre-allocated
 * array of (char *) pointers.
 *
 * Params:
 *  me	 ptr to MqEnv structure, must not be NULL.
 *  argv argument vector
 *  argc number of elements in argv
 */
extern void MqAppParseArgs (MqEnv *me, char *argv[], int argc);

/*
 * Create the java command line based on information
 * in the MqEnv structure.
 *
 * No fields in the MqEnv are updated.
 *
 * Params:
 *  me		ptr to MqEnv structure, must not be NULL.
 *  set_varhome	Boolean flag that indicates whether "-Dimq.varhome"
 *		should be appended to the command line or not.
 *  cmdLine	buffer for storing command line. This
 *		points to pre allocated space.
 */
extern void MqAppCreateJavaCmdLine(MqEnv *me, BOOL set_varhome, char *cmdLine);

/*
 * Run the command, wait for it to exit,
 * and return the exit code.
 * A process is forked to run the command via
 * CreateProcess()
 *
 * Params:
 *  cmdLine	buffer for storing command line. This
 *		points to pre allocated space.
 */
extern DWORD MqAppRunCmd(char *cmdLine);

/*
 * Initialize MqEnv structure
 *
 * The following fields in the MqEnv are updated.
 *  imqhome
 *  imqlibhome
 *  imqvarhome
 *  imqetchome
 *  imqextjars
 *  jrehome
 *  classpath
 *  application_argc
 *  main_class
 *
 * All the string fields above need to point to pre-allocated space.
 *
 * Params:
 *  me		ptr to MqEnv structure, must not be NULL.
 *  main_class	string containing main class of application
 *	e.g. 
 *	"com.sun.messaging.jmq.admin.apps.console.AdminConsole"
 */
extern void MqAppInitMqEnv(MqEnv *me, char *main_class);

/*
 * Run the java command, wait for it to exit,
 * and return the exit code. This function uses
 * the VM invocation APIs in JNI.
 *
 * Params:
 *  me	ptr to MqEnv structure, must not be NULL.
 */
extern DWORD MqAppJNIRunJavaCmd(MqEnv *me);
