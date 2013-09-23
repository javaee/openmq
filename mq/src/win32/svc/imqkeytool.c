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
 * @(#)imqkeytool.c	1.9 07/02/07
 */ 

/*
 * Front-end program to MQ key tool administration for generating SSL
 * key pairs. It calls JDK's keytool.
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

char keystore[MAX_PATH];
char aliasname[MAX_PATH];
BOOL do_broker = TRUE;

/*
 * Class path entries. Relative paths are assumed to be relative to
 * $imqhome/lib
 */
char *classpath_entries[] = {
        "dummy.jar"
        };
int nclasspath_entries = 0;


void printUsage ()
{
    printf(
	"\nusage:\n"
        "imqkeytool [-broker]\n"
        "   generates a keystore and self-signed certificate for the broker\n\n"
        "imqkeytool -servlet <keystore_location>\n"
        "   generates a keystore and self-signed certificate for the HTTPS\n"
        "   tunnel servlet, keystore_location specifies the name and location\n"
        "   of the keystore file\n\n");
}

void keyToolParseArgs (MqEnv *me, char *argv[], int argc)
{
    argv++; argc--;
    while (argc > 0) {
        if (strcmp(*argv, "-javahome") == 0) {
            argv++; argc--;
            if (argc > 0) {
                strncpy(me->jrehome, *argv, sizeof(me->jrehome) - 32);
                strcat(me->jrehome, "\\jre");
            }
        } else if (strcmp(*argv, "-jrehome") == 0) {
            argv++; argc--;
            if (argc > 0) {
                strncpy(me->jrehome, *argv, sizeof(me->jrehome));
            }
        } else if (strcmp(*argv, "-varhome") == 0) {
            argv++; argc--;
            if (argc > 0) {
                strncpy(me->imqvarhome, *argv, sizeof(me->imqvarhome));
            }
        } else if (strcmp(*argv, "-broker") == 0) {
	    do_broker = TRUE;
        } else if (strcmp(*argv, "-servlet") == 0) {
	    do_broker = FALSE;

            argv++; argc--;
	    if (argc > 0)  {
                strncpy(keystore, *argv, sizeof(keystore));
	    } else  {
		printf("Please specify keystore location for the -servlet option\n");
	        printUsage();
	        exit(1);
	    }
        } else {
	    printUsage();
	    exit(1);
        }
        argv++; argc--;
    }
}

void main(int argc, char** argv)
{
    char cmdLine[1024];
    char keytoolCmd[512];
    char *p;
    DWORD exitCode = 0;
    MqEnv	me;

    p = getenv("OS");
    if ((p == NULL) || (strcmp(p, "Windows_NT") != 0)) {
	printf("The MQ keytool requires Windows NT or Windows 2000\n");
	exit(1);
    }

    aliasname[0] = '\0';
    keystore[0] = '\0';

    MqAppInitMqEnv(&me, "");

    keyToolParseArgs (&me, argv, argc);

    if (MqAppInitialize(&me, classpath_entries, nclasspath_entries, FALSE, FALSE) < 0) {
	exit (1);
    }

    if (do_broker)  {
	strcpy(aliasname, "imq");
    } else  {
	strcpy(aliasname, "imqservlet");
    }

    /*
     * keystore is set only if -servlet is used (-servlet usage
     * is detected in keyToolParseArgs()), set the keystore to 
     * imqhome/etc/keystore
     */
    if (keystore[0] == '\0') {
        if (me.imqetchome[0] == '\0') {
            strcpy(keystore, me.imqhome);
            strcat(keystore, "\\etc");
        } else {
            strcpy(keystore, me.imqetchome);
        }
        strcat(keystore, "\\keystore");

    }

    sprintf(keytoolCmd, "\"%s\\bin\\keytool\"", me.jrehome);

    /* Copy Java command and command line arguments into command line */
    strcpy(cmdLine, keytoolCmd);

    /*
     * Append rest of keytool options including alias name and keystore
     * location.
     */
    strcat(cmdLine, " -v -genkey -keyalg \"RSA\" -alias ");
    strcat(cmdLine, aliasname);
    strcat(cmdLine, " -keystore ");
    strcat(cmdLine, "\"");
    strcat(cmdLine, keystore);
    strcat(cmdLine, "\"");

    printf("Keystore: %s\n", keystore);
    if (do_broker)  {
	printf("Generating keystore for the broker ...\n");
    } else  {
	printf("Generating keystore for the HTTPS tunnel servlet ...\n");
    }

    exitCode = MqAppRunCmd(cmdLine);

    if (!do_broker && (exitCode == 0))  {
	printf("Make sure the keystore is accessible and readable by the HTTPS tunnel servlet.\n");
    }

    exit(exitCode);
}
