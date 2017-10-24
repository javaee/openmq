/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

#include <jni.h>
#include <unistd.h>
#include <stdlib.h>
#include "com_sun_messaging_jmq_util_Password.h"
#if defined(HPUX)
#include <string.h>
#include <sys/termios.h>
#endif
#if defined(AIX)
#include <string.h>
#include <termios.h>
#endif

/*
 * @(#)com_sun_messaging_jmq_util_Password.c	1.9 07/02/07
 */ 

/*
 * Class:     Password
 * Method:    getHiddenPassword
 * Signature: ()Ljava/lang/String;
 */

JNIEXPORT jstring JNICALL Java_com_sun_messaging_jmq_util_Password_getHiddenPassword (JNIEnv *env, jobject obj)  {
    char	*buf;

#if defined(HPUX) || defined(AIX)
    struct termios termio;
    int res;
    char pbuf[257];
    if( (res = tcgetattr( 1, &termio)) )
    {
	return NULL;
    }
    termio.c_lflag &= ~ECHO;
    if( (res = tcsetattr( 1, TCSANOW, &termio)) )
    {
        return NULL;
    }
    if (fgets(pbuf,256,stdin) == NULL)
    {
	buf = NULL;
    }
    else
    {
	char *tmp;
        tmp = strchr(pbuf,'\n');
        if (tmp) *tmp = '\0';
        tmp = strchr(pbuf,'\r');
        if (tmp) *tmp = '\0';
        buf = strdup(pbuf);
    }
    if( (res = tcgetattr( 1, &termio)) )
    {
        return NULL;
    }
    termio.c_lflag |= ECHO;
    if( (res = tcsetattr( 1, TCSANOW, &termio)) )
    {
        return NULL;
    }
#else
    buf = (char *)getpassphrase("");
#endif


    return((*env)->NewStringUTF(env, buf));
}


