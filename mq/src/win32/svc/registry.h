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

/*
 * @(#)registry.h	1.10 10/17/07
 */ 

#ifndef _REGISTRY_H
#define _REGISTRY_H

#define JREHOME_KEY    "JREHome"
#define JVMARGS_KEY     "JVMArgs"
#define SERVICEARGS_KEY "ServiceArgs"
/* ####jes-windev#### start ## changing the SERVICE_NAME from iMQ_Broker to MQ_Broker and DISPLAY_NAME from iMQ Broker to Message Queue Broker */ 
#define SERVICE_NAME                "MQ5.1_Broker"
#define DISPLAY_NAME                "Message Queue 5.1 Broker"
/* ####jes-windev#### end */
#define PARAM_KEY_PATH  "SYSTEM\\CurrentControlSet\\Services\\" SERVICE_NAME "\\Parameters"

#ifdef __cplusplus
extern "C" {
#endif

extern char *vm_libs[];
extern int nvm_libs;

/************************************************************************
 *
 * saveStringInRegistry()
 *
 * value - Char array holding '\0' terminated string to save
 * value_size - size of 'value' parameter including terminating '\0'
 * key - subKey to hold value in. E.g. "VMArgs"
 *
 * Returns
 *     ERROR_SUCCESS on success
 *     Winerror.h error on failure
 ************************************************************************/
extern int saveStringInRegistry(const char *value, long value_size, const char* key);

/************************************************************************
 *
 * getStringFromRegistry()
 *
 * value - Char array to place string value in
 * value_size - size of 'value' parameter including terminating '\0'. 
 *              Upon return this will contain the number of bytes of data
 *              retrieved.
 * key - subKey to get value from. E.g. "VMArgs"
 *
 * Returns
 *     ERROR_SUCCESS on success
 *     Winerror.h error on failure
 ************************************************************************/
extern int getStringFromRegistry(char *value, long *value_size, const char* key);
extern int getAnyStringFromRegistry(const char path, char *value, long *value_size, const char* key);

#ifdef __cplusplus
}
#endif

#endif
