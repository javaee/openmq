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

package com.sun.messaging.jmq.jmsclient;

import java.io.PrintStream;
import java.util.logging.Level;
import javax.jms.Message;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.CompletionListener;
import com.sun.messaging.jmq.io.ReadOnlyPacket;
import com.sun.messaging.AdministeredObject;
import com.sun.messaging.jmq.jmsclient.resources.ClientResources;

public class AsyncSendCallback implements Traceable {

     protected MessageProducerImpl producer = null;
     private Destination destination = null;
     private long transactionID = -1L;

     private Message message;
     private CompletionListener completionListener;
     private Message foreignMessage;

     private boolean onAckWait = false; 
     private boolean sendSuccessReturn = false;
     private boolean sendReturned = false;
     private boolean completed = false;
     private Exception exception = null;
     private boolean callbackCalled = false;
     private long timeoutTime = 0L; //only accessed by CB processor thread
     private static final Exception timedoutEx = getTimedoutException();


     private static Exception getTimedoutException() {
         String emsg = AdministeredObject.cr.getKString(
                       ClientResources.X_ASYNC_SEND_COMPLETION_WAIT_TIMEOUT);
         return new JMSException(emsg,
                    ClientResources.X_ASYNC_SEND_COMPLETION_WAIT_TIMEOUT);
     }

     public AsyncSendCallback(MessageProducerImpl p, Destination d,
                              Message m, CompletionListener l, Message fm) {
         this.producer = p;
         this.destination = d;
         this.message = m;
         this.completionListener = l;
         this.foreignMessage = fm;
     }

     protected void startTimeoutTimer() {
         if (timeoutTime != 0L) {
             return;
         }
         timeoutTime = System.currentTimeMillis()+
             producer.session.connection.getAsyncSendCompletionWaitTimeout();
     }

     protected boolean isTimedout() {
         synchronized(this) {
             if (completed || exception != null) {
                 return false;
             }
         }
         if (timeoutTime == 0L) {
             return false; 
         }
         return (System.currentTimeMillis() >= timeoutTime);
     }
    

     protected synchronized void setTransactionID(long tid) {
         transactionID = tid;
     }

     protected synchronized void asyncSendStart() throws JMSException {
         onAckWait = true;
     }

     protected synchronized void sendSuccessReturn() {
         sendSuccessReturn = true;
     }

     protected synchronized boolean hasSendReturned() {
         return sendReturned;
     }

     protected void sendReturn() {
         boolean remove = false;
         synchronized(this) {
             if (!sendSuccessReturn) {
                 remove = true;
             }
         }
         if (remove) {
             producer.session.removeAsyncSendCallback(this);
         }
         synchronized(this) {
             sendReturned = true;
         }
         producer.session.asyncSendCBProcessor.wakeup();
     }

     protected void processCompletion(ReadOnlyPacket ack, boolean checkstatus) {
         Exception ex = null;
         if (checkstatus) {
             ProtocolHandler ph = producer.session.protocolHandler;  
             try {
                 ph.checkWriteJMSMessageStatus(ph.getReplyStatus(ack), 
                     (com.sun.messaging.Destination)destination, ack, ph);
             } catch (Exception e) {
                 ex = e;
             }
         }
         boolean notify = true;
         synchronized(this) {
             if (exception != null && ex == null) {
                 producer.sessionLogger.log(Level.INFO,  
                     "Async send completed: "+this.toString(true));
             }
             if (completed || exception != null) {
                 notify = false;
             } else {
                 if (ex == null) {
                     completed = true;
                 } else {
                     exception = ex;
                     producer.sessionLogger.log(Level.INFO,
                         "Async send exceptioned: "+this.toString(true), ex);
                 }
             } 
         }
         if (notify) {
             if (completed && foreignMessage != null) {
                 try {
                     producer.resetForeignMessageHeader(message, foreignMessage);
                 } catch (Exception e) {
                    exception = e;
                    producer.sessionLogger.log(Level.INFO,
                         "Async send exceptioned: "+this.toString(true), e);
                    completed = false;
                 }
             }
             producer.session.asyncSendCBProcessor.wakeup();
         }
     }

     protected void processException(Exception ex) {
         synchronized(this) {
             if (completed || exception != null) {
                 return;
             }
             exception = ex;
         } 
         producer.sessionLogger.log(Level.INFO,
             "Async send exceptioned: "+this.toString(false), ex);
         producer.session.asyncSendCBProcessor.wakeup();
     }

     protected void callCompletionListener() {
         synchronized(this) {
             if (callbackCalled) {
                 return;
             }
             if (completed || exception != null) {
                 callbackCalled = true;
             } else if (isTimedout()) {
                 exception = timedoutEx; 
                 exception.fillInStackTrace();
                 callbackCalled = true;
             }
         }
         try {
         if (completed) {
             try {
                 completionListener.onCompletion(message);
             } catch (Exception ee) {
                 producer.sessionLogger.log(Level.WARNING, ee.getMessage()+this.toString(), ee);
             }
         } else if (exception != null) {
             try {
                 completionListener.onException(message, exception);
             } catch (Exception ee) {
                 producer.sessionLogger.log(Level.WARNING, ee.getMessage()+this.toString(), ee);
             }
         }

         } finally {
         producer.session.removeAsyncSendCallback(this);
         }
     }

     protected synchronized boolean isOnAckWait() {
         return onAckWait;
     }

     protected synchronized boolean isCompleted() {
         return completed;
     }

     protected synchronized boolean isExceptioned() {
         return (exception != null);
     }

     protected synchronized boolean isInTransaction() {
         return (transactionID != -1L);
     }

     private String toString(boolean getmid) {
         String str = "AsyncSendCallback[producer@" + producer.hashCode()+", "+destination;
         if (getmid) {
             try {
                 str = str+", "+message.getJMSMessageID();
             } catch (Exception e) {
                 str = str+", [message@"+message.hashCode()+":"+e.toString()+"]";
             }
         } else {
             str = str+", [message@"+message.hashCode()+"]";
         }
         str = str +", completed="+completed+", exception="+exception+
               ", sendSuccessReturn="+sendSuccessReturn+"]";
         return str;
     }

     public synchronized void dump (PrintStream ps) {
         ps.println ("------ AsyncSendCallback@"+this.hashCode()+" dump ------");
         ps.println("producer: @" + producer.hashCode());
         ps.println("destination: " + destination);
         if (completed) {
             try {
                 ps.println("message: " + message.getJMSMessageID());
             } catch (Exception e) {
                 ps.println("message: @" + message.hashCode()+":  "+e.toString()); 
             }
         } else {
             ps.println("message: @" + message.hashCode());
         }
         ps.println("completionListener: @" + completionListener.hashCode());
         if (foreignMessage == null) {
             ps.println("foreignMessage: null");
         } else {
             try {
                 ps.println("foreignMessage: @" +foreignMessage.getJMSMessageID());
             } catch (Exception e) {
                 ps.println("foreignMessage: @" +foreignMessage.hashCode()+": "+e.toString());
             }
         }
         ps.println("inTransaction: "+ !(transactionID == -1));
         ps.println("completed: " + completed);
         ps.println("exception: " + exception);
         ps.println("sendSuccessReturn: " + sendSuccessReturn);
         ps.println("sendReturned: " + sendReturned);
    }
}
