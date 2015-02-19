/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2014 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)GetMessagesHandler.java	1.8 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.nio.ByteBuffer;
import javax.jms.*;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;

public class GetMessagesHandler extends AdminCmdHandler  {

    private static boolean DEBUG = getDEBUG();

    public GetMessagesHandler(AdminDataHandler parent) {
        super(parent);
    }

    /**
     * Handle the incomming administration message.
     *
     * @param con    The Connection the message came in on.
     * @param cmd_msg    The administration message
     * @param cmd_props The properties from the administration message
     */
    public boolean handle(IMQConnection con, Packet cmd_msg,
                       Hashtable cmd_props) {

        if ( DEBUG ) {
            logger.log(Logger.DEBUG, this.getClass().getName() + ": " +
                            "Getting messages: " + cmd_props);
        }

        Vector v = new Vector();
        int status = Status.OK;
        String errMsg = null;

        String destination = (String)cmd_props.get(MessageType.JMQ_DESTINATION);
        Integer destType = (Integer)cmd_props.get(MessageType.JMQ_DEST_TYPE);
        Long startIndex = (Long)cmd_props.get(MessageType.JMQ_START_MESSAGE_INDEX),
            maxNumMsgs = (Long)cmd_props.get(MessageType.JMQ_MAX_NUM_MSGS_RETRIEVED);
        Boolean getBody = (Boolean)cmd_props.get(MessageType.JMQ_GET_MSG_BODY);
        String msgID = (String)cmd_props.get(MessageType.JMQ_MESSAGE_ID);
	HashMap destNameType = new HashMap();

	if ((destination == null) || (destType == null))  {
            errMsg = "Destination name and type not specified";
            logger.log(Logger.ERROR, errMsg);

            status = Status.ERROR;
	}

	if (getBody == null)  {
	    getBody = Boolean.FALSE;
	}

        if (destination != null) {
            try {
                Destination[] ds = DL.getDestination(null, destination,
                                     DestType.isQueue(destType.intValue()));
                Destination d = ds[0];

                if (d != null) {
                    if (DEBUG) {
                        d.debug();
                    }

		    if (msgID != null)  {
			d.load();

		        SysMessageID sysMsgID = SysMessageID.get(msgID);
                        PacketReference	pr = getPacketReference(sysMsgID);

			if (pr != null)  {
			    HashMap h = constructMessageInfo(sysMsgID, 
							getBody.booleanValue(), 
							destNameType);
                            if (h == null) {
                                pr = null;
                            } else {
                                v.add(h);
                            }
			} 
                        if (pr == null) {
			    /*
                            errMsg= rb.getString(rb.X_MSG_NOT_FOUND, msgID);
			    */
                            errMsg= "Could not locate message " 
					+ msgID 
					+ " in destination " 
					+ destination;
                            status = Status.NOT_FOUND;
			}
		    } else  {
		        SysMessageID sysMsgIDs[] = d.getSysMessageIDs(startIndex, maxNumMsgs);

		        for (int i = 0;i < sysMsgIDs.length; ++i)  {
			    HashMap h = constructMessageInfo(sysMsgIDs[i], 
						getBody.booleanValue(),
						destNameType);
                            if (h == null) {
                                continue;
                            }
			    v.add(h);
		        }
		    }
                } else {
                    errMsg= rb.getString( rb.X_DESTINATION_NOT_FOUND, 
                               destination);
                    status = Status.NOT_FOUND;
                }
            } catch (Exception ex) {
                logger.logStack(Logger.ERROR, ex.getMessage(), ex);
		ex.printStackTrace();
                status = Status.ERROR;
                assert false;
            }
        }

       // Send reply
       Packet reply = new Packet(con.useDirectBuffers());
       reply.setPacketType(PacketType.OBJECT_MESSAGE);
   
       setProperties(reply, MessageType.GET_MESSAGES_REPLY,
           status, errMsg);
   
       setBodyObject(reply, v);
       parent.sendReply(con, cmd_msg, reply);

       return true;
   }

    public HashMap constructMessageInfo(SysMessageID sysMsgID, boolean getBody,
						HashMap destNameType)  {
        HashMap h = new HashMap();
        PacketReference	pr = getPacketReference(sysMsgID);
        if (pr == null) {
            return null;
        }
        Packet	pkt = pr.getPacket();
        if (pkt == null) {
            return null;
        }
        HashMap msgHeaders = pr.getHeaders();
        //Destination d = pr.getDestination();
        String corrID = pkt.getCorrelationID(), errMsg;
        int typeMask = DestType.DEST_TYPE_QUEUE;
        byte b[] = null;

        h.put("CorrelationID", corrID);

        if (corrID != null)  {
	    try  {
                b = corrID.getBytes("UTF8");
	    } catch(Exception e)  {
	    }
        }

        h.put("CorrelationIDAsBytes", b);

        h.put("DeliveryMode", (pkt.getPersistent()) ? 
			Integer.valueOf(javax.jms.DeliveryMode.PERSISTENT) : 
			Integer.valueOf(javax.jms.DeliveryMode.NON_PERSISTENT));

        h.put("DestinationName", pkt.getDestination());

	if (pkt.getIsQueue())  {
            typeMask = DestType.DEST_TYPE_QUEUE;
	} else  {
            typeMask = DestType.DEST_TYPE_TOPIC;
	}

        h.put("DestinationType", Integer.valueOf(typeMask));

        h.put("Expiration", Long.valueOf(pkt.getExpiration()));

        h.put("MessageID", msgHeaders.get("JMSMessageID"));
 
        h.put("Priority", Integer.valueOf(pkt.getPriority()));

        h.put("Redelivered", Boolean.valueOf(pkt.getRedelivered()));

	/*
	 * The ReplyTo information in the packet contains
	 * the destination name and class name (i.e. dest class
	 * name), which the broker cannot really use.
	 * We need to query/check if:
	 *  - the destination exists
	 *  - what it's type is
	 */
        String replyToDestName = pkt.getReplyTo();

        if (replyToDestName != null)  {
	    boolean destFound = false, isQueue = true;

	    if (destNameType != null)  {
		Boolean isQ = (Boolean)destNameType.get(replyToDestName);

		if (isQ != null)  {
		    isQueue = isQ.booleanValue();
		    destFound = true;
		}
	    }

	    if (!destFound)  {
	        try  {
	            Destination topic, queue;

	            Destination[] ds = DL.findDestination(null, replyToDestName, true);
                    queue = ds[0]; //PART
	            ds = DL.findDestination(null, replyToDestName, false);
                    topic = ds[0];

		    if ((queue != null) && (topic != null))  {
                        errMsg = "Cannot determine type of ReplyTo destination."
			    + " There is a topic and queue with the name: "
			    + replyToDestName;
                        logger.log(Logger.WARNING, errMsg);
		    } else if (queue != null)  {
		        destFound = true;
		        isQueue = true;
		    } else if (topic != null)  {
		        destFound = true;
		        isQueue = false;
		    }

		    if (destFound)  {
		        /*
		         * Cache dest name/type so that we can look it up there
		         * next time.
		         */
		        destNameType.put(replyToDestName, Boolean.valueOf(isQueue));
		    } else  {
		        /*
		         * It is possible that this destination no longer exists.
		         * e.g. Temporary destination, whose connection has gone away.
		         * Not sure how to proceed at this point.
		         */
		    }
	        } catch (Exception e)  {
                    errMsg = "Caught exception while determining ReplyTo destination type";
                    logger.log(Logger.WARNING, errMsg, e);
	        }
	    }

            h.put("ReplyToDestinationName", replyToDestName);
	    if (destFound)  {
		if (isQueue)  {
                    typeMask = DestType.DEST_TYPE_QUEUE;
		} else  {
                    typeMask = DestType.DEST_TYPE_TOPIC;
		}
                h.put("ReplyToDestinationType", Integer.valueOf(typeMask));
	    }
        }

        h.put("Timestamp", Long.valueOf(pkt.getTimestamp()));

        h.put("Type", pkt.getMessageType());

        Hashtable msgProps;

        try  {
            msgProps = pr.getProperties();
        } catch (Exception e)  {
            msgProps = null;
        }
        h.put("MessageProperties", msgProps);

        int packetType = pr.getPacket().getPacketType();
        h.put("MessageBodyType", Integer.valueOf(packetType));

        byte[] msgBody = null;
        if (getBody)  {
            ByteBuffer bb = pr.getPacket().getMessageBodyByteBuffer();
            if (bb != null && bb.hasArray())  {
                msgBody = bb.array();
            }
        }
        if (msgBody != null) {

            switch (packetType)  {
            case PacketType.TEXT_MESSAGE:
	        try  {
                    String textMsg = new String(msgBody, "UTF8");

                    h.put("MessageBody", textMsg);
	        } catch(Exception e)  {
                    errMsg = "Caught exception while creating text message body";
                    logger.log(Logger.ERROR, errMsg, e);
	        }
            break;

            case PacketType.BYTES_MESSAGE:
            case PacketType.STREAM_MESSAGE:
                h.put("MessageBody", msgBody);
            break;

            case PacketType.MAP_MESSAGE:
                try  {
                    ByteArrayInputStream byteArrayInputStream = 
					new ByteArrayInputStream (msgBody);
                    ObjectInputStream objectInputStream = 
				new ObjectInputStream(byteArrayInputStream);
                    HashMap mapMsg = (HashMap)objectInputStream.readObject();

                    h.put("MessageBody", mapMsg);
                } catch(Exception e)  {
                    errMsg = "Caught exception while creating map message body";
                    logger.log(Logger.ERROR, errMsg, e);
                }
            break;


            case PacketType.OBJECT_MESSAGE:
                try  {
                    ByteArrayInputStream byteArrayInputStream = 
					new ByteArrayInputStream (msgBody);
                    ObjectInputStream objectInputStream = 
				new ObjectInputStream(byteArrayInputStream);
                    Object objMsg = (Serializable)objectInputStream.readObject();

                    h.put("MessageBody", objMsg);
	        } catch(Exception e)  {
                    errMsg = "Caught exception while creating object message body";
                    logger.log(Logger.ERROR, errMsg, e);
                }
            break;

	    default:
                errMsg = "Unsupported message type for GET_MESSAGES handler: " + packetType;
                logger.log(Logger.ERROR, errMsg);
	    break;
            }
        }
    
        return (h);
    }

    public static PacketReference getPacketReference(SysMessageID sysMsgID)  {
        return Globals.getDestinationList().get(null, sysMsgID);
    }
}
