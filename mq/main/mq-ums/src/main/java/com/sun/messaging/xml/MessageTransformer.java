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
 * @(#)MessageTransformer.java	1.9 07/02/07
 */ 
 
package com.sun.messaging.xml;

import java.util.*;
import java.io.*;

import javax.jms.Message;
import javax.jms.BytesMessage;
import javax.jms.Session;
import javax.jms.JMSException;

import javax.xml.messaging.*;
import javax.xml.soap.*;

/**
 * The <code>Transformer</code> class encapsulates the functionality
 * to transform SOAP and JMS messages.
 */
public class MessageTransformer {

    /** Private Constructor */
    private MessageTransformer() {}

    /**
    * Transforms a <code>javax.xml.soap.SOAPMessage</code> message
    * into a <code>javax.jms.Message</code> message.
    *
    * @param soapMessage the SOAPMessage to be converted to the JMS Message.
    * @param session The JMS Session to be used to construct the JMS Message.
    *
    * @exception JAXMException If any error is encountered when transforming the message.
    */
    public static Message
    SOAPMessageIntoJMSMessage (SOAPMessage soapMessage, Session session) throws JAXMException {

        try {
            /**
             * Construct a bytes message object.
             * This is to make sure the utility works across all vendors.
             */
            BytesMessage bmessage = session.createBytesMessage();

            /**
             * This is here to make sure that RI's bad SOAP implementation
             * will get updated for internal buffers.
             */
            soapMessage.saveChanges();

            /**
             * Write SOAP MIME headers.
             */
            writeMimeHeaders (soapMessage, bmessage);

            /**
             * write message body to byte array output stream.
             */
            writeSOAPBody (soapMessage, bmessage);

            return bmessage;

        } catch (JAXMException JAXMe) {
            throw JAXMe;
        } catch (Exception e) {
            e.printStackTrace();
            throw new JAXMException (e);
        }
    }

    /**
    * Extracts a <code>javax.xml.soap.SOAPMessage</code> object from the
    * <code>javax.jms.Message</code> object into which it was transformed
    * using the <code>SOAPMessageIntoJMSMessage</code> method.
    *
    * The <code>MessageFactory</code> parameter is used to construct the
    * <code>javax.xml.soap.SOAPMessage</code> object.
    * <p>
    * If <code>MessageFactory</code> is <code>null</code> then the
    * default SOAP MessageFactory will be used to construct the
    * SOAP message.
    *
    * @param message The JMS message from which the SOAP message is to be extracted.
    * @param messageFactory The SOAP MessageFactory to be used to contruct the SOAP message.
    *
    * @exception JAXMException If any error is encountered when extracting the message.
    */
    public static SOAPMessage
    SOAPMessageFromJMSMessage(Message message, MessageFactory messageFactory)
                                                throws JAXMException {

        SOAPMessage soapMessage = null;
        BytesMessage bmessage = (BytesMessage) message;

        try {

            //1. construct mime header
            int mimeLength = bmessage.readInt();
            byte[] mbuf = new byte [mimeLength];
            bmessage.readBytes(mbuf, mimeLength);

            ByteArrayInputStream mbin = new ByteArrayInputStream (mbuf);
            ObjectInputStream oi = new ObjectInputStream (mbin);
            Hashtable ht = (Hashtable) oi.readObject();
            MimeHeaders mimeHeaders = hashtableToMime (ht);

            //2. get soap body stream.
            int bodyLength = bmessage.readInt();
            byte[] buf = new byte [bodyLength];
            bmessage.readBytes(buf, bodyLength);

            ByteArrayInputStream bin = new ByteArrayInputStream (buf);

            if ( messageFactory == null ) {
                messageFactory = getMessageFactory ();
            }

            //3. construct soap message object.
            soapMessage = messageFactory.createMessage(mimeHeaders, bin );

        }catch (Exception e) {
            throw new JAXMException (e);
        }

        return soapMessage;
    }

    /**
     * Write MIME headers to JMS message body.
     */
    private static void
    writeMimeHeaders (SOAPMessage soapMessage, BytesMessage bmessage)
    throws Exception {

        /**
         * Convert JAXM MIME headers to Hashtable
         */
        MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
        Hashtable hashtable = MimeToHashtable (mimeHeaders);

        /**
         * Write hashtable to object output stream
         */
        ByteArrayOutputStream mimeOut = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream (mimeOut);
        oo.writeObject( hashtable );
        oo.flush();
        oo.close();

        /**
         * convert mime output stream to byte array.
         */
        byte[] mimebuf = mimeOut.toByteArray();

        /**
         * Write buffer length to JMS bytes message.
         */
        bmessage.writeInt(mimebuf.length);

        /**
         * Write header byte[] to JMS bytes message.
         */
        bmessage.writeBytes( mimebuf );

        /**
         * Close streams.
         */
        mimeOut.close();

        //System.out.println ("SOAP to JMS mime length: " + mimebuf.length);
    }

    /**
     * Write SOAP message body to JMS bytes message.
     */
    private static void
    writeSOAPBody (SOAPMessage soapMessage, BytesMessage bmessage) throws Exception {

        ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
        soapMessage.writeTo(bodyOut);

        /**
         * Convert byte array output stream to byte[].
         */
        byte[] buf = bodyOut.toByteArray();

        /**
         * Write message body byte[] length.
         */
        bmessage.writeInt(buf.length);

        /**
         * Write message body byte[] buffer.
         */
        bmessage.writeBytes(buf);

        bodyOut.close();

        //System.out.println ("SOAP to JMS body length: " + buf.length);
    }

    /**
     * Convert MimeHeaders to Hashtable.  The hashtable is then used to write
     * to JMS BytesMessage.
     */
    private static Hashtable
    MimeToHashtable ( MimeHeaders mimeHeaders ) {

        Hashtable hashtable = new Hashtable();
        Iterator it = mimeHeaders.getAllHeaders();

        while ( it.hasNext() ) {
            MimeHeader mh = (MimeHeader) it.next();
            hashtable.put(mh.getName(), mh.getValue());

            //System.out.println("name: " + mh.getName() + "  Val: " + mh.getValue());
        }

        return hashtable;
    }

    /**
     * Convert Hashtable to MimeHeaders.  Used when converting from JMS
     * to SOAP messages.
     */
    private static MimeHeaders
    hashtableToMime (Hashtable hashtable) {
        MimeHeaders mimeHeaders = new MimeHeaders();

        Enumeration enm = hashtable.keys();
        while ( enm.hasMoreElements() ) {
            Object key = enm.nextElement();
            mimeHeaders.addHeader((String)key, (String)hashtable.get(key));
        }

        return mimeHeaders;
    }

    /**
     * Get SOAP message factory from JMS message.
     * @param message JMS message.
     */
    private static MessageFactory
    getMessageFactory () throws SOAPException {
        return MessageFactory.newInstance();
    }

}
