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
 * @(#)ReadWritePacket.java	1.28 06/27/07
 */ 

package com.sun.messaging.jmq.io;

import java.util.Hashtable;
import java.io.*;

/**
 * This class is an ecapsulation of a JMQ packet.
 */
public class ReadWritePacket extends ReadOnlyPacket {

    public ReadWritePacket() {
	super();
    }

    /**
     * Read packet from an InputStream. This method reads one packet
     * from the InputStream and sets the state of this object to
     * reflect the packet read.
     *
     * @param is        the InputStream to read the packet from
     */
    public synchronized void readPacket(InputStream is)
	throws IOException, EOFException {

	// Read packet into internal buffers
	super.readPacket(is);
    }


    /**
     * Write the packet to the specified OutputStream
     */
    public synchronized void writePacket(OutputStream os)
	throws IOException {
        super.writePacket(os);
    }

    /**
     * Update the timestamp on the packet. If you do this
     * you should call generateTimestamp(false) before writing the
     * packet, otherwise the timestamp will be overwritten when
     * writePacket() is called.
     */
    public synchronized void updateTimestamp() {
        super.updateTimestamp();
    }

    /**
     * Update the sequence number on the packet. If you do this
     * you should call generateSequenceNumber(false) before writing the
     * packet, otherwise the sequence number will be overwritten when
     * writePacket() is called.
     */
    public synchronized void updateSequenceNumber() {
        super.updateSequenceNumber();
    }

    /** 
     * Set the packet type.
     *
     * @param    new_packetType    The type of packet
     */
    public synchronized void setPacketType(int pType) {
	super.setPacketType(pType);
    }

    public synchronized void setTimestamp(long t) {
	super.setTimestamp(t);
    }

    public synchronized void setExpiration(long e) {
	super.setExpiration(e);
    }

    public synchronized void setPort(int p) {
	super.setPort(p);
    }

    public synchronized void setIP(byte[] ip) {
    super.setIP(ip);
    }

    public synchronized void setIP(byte[] ip, byte[] mac) {
	super.setIP(ip, mac);
    }

    public synchronized void setSequence(int n) {
    super.setSequence(n);
    }

    // Version should be VERSION1, VERSION2 or VERSION3. Default is VERSION3
    public synchronized void setVersion(int n) {
        super.setVersion(n);
    }

    public synchronized void setTransactionID(long n) {
	super.setTransactionID(n);
    }

    public synchronized void setEncryption(int e) {
	super.setEncryption(e);
    }

    public synchronized void setPriority(int p) {
	super.setPriority(p);
    }

    public synchronized void setFlag(int flag, boolean on) {
        super.setFlag(flag, on);
    }

    public synchronized void setProducerID(long l) {
        super.setProducerID(l);
    }

    public synchronized void setDestination(String d) {
	super.setDestination(d);
    }

    public synchronized void setDestinationClass(String d) {
    super.setDestinationClass(d);
    }

    public synchronized void setMessageID(String id) {
    super.setMessageID(id);
    }

    public synchronized void setCorrelationID(String id) {
	super.setCorrelationID(id);
    }

    public synchronized void setReplyTo(String r) {
	super.setReplyTo(r);
    }

    public synchronized void setReplyToClass(String r) {
	super.setReplyToClass(r);
    }

    public synchronized void setMessageType(String t) {
	super.setMessageType(t);
    }

    /**
     * Set the message properties.
     * WARNING! The Hashtable is NOT copied.
     *
     * @param    body    The message body.
     */
    public synchronized void setProperties(Hashtable props) {
	super.setProperties(props);
    }

    /**
     * Set the message body.
     * WARNING! The byte array is NOT copied.
     *
     * @param    body    The message body.
     */
    public synchronized void setMessageBody(byte[] body) {
    super.setMessageBody(body);
    }

    /**
     * Set the message body. Specify offset and length of where to take
     * data from buffer.
     * WARNING! The byte array is NOT copied.
     *
     * @param    body    The message body.
     */
    public synchronized void setMessageBody(byte[] body, int off, int len) {
    super.setMessageBody(body, off, len);
    }

    /**
     * Get the length of the message body
     *
     * @return Legnth of the message body in bytes
     */
    public synchronized int getMessageBodyLength() {
        return getMessageBodySize();
    }
    public synchronized int getMessageBodySize() {
        return super.getMessageBodySize();
    }

    /**
     * Get the offset into the message body buffer where the message
     * body data starts
     *
     * @return Byte offset into buffer returned by getMessageBody where
     *         message body data starts.
     *
     */
    public synchronized int getMessageBodyOffset() {
        return 0;
    }

    /**
     * Return the message body.
     * WARNING! This returns a reference to the message body, not a copy.
     * Also, if the body was set using setMessageBody(buf, off, len) then
     * you will get back the buffer that was passed to setMessageBody().
     * Therefore you may need to use getMessageBodyOffset() and
     * getMessageBodyLength() to determine the true location of the 
     * message body in the buffer.
     *
     * @return     A byte array containing the message body. 
     *		   null if no message body.
     */ 
    public synchronized byte[] getMessageBody() {
	return super.getMessageBodyByteArray();
    }

    /**
     * Make a shallow copy of this packet
     */
     public Object cloneShallow() {
         try {
             ReadWritePacket rp = new ReadWritePacket();
             rp.fill(this);
             return rp;
         } catch (IOException ex) {
             return null;
         }
     }

    /**
     * Make a deep copy of this packet
     */
    public Object clone() {
         try {
             ReadWritePacket rp = new ReadWritePacket();
             rp.fill(this, true);
             return rp;
         } catch (IOException ex) {
             return null;
         }
    }

    /**
     * Reset state of packet to initial values
     */
    public synchronized void reset() {
	    super.reset();
    }

    /* 
     * Dump the contents of the packet in human readable form to
     * the specified OutputStream.
     *
     * @param    os    OutputStream to write packet contents to
     */
    public void dump(PrintStream os) {
	super.dump(os);
    }
}
