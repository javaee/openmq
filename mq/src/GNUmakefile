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

TOPDIR		= ./..
MAKEDEFS 	= $(TOPDIR)/src/buildcfg/Defs.gmk
INSTALLDEFS 	= $(TOPDIR)/src/buildcfg/Install.gmk
SUBDIRS 	= share $(ARCH)

include $(MAKEDEFS)
include $(INSTALLDEFS)

all optimized debug clean: create_links $(SUBDIRS) 

install:  doinstall
install-c:  doinstall-c
install-ri:  doinstall-ri
ips:  doipsstage
ias:  doias
fb: dofb 
fbv3: dofbv3
ias-l10n:  doias-l10n
l10n-install:  dol10n-install

create_links:
	@if [ $(PLATFORM) != Windows_NT -a $(PLATFORM) != SunOS ]; then \
	    rm -f $(PLATFORM); \
	    ln -s solaris $(PLATFORM); \
	fi;

#
# Any of these targets will build the C-API
# This target is mainly here for the windows build because
# we don't build C-API by default on windows since not everyone
# has the correct C compiler.
# For Solaris and Linux, this is built by default from the top.
#
c capi c-api cclient: do-c do-c64

do-c c32:
	cd '$(CCLIENTDIR)'; $(MAKE); 
	cd '$(CCLIENTDIR)'; $(MAKE) debug; 

clean-c:
	cd '$(CCLIENTDIR)'; $(MAKE) clean; 
	cd '$(CCLIENTDIR)'; $(MAKE) DATA_MODEL_64=1 clean; 

#
# Build 64-bit C-API libs on only.
#
do-c64 c64 capi64 c-api64 cclient64:
	cd '$(CCLIENTDIR)'; \
	$(MAKE) DATA_MODEL_64=1; \
	$(MAKE) DATA_MODEL_64=1 debug; \
#
# Makes an RI src tree from an MQ product workspace in TOP/src.
# This should only be run once on a clean MQ product workspace.
# Note: It will not restore your original TOP/src directory.
#
ri-src:
	@if [ $(PLATFORM) != Windows_NT ]; then \
	    /bin/sh $(TOPDIR)/src/buildcfg/tools/ri/scripts/j2ee_ri_src.sh; \
	fi;
#
# This make the RI src and binary bundles that we deliver 
# after:  gnumake ri-src  (build ri src tree)
#  	  gnumake (compile ri src and make ri install image)	  
#  	  gnumake ri-bundles (builds ri src and binary bundles.
#
ri-bundles: dori-bundles

# These targets below should be done AFTER an optimized (all) build is
# built and a 'gnumake install' image is done.
# The targets 'package', 'packages', 'bundles' are all the same.
# Do RPMs only on sparc and if the user is root or nakata or BUILD_RPMS=true
#   RPMs won't build for other users unless setup is correct in
#   $TOP/src/buildcfg/install/linux/GNUmakefile.
#
package packages bundles l10n-bundles: fb
	cd '$(PKGMAKEDIR)'; $(MAKE) $@ || exit 1; cd ..;
	@if [ "$(ARCH_TYPE)" = "sparc" ]; then \
	    cd '$(RPMMAKEDIR)'; $(MAKE) $@ || exit 1; cd ..; \
	fi;

bundles.installer: dofbv3
	cd '$(PKGMAKEDIR)'; $(MAKE) $@ || exit 1; cd ..;
	@if [ "$(ARCH_TYPE)" = "sparc" ]; then \
	    cd '$(RPMMAKEDIR)'; $(MAKE) $@ || exit 1; cd ..; \
	fi;

patches patch:
	cd '$(PATCHMAKEDIR)'; $(MAKE) $@ || exit 1; cd ..;
#
# The target 'eabundles' will make a tar.Z (solaris) file or a .zip (win32)
# of the install image and place the file in $TOP/dist/bundles.
# This target should be used after an optimized build is built and a 
# 'gnumake install' image is done.  The final tar.Z or .zip
# file can be used to distribute an Early Access/Alpha release
# until formal packaging/install shield bundles are in place.
#
eabundles: doeabundles

#
# Generate both public and private javadocs for JMQ.
# Output goes to src/doc/en/develop.
#
docs: 
	cd '$(_JAVA_SRC)'; $(MAKE) docs || exit 1; cd ..;

clobber:  doclobber
	cd '$(EXAMPLES_SRC)'; $(MAKE) clean || exit 1; cd ..;


#
# This target is only for RE to be used on Solaris10 x86 w/amd64 support
# Reason for this target:  Solaris x86 32-bit C libs need to be
# built on S9 while Solaris x86 64-bit (amd64) C libs needs to be
# built on S10.
#
# When using this target, it assumes you already built 32-bit C libs
# both libimqutil.so and libmqcrt.so on Solaris9.
# Then it saves the 32-bits libs on Solaris 9 away
# and builds on Solaris10 and saves the amd64 libs.
# Finally, it restores the S9 libs and objs.
#
# Note: This target is fragile and assume a bunch of
# files, libs, directory already exist.  If you get an
# error during this amd64 target, it is best to
# go back to the S9 system, do a clean and do a make.
# Then come back to this amd64 target on S10.
#
#
amd64:  
	@if [ "$(ARCH_TYPE)" = "i386" ]; then \
	   rm -rf $(TOPDIR)/binary/solaris/opt/obj/native/amd64; \
	   rm -rf $(TOPDIR)/binary/solaris/opt/lib/amd64/libimqutil.so.1; \
	   cd $(TOPDIR)/src/solaris; $(MAKE) c-lib64; cd ../; \
	fi; 

amd64-old:  
	@if [ "$(ARCH_TYPE)" = "i386" ]; then \
	   mv -f $(TOPDIR)/binary/solaris/opt/lib/libimqutil.so.1 $(TOPDIR)/binary/solaris/opt/lib/libimqutil.so.1-s9; \
	   mv -f $(TOPDIR)/binary/solaris/opt/lib/amd64/libimqutil.so.1 $(TOPDIR)/binary/solaris/opt/lib/amd64/libimqutil.so.1-s9; \
	   mv -f $(TOPDIR)/binary/solaris/opt/lib/libmqcrt.so.1 $(TOPDIR)/binary/solaris/opt/lib/libmqcrt.so.1-s9; \
	   mv -f $(TOPDIR)/binary/solaris/opt/lib/amd64/libmqcrt.so.1 $(TOPDIR)/binary/solaris/opt/lib/amd64/libmqcrt.so.1-s9; \
	   \
	   \
	   rm -rf $(TOPDIR)/binary/solaris/opt/obj/native-s9; \
	   rm -rf $(TOPDIR)/binary/solaris/opt/obj/cclient-s9; \
	   mv -f $(TOPDIR)/binary/solaris/opt/obj/native $(TOPDIR)/binary/solaris/opt/obj/native-s9; \
	   mv -f $(TOPDIR)/binary/solaris/opt/obj/cclient $(TOPDIR)/binary/solaris/opt/obj/cclient-s9; \
	   cd $(TOPDIR)/src/solaris/native; $(MAKE); cd ../; $(MAKE) c-lib64; cd ../; \
	   cd '$(CCLIENTDIR)'; $(MAKE); $(MAKE) DATA_MODEL_64=1; $(MAKE) DATA_MODEL_64=1 debug; cd ../..; \
	   cp -fp $(TOPDIR)/binary/solaris/opt/lib/libimqutil.so.1 $(TOPDIR)/binary/solaris/opt/lib/libimqutil.so.1-s10; \
	   cp -fp $(TOPDIR)/binary/solaris/opt/lib/amd64/libimqutil.so.1 $(TOPDIR)/binary/solaris/opt/lib/amd64/libimqutil.so.1-s10; \
	   cp -fp $(TOPDIR)/binary/solaris/opt/lib/libmqcrt.so.1 $(TOPDIR)/binary/solaris/opt/lib/libmqcrt.so.1-s10; \
	   cp -fp $(TOPDIR)/binary/solaris/opt/lib/amd64/libmqcrt.so.1 $(TOPDIR)/binary/solaris/opt/lib/amd64/libmqcrt.so.1-s10; \
	   \
	   \
	   rm -rf $(TOPDIR)/binary/solaris/opt/obj/native; \
	   rm -rf $(TOPDIR)/binary/solaris/opt/obj/cclient; \
	   mv -f $(TOPDIR)/binary/solaris/opt/obj/native-s9 $(TOPDIR)/binary/solaris/opt/obj/native; \
	   mv -f $(TOPDIR)/binary/solaris/opt/obj/cclient-s9 $(TOPDIR)/binary/solaris/opt/obj/cclient; \
	   mv -f $(TOPDIR)/binary/solaris/opt/lib/libimqutil.so.1-s9 $(TOPDIR)/binary/solaris/opt/lib/libimqutil.so.1; \
	   mv -f $(TOPDIR)/binary/solaris/opt/lib/amd64/libimqutil.so.1-s9 $(TOPDIR)/binary/solaris/opt/lib/amd64/libimqutil.so.1; \
	   mv -f $(TOPDIR)/binary/solaris/opt/lib/libmqcrt.so.1-s9 $(TOPDIR)/binary/solaris/opt/lib/libmqcrt.so.1; \
	   mv -f $(TOPDIR)/binary/solaris/opt/lib/amd64/libmqcrt.so.1-s9 $(TOPDIR)/binary/solaris/opt/lib/amd64/libmqcrt.so.1; \
	fi; 

findbugs: findbugs-report
