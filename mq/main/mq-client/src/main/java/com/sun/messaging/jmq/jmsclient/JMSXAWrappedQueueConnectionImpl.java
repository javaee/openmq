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
 * @(#)JMSXAWrappedQueueConnectionImpl.java	1.4 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import java.util.Vector;
import javax.jms.*;
import com.sun.jms.spi.xa.*;

/** An XAConnection is an active connection to a JMS provider.
  * A client uses an XAConnection to create one or more XASessions
  * for producing and consuming messages.
  *
  * @see javax.jms.XAConnectionFactory
  * @see javax.jms.XAConnection
  */

public class JMSXAWrappedQueueConnectionImpl implements JMSXAQueueConnection {

    private final static boolean debug = JMSXAWrappedConnectionFactoryImpl.debug;
    private Connection wrapped_connection;;

    private JMSXAWrappedConnectionFactoryImpl wcf_ = null;;
    private String username_ = null;
    private String password_ = null;

    private Vector sessions_ = new Vector();
    private boolean markClosed_ = false;
    private boolean closed_ = false;

    /** private constructor - disallow null constructor */
    private JMSXAWrappedQueueConnectionImpl() {}


    public JMSXAWrappedQueueConnectionImpl(QueueConnection qconn, 
                                      JMSXAWrappedConnectionFactoryImpl wcf, 
                                      String username, String password) throws JMSException {
        wrapped_connection = qconn;
        this.wcf_ = wcf;
        this.username_ = username;
        this.password_ = password;
    }

    /**
     * Create an XAQueueSession
     *  
     * @param transacted      ignored.
     * @param acknowledgeMode ignored.
     *  
     * @return a newly created XA topic session.
     *  
     * @exception JMSException if JMS Connection fails to create a
     *                         XA topic session due to some internal error.
     */ 
    public JMSXAQueueSession createXAQueueSession(boolean transacted,
                                                int acknowledgeMode) throws JMSException {
        synchronized(sessions_) {

        if (closed_)  {
            throw new javax.jms.IllegalStateException("JMSXWrapped Connection has been closed");
        }

        if (markClosed_)  {
            throw new javax.jms.IllegalStateException("JMSXAWrapped Connection is closed");
        }

        JMSXAQueueSession s = (JMSXAQueueSession) (new JMSXAWrappedQueueSessionImpl(
                                                         (QueueConnection)wrapped_connection,
                                                          transacted, acknowledgeMode, this));

        if (((JMSXAWrappedQueueSessionImpl)s).delaySessionClose()) sessions_.add(s);

        return s;
        }
    }


    /**
     * get a QueueConnection associated with this XAQueueConnection object.
     *  
     * @return a QueueConnection.
     */ 
    public QueueConnection getQueueConnection() {
        return (QueueConnection) wrapped_connection;
    }

    public void close() throws JMSException {
        dlog("closing "+wrapped_connection+" "+wrapped_connection.getClass().getName());
        synchronized(sessions_) {
           if (sessions_.isEmpty()) {
               closed_ = true;
           } else {
               markClosed_ = true; 
           }
        }
        if (closed_) {
           hardClose();
        }
    }

    private void hardClose() throws JMSException {
        dlog("hard closing "+wrapped_connection+" "+wrapped_connection.getClass().getName());
        wrapped_connection.close();
        closed_ = true;
        dlog("hard closed "+wrapped_connection+" "+wrapped_connection.getClass().getName());
    }

    protected void removeSession(JMSXAWrappedQueueSessionImpl s) {
        synchronized(sessions_) {
           sessions_.remove(s);
           if (sessions_.isEmpty() && markClosed_) {
               dlog("All sessions closed, hard close connection "
                   +wrapped_connection+" "+wrapped_connection.getClass().getName());
               closed_ = true;
            }
        }
        if (closed_) { 
           try {
           hardClose();
           } catch (JMSException e) {
           log("Warning:", e);
           }
        }
    }

    protected JMSXAConnectionFactory getJMSXAWrappedConnectionFactory() {
        return wcf_;
    }

    protected String getUsername() {
        return username_;
    }

    protected String getPassword() {
        return password_;
    }

    private final static void dlog(String msg) {
        if (debug) log("Info:", msg);
    }

    private final static void dlogStack(Exception e) {
        if (debug) e.printStackTrace();
    }

    private final static void log(String level, Exception e) {
        log(level, e.getMessage());
        e.printStackTrace();
    }
    private final static void log(String level, String msg) {
        System.out.println(level+ " "+"JMSXAWrappedQueueConnectionImpl: " + msg);
    }

}
