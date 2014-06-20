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
 * @(#)jmqdbsvc.c	1.12 07/02/07
 */ 

/*
 * Front-end program to start up the database utility.
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
        "imqbroker.jar"
        };
int nclasspath_entries = sizeof (classpath_entries) / sizeof(char *);

char *main_class = "com.sun.messaging.jmq.jmsserver.persist.jdbc.DBTool";

void main(int argc, char** argv)
{
    char cmdLine[1024];
    DWORD exitCode = 0;
    char *p;
    MqEnv	me;

    p = getenv("OS");
    if ((p == NULL) || (strcmp(p, "Windows_NT") != 0)) {
	printf("The MQ Database Manager Utility requires Windows NT or Windows 2000\n");
	exit (1);
    }

    MqAppInitMqEnv(&me, main_class);

    MqAppParseArgs(&me, argv, argc);

    if (MqAppInitialize(&me, classpath_entries, nclasspath_entries, TRUE, TRUE) < 0) {
	exit (1);
    }

    if (_access(me.imqvarhome, 00) < 0) {
        fprintf(stderr, "Couldn't find IMQ_VARHOME '%s'\n", me.imqvarhome);
        exit(1);
    }

    MqAppCreateJavaCmdLine(&me, TRUE, cmdLine);

    exitCode = MqAppRunCmd(cmdLine);

    exit(exitCode);
}
