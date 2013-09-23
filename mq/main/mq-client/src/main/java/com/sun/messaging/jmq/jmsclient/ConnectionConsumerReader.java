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
 * @(#)ConnectionConsumerReader.java	1.3 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient;

import javax.jms.*;
import com.sun.messaging.jmq.io.*;
import java.io.*;

/**
 * A ConnectionConsumerReader reads off messages from the connection
 * consumer's Read Queue and delivers the messages to the connection
 * consumer which will load the messages to ServerSessions
 */

//XXX REVISIT
public class ConnectionConsumerReader extends ConsumerReader {
    private ConnectionConsumerImpl connectionConsumer = null;
    private int load = 0;
    private int maxMessages;

    public ConnectionConsumerReader(ConnectionConsumerImpl connectionConsumer) {
        super(connectionConsumer.getConnection(), 
              connectionConsumer.getReadQueue());

       this.connectionConsumer = connectionConsumer;
       maxMessages = connectionConsumer.getMaxMessages();
       if (maxMessages < 1) { //should not happen
           maxMessages = 1;
       }
       load = 0;
    }

    protected void deliver(ReadOnlyPacket packet) throws IOException, JMSException {
        MessageImpl message = protocolHandler.getJMSMessage(packet);
        if (maxMessages == 1) {
            connectionConsumer.onMessage(message);
            connectionConsumer.startServerSession();
        }
		else {
            if (load == 0) {
                //'sessionQueue' really should named as 'readQueue'
                load = sessionQueue.size() + 1;
                if (load > maxMessages) {
                    load = maxMessages;
                }
            }
            connectionConsumer.onMessage(message);
            load--;
            if (load == 0) {
                connectionConsumer.startServerSession();
            }
        }
	}

    protected void deliver() throws IOException, JMSException {
        connectionConsumer.onNullMessage();
    }

    public void dump (PrintStream ps) {
        ps.println ("------ ConnectionConsumerReader dump ------");
        ps.println ("maxMessages: " + maxMessages);
        ps.println ("current load: " + load);
        super.dump(ps);
    }

}

