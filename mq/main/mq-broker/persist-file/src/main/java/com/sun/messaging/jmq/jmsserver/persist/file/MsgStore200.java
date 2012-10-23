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
 * @(#)MsgStore200.java	1.7 06/29/07
 */ 

package com.sun.messaging.jmq.jmsserver.persist.file;

import com.sun.messaging.jmq.io.SysMessageID;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.core.ConsumerUID;
import com.sun.messaging.jmq.jmsserver.util.*;
import com.sun.messaging.jmq.jmsserver.Broker;
import com.sun.messaging.jmq.jmsserver.persist.api.Store;

import java.io.*;
import java.util.*;

/**
 * MsgStore200 is a simple class to load and return messages and the
 * associated interest lists from a version 200 message store.
 *
 * A version 200 message store is
 * - a directory of files, one message per file with numeric file names
 *
 * usage:
 * MsgStore200 oldmsgstore = new MsgStore200(File msgdir);
 * while (oldmsgstore.hasMoreMessages()) {
 *     Packet msg = oldmsgstore.nextMessage();
 *     ConsumerUID[] iids = oldmsgstore.nextCUIDs();
 *     int[] states = oldmsgstore.nextStates();
 *
 *     // do something with the message and it's interest list
 * }
 * oldmsgstore.close();
 */
class MsgStore200 extends RandomAccessStore {

    // if hasMoreMessages() returns true, the following variables
    // holds the next message and it's interest list
    private Packet msgToReturn = null;
    private ConsumerUID[] iidsToReturn = null;
    private int[] statesToReturn = null;

    private Enumeration msgenum = null;

    // accept everything
    private static FilenameFilter filenameFilter = new FilenameFilter() {
	public boolean accept(File dir, String name) {
	    return true;
	}
    };

    /**
     * When instantiated, all messages are loaded.
     */
    MsgStore200(File msgDir) throws BrokerException {

	super(msgDir, 0, 0, 0);
	msgenum = getEnumeration(false);	// false->not peekonly

	if (Store.getDEBUG()) {
	    logger.log(logger.DEBUG, "Loading version 200 message store");
	}
    }

    //
    //  Implement Enumeration methods
    //
    boolean hasMoreMessages() {
	return msgenum.hasMoreElements();
    }

    Packet nextMessage() {
	return msgToReturn;
    }

    ConsumerUID[] nextCUIDs() {
	return iidsToReturn;
    }

    int[] nextStates() {
	return statesToReturn;
    }

    // implement super class abstract method
    /**
     * parse the message and it's associated interest list from
     * the given buffers.
     * Returns the sysMessageID.
     */
    Object parseData(byte[] data, byte[] attachment) throws IOException {

	// parse message
	ByteArrayInputStream bais = new ByteArrayInputStream(data);
	msgToReturn = new Packet(false);
        msgToReturn.generateTimestamp(false);
        msgToReturn.generateSequenceNumber(false);
	msgToReturn.readPacket(bais);
	bais.close();

	int size = 0;
	// parse interest list
	if (attachment != null && attachment.length > 0) {

	    ByteArrayInputStream bis = new ByteArrayInputStream(attachment);
	    DataInputStream dis = new DataInputStream(bis);

	    // read in number of entries
	    size = dis.readInt();
	    iidsToReturn = new ConsumerUID[size];
	    statesToReturn = new int[size];

	    for (int i = 0; i < size; i++) {
		iidsToReturn[i] = new ConsumerUID(dis.readLong()); 
		statesToReturn[i] = dis.readInt();
	    }
	    dis.close();
	    bis.close();
	}

	SysMessageID mid = msgToReturn.getSysMessageID();

	if (Store.getDEBUG()) {
	    logger.log(logger.DEBUG,
		"loaded " + mid + " with " + size + " interest states");
	}

	return mid;
    }

    // print out informational message
    protected void close() {
	super.close(true);
    }

    FilenameFilter getFilenameFilter() {
	return filenameFilter;
    }
}

