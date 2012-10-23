#ifndef lint
static  char sccsid[] = "@(#)com_sun_messaging_jmq_jmsserver_audit_BSMAuditSession.c	1.5 07/02/07 Copyr 2004 Sun Microsystems, Inc.";
#endif

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)com_sun_messaging_jmq_jmsserver_audit_BSMAuditSession.c	1.5 07/02/07
 */ 

#include <pwd.h>
#include <jni.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>

#include "com_sun_messaging_jmq_jmsserver_audit_impl_BSMAuditSession.h"

/*
 * Class:     BSMAuditSession
 * Method:    nativeGetUidGid
 * Signature: int[] nativeGetUidGid(String)
 */

JNIEXPORT jintArray JNICALL Java_com_sun_messaging_jmq_jmsserver_audit_BSMAuditSession_nativeGetUidGid (JNIEnv *env, jclass class, jstring juser)
{
    jint* jids;
    jintArray idArray;
    struct passwd *pw;
    const char *user = (*env)->GetStringUTFChars(env, juser, NULL);

    jids = (jint*)malloc(2 *sizeof(jint));
    jids[0] = -1;
    jids[1] = -1;
    if (user != NULL && strlen(user)) {
	pw = getpwnam(user);
	if (pw != NULL) {
	    jids[0] = pw->pw_uid;
	    jids[1] = pw->pw_gid;
	}
    }   

    idArray = (*env)->NewIntArray(env, 2);
    (*env)->SetIntArrayRegion(env, idArray, 0, 2, jids);
    (*env)->ReleaseStringUTFChars(env, juser, user);
    free(jids);

    return idArray;
}

/*
 * Class:     BSMAuditSession
 * Method:    nativeBrokerUidGid
 * Signature: int[] nativeBrokerUidGid()
 */

JNIEXPORT jintArray JNICALL Java_com_sun_messaging_jmq_jmsserver_audit_BSMAuditSession_nativeBrokerUidGid (JNIEnv *env, jclass class)
{
    jint* jids;
    jintArray idArray;

    int ids[2] = { -1, -1 };
    ids[0] = getuid();
    ids[1] = getgid();

    jids = (jint*)malloc(2 *sizeof(jint));
    jids[0] = getuid();
    jids[1] = getgid();
    idArray = (*env)->NewIntArray(env, 2);
    (*env)->SetIntArrayRegion(env, idArray, 0, 2, jids);
    free(jids);

    return idArray;
}

