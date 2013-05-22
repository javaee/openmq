#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

PACKAGE     = cclient
TOPDIR		= ../../..
MAKEDEFS 	= $(TOPDIR)/src/buildcfg/Defs.gmk
SUBDIRS 	= error util basictypes containers net io serial client cshim 


DLL_NAME=mqcrt
VERSION_NUM=1

DLL_VERSIONING = for SunOS 
USE_PDB_FILE = for Windows_NT 

include $(MAKEDEFS)

include error/error_srcs.mk
include util/util_srcs.mk
include basictypes/basictypes_srcs.mk
include containers/containers_srcs.mk
include net/net_srcs.mk
include io/io_srcs.mk
include serial/serial_srcs.mk
include client/client_srcs.mk
include client/auth/auth_srcs.mk
include client/protocol/protocol_srcs.mk
include cshim/cshim_srcs.mk

FILES.o = $(addsuffix $(OBJECT_SUFFIX), \
			$(addprefix $(subst \,/,$(PKGDIR))/error/,      $(CCLIENT_ERROR_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/util/,       $(CCLIENT_UTIL_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/basictypes/, $(CCLIENT_BASICTYPES_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/containers/, $(CCLIENT_CONTAINERS_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/net/,        $(CCLIENT_NET_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/io/,         $(CCLIENT_IO_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/serial/,     $(CCLIENT_SERIAL_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/client/,     $(CCLIENT_CLIENT_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/client/auth/, $(CCLIENT_CLIENT_AUTH_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/client/protocol/, $(CCLIENT_CLIENT_PROTOCOL_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/cshim/,      $(CCLIENT_CSHIM_OBJS)))

ifdef DATA_MODEL_64
ifeq ($(PLATFORM), SunOS)
ifeq ($(ARCH_TYPE), sparc)
_64_LIBRARYNAME=sparcv9
endif
ifeq ($(ARCH_TYPE), i386)
_64_LIBRARYNAME=amd64
endif
endif
endif

#objs only go to 64 if DATA_MODEL_64 defined
#so let it fail if DATA_MODEL_64 not defined
ifdef DATA_MODEL_64
ifeq ($(PLATFORM), Linux)
_64_LIBRARYNAME=64
endif
endif

ifdef _64_LIBRARYNAME
FILES.o = $(addsuffix $(OBJECT_SUFFIX), \
			$(addprefix $(subst \,/,$(PKGDIR))/error/$(_64_LIBRARYNAME)/,      $(CCLIENT_ERROR_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/util/$(_64_LIBRARYNAME)/,       $(CCLIENT_UTIL_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/basictypes/$(_64_LIBRARYNAME)/, $(CCLIENT_BASICTYPES_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/containers/$(_64_LIBRARYNAME)/, $(CCLIENT_CONTAINERS_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/net/$(_64_LIBRARYNAME)/,        $(CCLIENT_NET_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/io/$(_64_LIBRARYNAME)/,         $(CCLIENT_IO_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/serial/$(_64_LIBRARYNAME)/,     $(CCLIENT_SERIAL_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/client/$(_64_LIBRARYNAME)/,     $(CCLIENT_CLIENT_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/client/auth/$(_64_LIBRARYNAME)/, $(CCLIENT_CLIENT_AUTH_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/client/protocol/$(_64_LIBRARYNAME)/, $(CCLIENT_CLIENT_PROTOCOL_OBJS)) \
			$(addprefix $(subst \,/,$(PKGDIR))/cshim/$(_64_LIBRARYNAME)/,      $(CCLIENT_CSHIM_OBJS)))
endif

CC = $(CCLIENT_CC)

#solaris
DLLFLAGS = $(CCLIENT_CPLUSFLAGS) $(CCLIENT_DLLFLAGS)
DEFAULT_DLLDEPENDS =
#windows
DLL_FLAGS = $(CCLIENT_DLLFLAGS)
DLL_FILES = $(FILES.o)
DLLDEPENDS =

DLL_LINKFLAGS = $(CCLIENT_DLL_LINKFLAGS)
DLL_LIBS = $(CCLIENT_DLL_LIBS)
DLL_DEPENDS = $(FILES.o)


#
#public headers to publish
#
ifeq ($(PLATFORM), Windows_NT)
_PUBLIC_HEADERS_CP = $(addsuffix ', $(addprefix 'cshim\, $(CCLIENT_PUBLIC_HEADERS)))
else
_PUBLIC_HEADERS_CP = $(addprefix cshim/, $(CCLIENT_PUBLIC_HEADERS))
endif

#
#NSPR and NSS libraries to publish
#
ifeq ($(PLATFORM), Windows_NT)
_NSPR_LIBS_opt = libnspr4.dll libnspr4.lib libplc4.dll libplc4.lib libplds4.dll libplds4.lib
_NSS_LIBS_opt = freebl3.dll nss3.dll nss3.lib nssckbi.dll nssdbm3.dll nssutil3.dll nssutil3.lib smime3.dll smime3.lib softokn3.dll sqlite3.dll ssl3.dll ssl3.lib
_NSPR_LIBS_debug = libnspr4.dll libnspr4.lib libplc4.dll libplc4.lib libplds4.dll libplds4.lib libnspr4.pdb libplds4.pdb libplc4.pdb
_NSS_LIBS_debug = freebl3.dll nss3.dll nss3.lib nssckbi.dll nssdbm3.dll nssutil3.dll nssutil3.lib smime3.dll smime3.lib softokn3.dll sqlite3.dll ssl3.dll ssl3.lib freebl3.pdb nssckbi.pdb nssutil3.pdb softokn3.pdb ssl3.pdb nss3.pdb nssdbm3.pdb smime3.pdb sqlite3.pdb
_NSPR_LIBS = $(_NSPR_LIBS_$(VARIANT))
_NSS_LIBS = $(_NSS_LIBS_$(VARIANT))
_NSS_CHK_FILES = freebl3.chk softokn3.chk nssdbm3.chk 
endif

ifeq ($(PLATFORM), Linux)
_NSPR_LIBS = libnspr4.so libplc4.so libplds4.so
_NSS_LIBS =  libfreebl3.so libnss3.so libnssckbi.so libnssdbm3.so libnssutil3.so libsmime3.so libsoftokn3.so libsqlite3.so libssl3.so
_NSS_CHK_FILES = libfreebl3.chk libsoftokn3.chk libnssdbm3.chk
endif

ifeq ($(PLATFORM), HP-UX)
_NSPR_LIBS = libnspr4.sl  libplc4.sl   libplds4.sl
_NSS_LIBS = libfreebl_32fpu_3.sl libfreebl_32int_3.sl libnss3.sl libnssckbi.sl libnssdbm3.sl libnssutil3.sl libsmime3.sl libsoftokn3.sl libsqlite3.sl libssl3.sl
_NSS_CHK_FILES = libfreebl_32fpu_3.chk libfreebl_32int_3.chk libsoftokn3.chk libnssdbm3.chk
endif

ifeq ($(PLATFORM),  AIX)
_NSPR_LIBS = libnspr4.so  libplc4.so   libplds4.so
_NSS_LIBS = libfreebl3.so libnss3.so libnssckbi.so libnssdbm3.so libnssutil3.so libsmime3.so libsoftokn3.so libsqlite3.so libssl3.so
_NSS_CHK_FILES = libfreebl3.chk libsoftokn3.chk libnssdbm3.chk
endif

ifeq ($(PLATFORM), SunOS)	
_NSPR_LIBS = libnspr4.so libplc4.so libplds4.so
ifeq ($(ARCH_TYPE), sparc)	
ifndef DATA_MODEL_64
_NSS_LIBS = libfreebl_32fpu_3.so libfreebl_32int64_3.so libfreebl_32int_3.so libnss3.so libnssckbi.so libnssdbm3.so libnssutil3.so libsmime3.so libsoftokn3.so libsqlite3.so libssl3.so
_NSS_CHK_FILES = libfreebl_32fpu_3.chk libfreebl_32int64_3.chk libfreebl_32int_3.chk libsoftokn3.chk libnssdbm3.chk
_NSPR_FLT4_DIR = cpu/sparcv8plus
_NSPR_FLT4_LIB = libnspr_flt4.so
else
_NSS_LIBS = libfreebl_64fpu_3.so libfreebl_64int_3.so libnss3.so libnssckbi.so libnssdbm3.so libnssutil3.so libsmime3.so libsoftokn3.so libsqlite3.so libssl3.so
_NSS_CHK_FILES = libfreebl_64fpu_3.chk libfreebl_64int_3.chk libsoftokn3.chk libnssdbm3.chk
endif
else
_NSPR_LIBS = libnspr4.so libplc4.so libplds4.so
_NSS_LIBS = libfreebl3.so libnss3.so libnssckbi.so libnssdbm3.so libnssutil3.so libsmime3.so libsoftokn3.so libsqlite3.so libssl3.so
_NSS_CHK_FILES = libfreebl3.chk libsoftokn3.chk libnssdbm3.chk
endif
endif

#
#NSS utilities to publish (sync with SUNWtlsu)
#
__NSS_TOOLS = addbuiltin certutil cmsutil crlutil modutil pk12util signtool signver ssltap


ifeq ($(PLATFORM), Windows_NT)	
_NSS_TOOLS = $(addsuffix .exe, $(__NSS_TOOLS))
_NSPR_LIBS_CP = $(addprefix $(subst \,/,$(NSPR_LIB)/), $(_NSPR_LIBS))
_NSS_LIBS_CP = $(addprefix $(subst \,/,$(NSS_LIB)/), $(_NSS_LIBS))
_NSS_TOOLS_CP = $(addprefix $(subst \,/,$(NSS_EXE)/), $(_NSS_TOOLS))
_NSS_CHK_FILES_CP = $(addprefix $(subst \,/,$(NSS_LIB)/), $(_NSS_CHK_FILES))
else
_NSS_TOOLS = $(__NSS_TOOLS)
_NSPR_LIBS_CP = $(addprefix $(NSPR_LIB)/, $(_NSPR_LIBS))
_NSS_LIBS_CP  = $(addprefix $(NSS_LIB)/, $(_NSS_LIBS))
_NSS_TOOLS_CP  = $(addprefix $(NSS_EXE)/, $(_NSS_TOOLS))
_NSS_CHK_FILES_CP  = $(addprefix $(NSS_LIB)/, $(_NSS_CHK_FILES))
ifdef _NSPR_FLT4_LIB
_NSPR_FLT4_LIB_CP  = $(addprefix $(NSPR_LIB)/$(_NSPR_FLT4_DIR)/, $(_NSPR_FLT4_LIB))
endif
endif


_PUBLISH_LIBDIR = $(LIBDIR)
_PUBLISH_EXEDIR = $(EXEDIR)
ifdef DATA_MODEL_64
_PUBLISH_LIBDIR = $(LIBDIR_64)
_PUBLISH_EXEDIR = $(EXEDIR_64)
endif

##XXX
local_publish:
	@if [ ! -d '$(INSTALLINCL)' ]; then \
		$(MKDIR) '$(INSTALLINCL)';  \
	fi
	@if [ ! -d '$(_PUBLISH_LIBDIR)' ]; then \
		$(MKDIR) '$(_PUBLISH_LIBDIR)';  \
	fi
	@if [ ! -d '$(_PUBLISH_EXEDIR)' ]; then \
		$(MKDIR) '$(_PUBLISH_EXEDIR)';  \
	fi
	@$(ECHO) "Copy MQ C-API public headers: $(_PUBLIC_HEADERS_CP)"
	for i in $(_PUBLIC_HEADERS_CP); do \
	$(CP) $(CPFLAGS)  $$i '$(INSTALLINCL)'; \
	done
#	@$(ECHO) "Copy NSPR headers ... "
#	$(MKDIR_EXE) $(subst \,/,$(INSTALLINCL))/nspr
#	$(CP_EXE_R) $(subst \,/,$(NSPR_INCLUDE))/* '$(subst \,/,$(INSTALLINCL))/nspr'
	@$(ECHO) "Copy NSPR and NSS libraries ... "
	$(CP_EXE) $(_NSPR_LIBS_CP) $(_NSS_LIBS_CP) '$(subst \,/,$(_PUBLISH_LIBDIR))'
#should only for Solaris
ifdef _NSPR_FLT4_LIB_CP
	@if [ ! -d '$(_PUBLISH_LIBDIR)/$(_NSPR_FLT4_DIR)' ]; then \
		$(MKDIR) '$(_PUBLISH_LIBDIR)/$(_NSPR_FLT4_DIR)';  \
	fi
	$(CP_EXE) $(_NSPR_FLT4_LIB_CP) '$(subst \,/,$(_PUBLISH_LIBDIR)/$(_NSPR_FLT4_DIR))'
endif
	@$(ECHO) "Copy NSS .chk files ... "
	$(CP_EXE) $(_NSS_CHK_FILES_CP) '$(subst \,/,$(_PUBLISH_LIBDIR))'
	@$(ECHO) "Copy NSS Utilities ... "
	$(CP_EXE) $(_NSS_TOOLS_CP) '$(subst \,/,$(_PUBLISH_EXEDIR))'


local_publish.clean:
	@$(ECHO) "Removing MQ C-API public headers ..."
ifeq ($(PLATFORM), Windows_NT)
	$(RM) $(RMFLAGS) $(addsuffix ', $(addprefix '$(INSTALLINCL)\, $(CCLIENT_PUBLIC_HEADERS)))
	$(RM) $(RMFLAGS) $(subst opt,debug,$(addsuffix ', $(addprefix '$(INSTALLINCL)\, $(CCLIENT_PUBLIC_HEADERS))))
else
	$(RM) $(RMFLAGS) $(addprefix $(INSTALLINCL)/, $(CCLIENT_PUBLIC_HEADERS))
	$(RM) $(RMFLAGS) $(subst opt,debug,$(addprefix $(INSTALLINCL)/, $(CCLIENT_PUBLIC_HEADERS)))
endif
#	@$(ECHO) "Removing NSPR headers ..."
#	$(RM_EXE_R) $(subst \,/,$(INSTALLINCL))/nspr
#	$(RM_EXE_R) $(subst opt,debug,$(subst \,/,$(INSTALLINCL))/nspr)  	 
#
	@$(ECHO) "Removing NSPR libraries ..."
	$(RM_EXE) $(addprefix $(subst \,/,$(_PUBLISH_LIBDIR))/, $(_NSPR_LIBS)) $(subst opt,debug, $(addprefix $(subst \,/,$(_PUBLISH_LIBDIR))/, $(_NSPR_LIBS)))
ifdef _NSPR_FLT4_LIB_CP
	$(RM_EXE) $(addprefix $(subst \,/,$(_PUBLISH_LIBDIR)/$(_NSPR_FLT4_DIR)/, $(_NSPR_FLT4_LIB)), $(subst opt,debug, $(addprefix $(subst \,/,$(_PUBLISH_LIBDIR)/$(_NSPR_FLT4_DIR))/, $(_NSPR_FLT4_LIB))))
endif
#
	@$(ECHO) "Removing NSS libraries ..."
	$(RM_EXE) $(addprefix $(subst \,/,$(_PUBLISH_LIBDIR))/, $(_NSS_LIBS)) $(subst opt,debug, $(addprefix $(subst \,/,$(_PUBLISH_LIBDIR))/, $(_NSS_LIBS)))

	@$(ECHO) "Removing NSS .chk Files ..."
	$(RM_EXE) $(addprefix $(subst \,/,$(_PUBLISH_LIBDIR))/, $(_NSS_CHK_FILES)) $(subst opt,debug, $(addprefix $(subst \,/,$(_PUBLISH_LIBDIR))/, $(_NSS_CHK_FILES)))
#
	@$(ECHO) "Removing NSS utilities ..."
	$(RM_EXE) $(addprefix $(subst \,/,$(_PUBLISH_EXEDIR))/, $(_NSS_TOOLS)) $(subst opt,debug, $(addprefix $(subst \,/,$(_PUBLISH_EXEDIR))/, $(_NSS_TOOLS)))
##

all optimized:  make_nssdir $(SUBDIRS) $(DLL) local_publish
debug: subdirs.debug dll.debug
clean: subdirs.clean dll.clean local_publish.clean

#
# The gcc linker -rpath <dir> option on linux
# requires the <dir> to exist on the system so
# making nss/lib the directory so that we can add 
# -rpath ../../nss/lib to the linker.
#
make_nssdir:
	@if [ $(PLATFORM) = "Linux" ]; then \
	    echo "Making $(TOPDIR)/src/nss/lib"; \
	    mkdir -p $(TOPDIR)/src/nss/lib; \
	fi;
