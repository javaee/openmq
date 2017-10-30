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
 * @(#)registry.c	1.7 07/02/07
 */ 

#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <process.h>
#include <winreg.h>
#include "registry.h"

// VMs to use. Order is most desireable to least desireable.
// Relative paths are assumed to be relative to jrehome/bin
char *vm_libs[] = {
        "server\\jvm.dll",
        "hotspot\\jvm.dll",
        "client\\jvm.dll",
        "classic\\jvm.dll",
        };
int nvm_libs = sizeof (vm_libs) / sizeof(char *);

int saveStringInRegistry(const char *value, long value_size, const char* name)
{
    long result;
    HKEY hKey;
    DWORD disposition;

    /*
     * Get a handle to the service parameters key. It will be something like
     * HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\IMQBroker\Parameters
     */
    result = RegCreateKeyEx(
        HKEY_LOCAL_MACHINE,
        PARAM_KEY_PATH,
        0,
        "LocalSystem",
        REG_OPTION_NON_VOLATILE,
        KEY_ALL_ACCESS,
        NULL,
        (PHKEY)&hKey,
        (LPDWORD)&disposition);

    if (result != ERROR_SUCCESS) {
        return result;
    }

    /* Set the subkey and value */
    result = RegSetValueEx(
        hKey,
        name,
        0,
        REG_SZ,
        value,
        value_size);

    if (result != ERROR_SUCCESS) {
        return result;
    }

    RegCloseKey(hKey);
}

int getStringFromRegistry(char *value, long *value_size_p, const char* name)
{
    long result;
    HKEY hKey;
    long type;

    /*
     * Get a handle to the service parameters key. It will be something like
     * HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\IMQBroker\Parameters
     */
    result = RegOpenKeyEx(
        HKEY_LOCAL_MACHINE,
        PARAM_KEY_PATH,
        0,
        KEY_READ,
        (PHKEY)&hKey
        );

    if (result != ERROR_SUCCESS) {
        return result;
    }

    /* Get the subkey and value */
    result = RegQueryValueEx(
        hKey,
        name,
        0,
        &type,
        value,
        value_size_p);

    if (result != ERROR_SUCCESS) {
        return result;
    }
}

int getAnyStringFromRegistry(const char * startpath, char *value, long *value_size_p, const char* name)
{
    long result;
    HKEY hKey;
    long type;

    /*
     * Get a handle to the service parameters key. It will be something like
     * HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\IMQBroker\Parameters
     */
    result = RegOpenKeyEx(
        HKEY_LOCAL_MACHINE,
        startpath,
        0,
        KEY_READ,
        (PHKEY)&hKey
        );

    if (result != ERROR_SUCCESS) {
        return result;
    }

    /* Get the subkey and value */
    result = RegQueryValueEx(
        hKey,
        name,
        0,
        &type,
        value,
        value_size_p);

    if (result != ERROR_SUCCESS) {
        return result;
    }

    RegCloseKey(hKey);
}
