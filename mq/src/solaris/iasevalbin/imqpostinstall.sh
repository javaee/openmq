#!/bin/sh
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

#
# MQ Postinstall Script for Solaris Evaluation 
#
# $1 - First parameter is the installation root of the entire iAS dist.
#
INSTALL_ROOT=$1
if [ "$INSTALL_ROOT" = "" ];then
    echo "Usage: $0 <Installation_Root_Dir>";
    exit 1;
fi;
IMQ_ROOT=$INSTALL_ROOT/imq

#
# Save out $2 (JDK_HOME) to var.init/jdk.env for scripts to pick up.
#
#JDK_HOME=$2
#if [ "${JDK_HOME}x" = "x" ];then
#    cat /dev/null> "$IMQ_ROOT/var/jdk.env"
#else
#    echo $JDK_HOME> "$IMQ_ROOT/var/jdk.env"
#fi;

/bin/mkdir -p "$IMQ_ROOT/var"
/bin/chmod -f 0755 "$IMQ_ROOT/var"
/bin/mkdir -p "$IMQ_ROOT/var/instances"
/bin/chmod -f 01777 "$IMQ_ROOT/var/instances"

# Copy security files only they do not already exist.
#cd "$IMQ_ROOT/var/security";
#if [ ! -s "$IMQ_ROOT/var/security/accesscontrol.properties" ];
#then
#	echo "Copying accesscontrol.properties file in "
#	echo "$IMQ_ROOT/var/security"
#	/bin/cp "$IMQ_ROOT/var.init/security/accesscontrol.properties" .
#	/bin/chmod 644 "$IMQ_ROOT/var/security/accesscontrol.properties";
#fi

#if [ ! -s "$IMQ_ROOT/var/security/passwd" ];
#then
#	echo "Copying passwd files in "
#	echo "$IMQ_ROOT/var/security"
#	/bin/cp "$IMQ_ROOT/var.init/security/passwd" .
#	/bin/chmod 644 "$IMQ_ROOT/var/security/passwd";
#fi

exit 0

