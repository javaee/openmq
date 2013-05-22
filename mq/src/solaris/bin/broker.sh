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
# Broker startup script: Developer Edition
#
# This is a a version of the broker startup script that works when
# run in the "binary" directory (as opposed to "dist"). It uses the
# loose class files and not the jars
#
# Parse Arguments 
#
#  -jmqhome -> sets jmq home
#  -jmqvarhome -> sets jmq var home
#  -javahome -> sets javahome
#

#def_jvm_args="-Djava.compiler=NONE -Xms192m -Xmx192m -Xss192k";
def_jvm_args="-Xms192m -Xmx192m -Xss192k";
# We will add vendor-specific JVM flags below

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
    -jmqhome) shift; imq_home=$1; shift ;;
    -jmqvarhome) shift; imq_varhome=$1 ; jvm_args="$jvm_args -Dimq.varhome=$imq_varhome"; shift  ;;
    -varhome) shift; imq_varhome=$1 ; jvm_args="$jvm_args -Dimq.varhome=$imq_varhome"; shift  ;;
    -javahome) shift;  javahome=$1; shift;;
    -jrehome) shift; javahome=$1; shift;;
    -vm) shift; jvm_args="$jvm_args $1"; shift ;;
    -vmargs) shift; jvm_args="$jvm_args $1"; shift ;;
    -managed) shift ;; #ignore JMSRA testing
    *)  args="$args $1"; shift  ;;
  esac
done

javacmd=$javahome/bin/$javacmd

# Add  vendor-specific JVM flags
$javacmd -version 2>&1 | grep JRockit > /dev/null
if [ $? -eq 0 ]; then
   # Add JRockit-specific JVM flags
   jvm_args="$jvm_args -XgcPrio:deterministic"
else
   # Add Sun-specific JVM flags
   jvm_args="$jvm_args -XX:MaxGCPauseMillis=5000"
fi

jvm_args="$def_jvm_args $jvm_args -Dimq.home=$imq_home"

_classes=$dependlibs/javax.jms-api.jar:$imq_home/../../share/opt/classes:$dependlibs/grizzly-framework.jar:$dependlibs/grizzly-portunif.jar:$dependlibs/glassfish-api.jar:$dependlibs/hk2-api.jar:$dependlibs/javax.transaction-api.jar:$dependlibs/jhall.jar:$dependlibs/fscontext.jar:$dependlibs/audit.jar:$dependlibs/bdb_je.jar

# Additional classes possibly needed for JDBC provider
_classes=$_classes:$imq_home/lib/ext
# Put all jar and zip files in $imq_home/lib/ext in the classpath
for file in $imq_home/lib/ext/*.jar $imq_home/lib/ext/*.zip; do
    if [ -r "$file" ]; then
	_classes=$_classes:$file
    fi
done

# Needed to locate libimq
#####hpux-dev#####
if [ "$PLATFORM" = HP-UX ] ; then
SHLIB_PATH=$SHLIB_PATH:$imq_home/lib; export SHLIB_PATH
else
LD_LIBRARY_PATH=$imq_home/lib:$LD_LIBRARY_PATH; export LD_LIBRARY_PATH
fi

# Restart loop. If the Broker exits with 255 then restart it
restart=true
while [ $restart ]; do
    $javacmd -cp $_classes $jvm_args com.sun.messaging.jmq.jmsserver.Broker $args
    status=$?

    if [ $status -eq 255 ]; then
	# We pause to avoid pegging system if we get here accidentally
	sleep 1
    else
	restart=
    fi
done

exit $status
