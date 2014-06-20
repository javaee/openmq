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
 * @(#)imqbrokerd.c	1.5 07/02/07
 */ 

/*
 * Front-end program to MQ broker. All this does is call
 * 	IMQ_HOME/bin/imqbrokersvc -console
 */

#include <windows.h>
#include <io.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <process.h>
#include <direct.h>
#include <errno.h>
#include "mqapp.h"

/*
 * Class path entries. Relative paths are assumed to be relative to
 * $imqhome/lib
 */
char *classpath_entries[] = {
        "dummy.jar"
        };
int nclasspath_entries = 0;

void brokerParseArgs (MqEnv *me, char *argv[], int argc)
{
    argv++; argc--;
    while (argc > 0) {
        /* We don't recognize the option, pass it on to application */
        me->application_argv[me->application_argc] = _strdup(*argv);
        me->application_argc++;
        argv++; argc--;
    }
}


void main(int argc, char** argv)
{
    char cmdLine[1024];
    char brokerCmd[512];
    char *p;
    DWORD exitCode = 0;
    MqEnv	me;
    int i;

    p = getenv("OS");
    if ((p == NULL) || (strcmp(p, "Windows_NT") != 0)) {
	printf("The MQ broker requires Windows NT or Windows 2000\n");
	exit(1);
    }

    MqAppInitMqEnv(&me, "");

    brokerParseArgs (&me, argv, argc);

    if (MqAppInitializeNoJava(&me) < 0) {
	exit (1);
    }

    sprintf(brokerCmd, "\"%s\\bin\\imqbrokersvc\"", me.imqhome);

    /* Copy Java command and command line arguments into command line */
    strcpy(cmdLine, brokerCmd);

    /*
     * Append -console
     */
    strcat(cmdLine, " -console");

    /*
     * Append any other options passed in
     */
    for (i = 0; i < me.application_argc; i++) {
        strcat(cmdLine, " ");
        strcat(cmdLine, "\"");
        strcat(cmdLine, me.application_argv[i]);
        strcat(cmdLine, "\"");
    }

    exitCode = MqAppRunCmd(cmdLine);

    exit(exitCode);
}
