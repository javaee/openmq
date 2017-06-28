#!/bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2000-2012 Oracle and/or its affiliates. All rights reserved.
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
# MQ Database Administration startup script: Developer Edition
#
# This is a a version of the database administration startup script
# that works when run in the "binary" directory (as opposed to "dist").
# It uses the loose class files and not the jars
#
# Parse Arguments 
#
#  -imqhome -> sets imq home
#  -imqvarhome -> sets imq var home
#  -javahome -> sets javahome
#

def_jvm_args="-Xmx128m"

bin_home=`dirname $0`

imq_home=$bin_home/..
imq_varhome=$bin_home/../var
dependlibs=$bin_home/../../../share/opt/depend

javacmd=java
# #####hpux-dev#####
PLATFORM=`uname`
ARCH=`uname -p`
if [ $PLATFORM = HP-UX ]; then
    javahome=${_JAVA_HOME:-/opt/java6}
elif [ $PLATFORM = Darwin ]; then
    javahome=${_JAVA_HOME:-/Library/Java/Home}
elif [ $PLATFORM = AIX ]; then
    javahome=${_JAVA_HOME:-/usr/java6}
elif [ $PLATFORM = SunOS ]; then
    javahome=${_JAVA_HOME:-/usr/jdk/latest}
elif [ $PLATFORM = Linux ]; then
    javahome=${_JAVA_HOME:-/usr/java/latest}
fi

while [ $# != 0 ]; do
  case "$1" in
    -imqhome) imq_home=$2; shift 2;;
    -imqvarhome) imq_varhome=$2 ; jvm_args="$jvm_args -Dimq.varhome=$imq_varhome"; shift 2;;
    -javahome) javahome=$2; shift 2;;
    -vmargs) shift; jvm_args="$jvm_args $1"; shift ;;
    *)  args="$args $1"; shift  ;;
  esac
done

javacmd=$javahome/bin/$javacmd

jvm_args="$def_jvm_args $jvm_args -Dimq.home=$imq_home"

#_classes=$imq_home/../../share/opt/classes
_classes=$imq_home/../../share/opt/classes:$dependlibs/javax.jms-api.jar:$dependlibs/grizzly-framework.jar:$dependlibs/grizzly-portunif.jar:$dependlibs/glassfish-api.jar:$dependlibs/hk2-api.jar:$dependlibs/javax.transaction-api.jar:$dependlibs/jhall.jar:$dependlibs/fscontext.jar:$dependlibs/audit.jar:$dependlibs/bdb_je.jar

# Additional classes possibly needed for JDBC provider
_classes=$_classes:$imq_home/lib/ext
# Put all jar and zip files in $imq_home/lib/ext in the classpath
for file in $imq_home/lib/ext/*.jar $imq_home/lib/ext/*.zip; do
    if [ -r "$file" ]; then
	_classes=$_classes:$file
    fi
done

_mainclass=com.sun.messaging.jmq.jmsserver.persist.jdbc.DBTool

# Needed to locate libimq
#####hpux-dev#####
if [ "$PLATFORM" = HP-UX ] ; then
SHLIB_PATH=$SHLIB_PATH:$imq_home/lib; export SHLIB_PATH
else
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$imq_home/lib; export LD_LIBRARY_PATH
fi

$javacmd -cp $_classes $jvm_args $_mainclass $args
