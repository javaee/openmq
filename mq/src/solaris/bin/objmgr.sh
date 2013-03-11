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
# JMS Object Administration startup script: Developer Edition
#
# This is a a version of the JMS Object Administration startup 
# script that works when run in the "binary" directory (as opposed
# to "dist"). It uses the loose class files and not the jars.
#
# Parse Arguments 
#
#  -imqhome -> sets imq home
#  -imqvarhome -> sets imq home
#  -javahome -> sets javahome
#  -imqext -> sets imq external
#
# Note: This script fails if you specify any of the options above
# without any arguments e.g.
#	objmgr -imqhome
# (i.e. did not specify a imqhome value)
#

jvm_args="-Xmx128m"

bin_home=`dirname $0`

imq_home=$bin_home/..
imq_external=${JMQ_EXTERNAL:-/net/jpgserv/export/jmq/external}
dependlibs=$bin_home/../../../share/opt/depend

javacmd=java
######hpux-dev#####
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

#
# Save -javahome, -imqhome, -imqvarhome, -imqext
# arg values without recreating the $args string
# so that args with spaces work correctly.
#
javahomenext=false
imqhomenext=false
imqvarhomenext=false
imqextnext=false

for opt in $*
do
  if [ $javahomenext = true ]
  then
    javahome=$opt
    javahomenext=false
  elif [ $imqhomenext = true ]
  then
    imq_home=$opt
    imqhomenext=false
  elif [ $imqvarhomenext = true ]
  then
    imq_varhome=$opt
    imqvarhomenext=false
  elif [ $imqextnext = true ]
  then
    imq_external=$opt
    imqextnext=false
  elif [ X$opt = X-javahome ]
  then
    javahomenext=true;
  elif [ X$opt = X-imqhome ]
  then
    imqhomenext=true;
  elif [ X$opt = X-imqvarhome ]
  then
    imqvarhomenext=true;
  elif [ X$opt = X-imqext ]
  then
    imqextnext=true;
  fi
done

javacmd=$javahome/bin/$javacmd

jvm_args="$jvm_args -Dimq.home=$imq_home"

#_ext_classes=$imq_external/jndifs/lib/fscontext.jar
_ext_classes=$dependlibs/javax.jms-api.jar:$imq_home/../../share/opt/classes:$dependlibs/grizzly-framework.jar:$dependlibs/grizzly-portunif.jar:$dependlibs/glassfish-api.jar:$dependlibs/hk2-api.jar:$dependlibs/javax.transaction-api.jar:$dependlibs/jhall.jar:$dependlibs/fscontext.jar:$dependlibs/audit.jar:$dependlibs/bdb_je.jar

#
# Append CLASSPATH value to _classes if it is set.
#
if [ ! -z "$CLASSPATH" ]; then
    _classes=$imq_home/../../share/opt/classes:$_ext_classes:$CLASSPATH
    CLASSPATH=
    export CLASSPATH
else
    _classes=$imq_home/../../share/opt/classes:$_ext_classes
fi


_mainclass=com.sun.messaging.jmq.admin.apps.objmgr.ObjMgr

# Needed to locate libimq
#####hpux-dev#####
if [ "$PLATFORM" = HP-UX ] ; then
SHLIB_PATH=$SHLIB_PATH:$imq_home/lib; export SHLIB_PATH
else
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$imq_home/lib; export LD_LIBRARY_PATH
fi


$javacmd -cp $_classes $jvm_args $_mainclass "$@"
