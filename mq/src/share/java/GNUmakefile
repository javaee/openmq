#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2003-2010 Oracle and/or its affiliates. All rights reserved.
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

PACKAGE		= 
TOPDIR          = ../../..
MAKEDEFS 	= $(TOPDIR)/src/buildcfg/Defs.gmk
DOCDEFS 	= $(TOPDIR)/src/buildcfg/Doc.gmk
SUBDIRS         = 

include $(MAKEDEFS)
include $(DOCDEFS)

FILES.jar = jms.jar \
	    imq.jar \
	    imqadmin.jar 
#
# - The jar files are only created at this level
#   because at this point all the necessary files
#   that go into the jar file should be compiled.
# - Examples need to be built after jar file is built (classes.jar).
# - Only the external javadocs are built with the all target.
#
all optimized : $(SUBDIRS) manifest web-inf rar-meta-inf properties_files.copy classes.jar .examples.opt javadocs 

debug : $(SUBDIRS) classes_debug.jar .examples.debug javadocs 

#
# 'clean' target at this level cleans classes, jar files,
# examples, and javadocs.
# Anything below this level will only clean jar files.
#
clean : $(SUBDIRS) jars.clean docs.clean .examples.clean

#
# This docs target will built both internal and external javadocs.
#
docs  : javadocs 

.examples.clean:
	@if [ $(BUILD_TYPE) = RI ]; then \
	    echo " "; \
	else \
	    cd examples; $(MAKE) clean; \
	fi
.examples.opt: 
	@if [ $(BUILD_TYPE) = RI ]; then \
	    echo " "; \
	else \
	    cd examples; $(MAKE); \
	fi
.examples.debug: 
	@if [ $(BUILD_TYPE) = RI ]; then \
	    echo " "; \
	else \
	    cd examples; $(MAKE) debug; \
	fi

