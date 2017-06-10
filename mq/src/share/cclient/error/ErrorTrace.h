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
 * @(#)ErrorTrace.h	1.7 06/26/07
 */ 

#ifndef ERRORTRACE_H
#define ERRORTRACE_H

#include <nspr.h>

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

struct ErrorTrace {
  PRBool usable;
  PRUint32 num_elements;
  PRUint32 num_allocated;
  char **trace;
};


/**
 * use PRStatus instead of MQError so that we don't need to
 * include mqtypes.h or MQ headers hence self contained */

PRStatus getErrorTrace(ErrorTrace ** trace);

PRStatus setErrorTraceElement(const char * method,
                              const char * file, PRInt32 lineNumber,
                              const char * errorType, PRUint32 errorCode);

PRStatus setVErrorTraceElement(const char * method,
                               const char * file, PRInt32 lineNumber,
                               const char * errorType, PRUint32 errorCode,
                               const char * const format, ...);

/**
 * clear ErrorTrace,  if all is true, clear thread private date as well */ 
PRStatus clearErrorTrace(PRBool all);


#ifdef __cplusplus
}
#endif /* __cplusplus */

#endif  /* ERRORTRACE_H */
