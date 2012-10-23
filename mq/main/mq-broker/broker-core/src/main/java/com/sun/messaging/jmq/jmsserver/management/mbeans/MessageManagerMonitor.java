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
 * @(#)MessageManagerMonitor.java	1.3 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.nio.ByteBuffer;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanException;
import javax.management.openmbean.CompositeData;

import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jms.management.server.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.management.util.ConsumerUtil;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;
import com.sun.messaging.jmq.jmsserver.core.Subscription;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.util.DestType;

public class MessageManagerMonitor extends MQMBeanReadOnly {
    private static MBeanParameterInfo[] getMessageInfoSignature = {
	            new MBeanParameterInfo("destinationType", String.class.getName(), 
		                        mbr.getString(mbr.I_DST_MGR_OP_PARAM_DEST_TYPE)),
	            new MBeanParameterInfo("destinationName", String.class.getName(), 
		                        mbr.getString(mbr.I_DST_MGR_OP_PARAM_DEST_NAME)),
		    new MBeanParameterInfo("messageID", String.class.getName(),
			                "Message ID"),
		    new MBeanParameterInfo("startMsgIndex", Long.class.getName(),
			                "Start Message Index"),
		    new MBeanParameterInfo("maxNumMsgsRetrieved", Long.class.getName(),
			                "Maximum Number of Messages Retrieved"),
		    new MBeanParameterInfo("getBody", Boolean.class.getName(),
			                "Get Body")
			    };

    private static MBeanOperationInfo[] ops = {
	    new MBeanOperationInfo("getMessageInfo",
		"Get information on messages from a destination",
		    getMessageInfoSignature, 
		    Vector.class.getName(),
		    MBeanOperationInfo.INFO)
		};

    public MessageManagerMonitor()  {
	super();
    }

    public Vector getMessageInfo(String destinationType, String destinationName,
			String messageID, Long startMsgIndex, 
			Long maxNumMsgsRetrieved, Boolean getBody) throws MBeanException {
	Vector msgInfo = new Vector();

	try  {
	    HashMap destNameType = new HashMap();

	    if ((destinationName == null) || (destinationType == null))  {
		throw new BrokerException("Destination name and type not specified");
	    }

	    Destination[] ds = DL.getDestination(null, destinationName,
			       (destinationType.equals(DestinationType.QUEUE)));
            Destination d = ds[0]; //PART

	    if (d == null)  {
		throw new BrokerException(rb.getString(rb.X_DESTINATION_NOT_FOUND,
					       destinationName));
	    }

	    if (getBody == null)  {
		getBody = Boolean.FALSE;
	    }

	    if (messageID != null)  {
		d.load();

	        SysMessageID sysMsgID = SysMessageID.get(messageID);
                PacketReference	pr = getPacketReference(sysMsgID);

		if (pr != null)  {
		    HashMap h = constructMessageInfo(sysMsgID, 
						getBody.booleanValue(), 
						destNameType);

		    msgInfo.add(h);
		} else  {
		    throw new BrokerException("Could not locate message " 
					+ messageID 
					+ " in destination " 
					+ destinationName);
		}
	    } else  {
		SysMessageID sysMsgIDs[] = d.getSysMessageIDs(startMsgIndex, maxNumMsgsRetrieved);

	        for (int i = 0;i < sysMsgIDs.length; ++i)  {
		    HashMap h = constructMessageInfo(sysMsgIDs[i], 
					getBody.booleanValue(),
					destNameType);

		    msgInfo.add(h);
	        }
            }
	} catch(Exception e)  {
	    handleOperationException("getMessageInfo", e);
	}

	return (msgInfo);
    }

    public String getMBeanName()  {
	return ("MessageManagerMonitor");
    }

    public String getMBeanDescription()  {
	return ("Monitoring MBean for Message Manager");
	/*
	return (mbr.getString(mbr.I_MSG_MGR_MON_DESC));
	*/
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfo()  {
	return (null);
    }

    public MBeanOperationInfo[] getMBeanOperationInfo()  {
	return (ops);
    }

    public MBeanNotificationInfo[] getMBeanNotificationInfo()  {
	return (null);
    }

    private HashMap constructMessageInfo(SysMessageID sysMsgID, boolean getBody,
				HashMap destNameType) throws BrokerException  {
        HashMap h = new HashMap();
        PacketReference	pr = getPacketReference(sysMsgID);
        Packet	pkt = pr.getPacket();
        HashMap msgHeaders = pr.getHeaders();
        Destination d = pr.getDestination();
        String corrID = pkt.getCorrelationID(), errMsg;
        String destType = DestinationType.QUEUE;
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
			new Integer(javax.jms.DeliveryMode.PERSISTENT) : 
			new Integer(javax.jms.DeliveryMode.NON_PERSISTENT));

        h.put("DestinationName", pkt.getDestination());

	if (pkt.getIsQueue())  {
            destType = DestinationType.QUEUE;
	} else  {
            destType = DestinationType.TOPIC;
	}

        h.put("DestinationType", destType);

        h.put("Expiration", new Long(pkt.getExpiration()));

        h.put("MessageID", msgHeaders.get("JMSMessageID"));
 
        h.put("Priority", new Integer(pkt.getPriority()));

        h.put("Redelivered", new Boolean(pkt.getRedelivered()));

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
			throw new BrokerException(errMsg);
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
		        destNameType.put(replyToDestName, new Boolean(isQueue));
		    } else  {
		        /*
		         * It is possible that this destination no longer exists.
		         * e.g. Temporary destination, whose connection has gone away.
		         * Not sure how to proceed at this point.
		         */
		    }
	        } catch (Exception e)  {
                    errMsg = "Caught exception while determining ReplyTo destination type";
		    throw new BrokerException(errMsg);
	        }
	    }

            h.put("ReplyToDestinationName", replyToDestName);
	    if (destFound)  {
		if (isQueue)  {
                    destType = DestinationType.QUEUE;
		} else  {
                    destType = DestinationType.TOPIC;
		}
                h.put("ReplyToDestinationType", destType);
	    }
        }

        h.put("Timestamp", new Long(pkt.getTimestamp()));

        h.put("Type", pkt.getMessageType());

        Hashtable msgProps;

        try  {
            msgProps = pr.getProperties();
        } catch (Exception e)  {
            msgProps = null;
        }
        h.put("MessageProperties", msgProps);

        int packetType = pr.getPacket().getPacketType();
        h.put("MessageBodyType", new Integer(packetType));

        if (getBody)  {
            ByteBuffer bb = pr.getPacket().getMessageBodyByteBuffer();
            byte[] msgBody = null;

            if (bb.hasArray())  {
                msgBody = bb.array();
            }

            switch (packetType)  {
            case PacketType.TEXT_MESSAGE:
	        try  {
                    String textMsg = new String(msgBody, "UTF8");

                    h.put("MessageBody", textMsg);
	        } catch(Exception e)  {
                    errMsg = "Caught exception while creating text message body";
		    throw new BrokerException(errMsg);
	        }
            break;

            case PacketType.BYTES_MESSAGE:
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
		    throw new BrokerException(errMsg);
                }
            break;

            case PacketType.STREAM_MESSAGE:
                h.put("MessageBody", msgBody);
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
		    throw new BrokerException(errMsg);
                }
            break;

	    default:
                errMsg = "Unsupported message type for GET_MESSAGES handler: " + packetType;
		throw new BrokerException(errMsg);
            }
        }
    
        return (h);
    }

    private PacketReference getPacketReference(SysMessageID sysMsgID)  {
        return Globals.getDestinationList().get(null, sysMsgID);
    }

}
