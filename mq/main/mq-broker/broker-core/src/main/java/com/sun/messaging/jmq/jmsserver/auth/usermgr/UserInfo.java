/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2012 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)UserInfo.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth.usermgr;

public class UserInfo {
    public final static String ROLE_ANON		= "anonymous";
    public final static String ROLE_USER		= "user";
    public final static String ROLE_ADMIN		= "admin";

    public final static String DEFAULT_ADMIN_USERNAME	= "admin";
    public final static String DEFAULT_ADMIN_PASSWD	= "admin";

    public final static String DEFAULT_ANON_USERNAME	= "guest";
    public final static String DEFAULT_ANON_PASSWD	= "guest";
    
    String	user = null,
    		passwd = null,
    		role = null;
    boolean	active = true;

    public UserInfo(String user, String passwd) {
        this(user, passwd, "user", true);
    }

    public UserInfo(String user, String passwd, String role) {
        this(user, passwd, role, true);
    }

    public UserInfo(String user, String passwd, String role, boolean active) {
        this.user = user;
        this.passwd = passwd;
        this.role = role;
        this.active = active;
    }

    public String getUser() {
        return user;
    }

    public String getPasswd() {
        return passwd;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive()  {
	return active;
    }

    public String getPasswdEntry()  {
        return (user
		+ ":" 
		+ passwd
		+ ":"
		+ role
		+ ":"
		+ (active ? "1" : "0"));
    }

    public String toString() {
        return (getPasswdEntry());
    }
}

