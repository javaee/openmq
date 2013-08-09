#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2000-2010 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

#############################################################################
#
# This file contains example properties for plugging a DB2 database
#

###########################################################################
# Persistence Settings
###########################################################################

# Type of data store
# To plug in a database, set the value to "jdbc".
imq.persist.store=jdbc

# An identifier to make database table names unique per broker.
# The specified value should contain alphanumeric characters only.
# The length of the identifier should not exceed the maximum length
# of a table name allowed in the database minus 12.
imq.brokerid=<alphanumeric id>

# DB2 database settings
##########################

# Specify DB2 as database vendor
imq.persist.jdbc.dbVendor=db2

# DB2 JDBC driver
imq.persist.jdbc.db2.driver=com.ibm.db2.jcc.DB2Driver

# The URL to connect to an DB2 database
imq.persist.jdbc.db2.opendburl=jdbc:db2://<host>:<port>/<database>

# User name used to access the database.
# This can also be specified by command line option for imqbroker and
# imqdbmgr.
imq.persist.jdbc.db2.user=<username>

# Specify whether the broker should prompt the user for a password for
# database access.
# It should be set to true if the password is not provided by other means
#imq.persist.jdbc.db2.needpassword=[true|false]
