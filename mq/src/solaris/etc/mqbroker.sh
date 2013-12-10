#!/sbin/sh
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
# Message Queue Broker Service method script
#
# This script starts the Message Queue broker service 
# within SMF.
#
# To debug this script, set the environmental variable
# DEBUG to 1 (true)
#
# e.g.
# env DEBUG=1 /lib/svc/method start
#
# This file is installed as:
#     /lib/svc/method start
#
# Note: To modify parameters passed to imqbrokerd, do not
# edit this script.  Instead, use svccfg(1m) to modify the
# SMF repository.  For example:
#
# # svccfg
# svc:> select svc:/application/sun/mq/mqbroker
# svc:/application/sun/mq/mqbroker> setprop options/broker_args="-loglevel DEBUGHIGH"
# svc:/application/sun/mq/mqbroker> exit
#
#

. /lib/svc/share/smf_include.sh

###### START LOCAL CONFIGURATION
# You may wish to modify these variables to suit your local configuration

# IMQ_HOME   Location of the Message Queue installation
IMQ_HOME=/
export IMQ_HOME

# IMQ_VARHOME
# Set IMQ_VARHOME if you wish to designate an alternate
# location for storing broker specific data like:
#	- persistent storage
#	- SSL certificates
#	- user passwd database
#
# IMQ_VARHOME=/var/opt/SUNWjmq
# export IMQ_VARHOME

# BROKER_OPTIONS	Default Options to pass to the broker executable
# additional arguments can be defined in the ARGS property
#
BROKER_OPTIONS="-silent"

#
# ETC HOME
IMQ_ETCHOME="/etc/imq"
#
# imqbrokerd.conf
CONF_FILE=$IMQ_ETCHOME/imqbrokerd.conf
#
#
###### END LOCAL CONFIGURATION

FMRI=svc:/application/sun/mq/mqbroker
EXECUTABLE=imqbrokerd


# error "description"
error () {
  echo $0: $* 2>&1
  exit $SMF_EXIT_ERROR_CONFIG
}

# find the named process(es)
findproc() {
  pid=`/usr/bin/ps -ef |
  /usr/bin/grep -w $1 | 
  /usr/bin/grep -v grep |
  /usr/bin/awk '{print $2}'`
  echo $pid
}

# kill the named process(es) (borrowed from S15nfs.server)
killproc() {
   pid=`findproc $1`
   [ "$pid" != "" ] && kill $pid
}

#
# require /usr/bin and Message Queue
#
if [ ! -d /usr/bin ]; then
   error "Cannot find /usr/bin."

elif [ ! -d "$IMQ_HOME" ]; then
   error "Cannot find Message Queue in IMQ_HOME ($IMQ_HOME)."
fi


#
# Start/stop Message Queue Broker
#
case "$1" in
'start')
         if [ -f $CONF_FILE ] ; then 
            ARGS=`grep -v "^#" $CONF_FILE | grep ARGS | cut -c6-`
         fi

	 # Overwrite args with one defined in service
	 # Change '\ ' to just ' '.
	 SVCARGS=`svcprop -p options/broker_args $FMRI`
	 if [ "$SVCARGS" != "\"\"" -a "$SVCARGS" != "" ]; then
             if [ $DEBUG ] ; then 
		echo "options/broker_args found - using $SVCARGS"
	     fi
	     ARGS=$SVCARGS
	 fi
	
         if [ $DEBUG ] ; then 
	     echo "Command \c"
	     echo "$IMQ_HOME/bin/$EXECUTABLE -bgnd $BROKER_OPTIONS  $ARGS... \c"
         fi
	 cd $IMQ_HOME

	 # Check if the server is already running.
	 if [ -n "`findproc $EXECUTABLE`" ]; then
             if [ $DEBUG ] ; then 
	        echo "$EXECUTABLE is already running."
             fi
	     exit $SMF_EXIT_OK
	 fi

	 # sh the command because it may contain '\' chars.
	 sh -c "bin/$EXECUTABLE -bgnd $BROKER_OPTIONS $ARGS" &
	
         if [ $DEBUG ] ; then 
  	     echo "done"
         fi

	;;

'stop')
        if [ $DEBUG ] ; then
	    echo "Stopping $EXECUTABLE ... \c"
        fi
	if [ -z "`findproc $EXECUTABLE`" ]; then
            if [ $DEBUG ] ; then
	        echo "$EXECUTABLE is not running."
            fi
	   exit $SMF_EXIT_OK
	fi
	killproc $EXECUTABLE
        if [ $DEBUG ] ; then
	    echo "done"
        fi
	;;

*)
	echo "Usage: $0 { start | stop }"
	;;
esac
