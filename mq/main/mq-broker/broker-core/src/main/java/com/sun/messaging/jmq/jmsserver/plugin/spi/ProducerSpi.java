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
 * %W% %G%
 */ 

package com.sun.messaging.jmq.jmsserver.plugin.spi;

import java.util.*;

import com.sun.messaging.jmq.util.log.*;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.io.PacketType;
import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.util.lists.Limitable;
import com.sun.messaging.jmq.jmsserver.core.ProducerUID;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;
import com.sun.messaging.jmq.jmsserver.service.ConnectionUID;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.util.CacheHashMap;

/**
 */

public abstract class ProducerSpi {
    
    protected static boolean DEBUG=false;

    protected transient Logger logger = Globals.getLogger();

    // record information about the last 20 removed
    // producers
    protected final static CacheHashMap cache = new CacheHashMap(20);

    private boolean valid = true;
    
    protected final static Map<ProducerUID, ProducerSpi> allProducers =
        Collections.synchronizedMap(new HashMap<ProducerUID, ProducerSpi>());

    protected static final Set wildcardProducers = Collections.synchronizedSet(new HashSet());

    protected transient Map lastResumeFlowSizes = Collections.synchronizedMap(new HashMap());

    private ConnectionUID connection_uid;
    
    protected DestinationUID destination_uid;
    protected transient Set destinations = null;

    protected ProducerUID uid;
    
    private long creationTime;

    private int pauseCnt = 0;
    private int resumeCnt = 0;
    private int msgCnt = 0;

    protected transient String creator = null;

    public String toString() {
        return "Producer["+ uid + "," + destination_uid + "," +
               connection_uid + "]";
    }
    
    public static Hashtable getAllDebugState() {
        Hashtable ht = new Hashtable();
        ht.put("TABLE", "AllProducers");
        Vector v = new Vector();
        synchronized (cache) {
            Iterator itr = cache.keySet().iterator();
            while (itr.hasNext()) {
                v.add(String.valueOf(((ProducerUID)itr.next()).longValue()));
            }
            
        }
        ht.put("cache", v);
        HashMap<ProducerUID, ProducerSpi> tmp = null;
        synchronized(allProducers) {
            tmp = new HashMap<ProducerUID, ProducerSpi>(allProducers);
        }
        Hashtable producers = new Hashtable();
        Iterator<ProducerUID> itr = tmp.keySet().iterator();
        while(itr.hasNext()) {
            ProducerUID p = itr.next();
            ProducerSpi producer = tmp.get(p);
            producers.put(String.valueOf(p.longValue()),
                  producer.getDebugState());
        }
        ht.put("producersCnt", new Integer(allProducers.size()));
        ht.put("producers", producers);
        return ht;
            
    }

    public synchronized void pause() {
        pauseCnt ++;
    }

    public synchronized void addMsg()
    {
        msgCnt ++;
    }

    public synchronized int getMsgCnt()
    {
        return msgCnt;
    }
    public synchronized boolean isPaused()
    {
        return pauseCnt > resumeCnt;
    }

    public synchronized void resume() { 
        resumeCnt ++;
    }

    public Hashtable getDebugState() {
        Hashtable ht = new Hashtable();
        ht.put("TABLE", "Producer["+uid.longValue()+"]");
        ht.put("uid", String.valueOf(uid.longValue()));
        ht.put("valid", String.valueOf(valid));
        ht.put("pauseCnt", String.valueOf(pauseCnt));
        ht.put("resumeCnt", String.valueOf(resumeCnt));
        if (connection_uid != null)
            ht.put("connectionUID", String.valueOf(connection_uid.longValue()));
        if (destination_uid != null)
            ht.put("destination", destination_uid.toString());
        return ht;
    }

    /** 
     */
    protected ProducerSpi(ConnectionUID cuid, DestinationUID duid, String id) {
        uid = new ProducerUID();
        this.connection_uid = cuid;
        this.destination_uid = duid;
        this.creator = id;
        logger.log(Logger.DEBUG,"Creating new Producer " + uid + " on "
             + duid + " for connection " + cuid);
    }

    public ProducerUID getProducerUID() {
        return uid;
    } 

    public ConnectionUID getConnectionUID() {
        return connection_uid;
    }

    public DestinationUID getDestinationUID() {
        return destination_uid;
    }

    /**
     */
    public static void clearProducers() {
        cache.clear();
        allProducers.clear();
        wildcardProducers.clear();
    }

	public abstract boolean isWildcard(); 

    public static Iterator getWildcardProducers() {
        return (new ArrayList(wildcardProducers)).iterator();
    }

    public static int getNumWildcardProducers() {
        return (wildcardProducers.size());
    }


    public static String checkProducer(ProducerUID uid)
    {
        String str = null;

        synchronized (cache) {
            str = (String)cache.get(uid);
        }
        if (str == null) {
             return " pid " + uid + " not of of last 20 removed";
        }
        return "Producer[" +uid + "]:" + str;
    }

    public static void updateProducerInfo(ProducerUID uid, String str)
    {
        synchronized (cache) {
            cache.put(uid, System.currentTimeMillis() + ":" + str);
        }
    }

    public static Iterator getAllProducers() {
        return (new ArrayList(allProducers.values())).iterator();
    }

    public static int getNumProducers() {
        return (allProducers.size());
    }

    public static ProducerSpi getProducer(ProducerUID uid) {
        return (ProducerSpi)allProducers.get(uid);
    }

    /** 
     */
    public static ProducerSpi destroyProducer(ProducerUID uid, String info) {
        ProducerSpi p = allProducers.remove(uid);
        updateProducerInfo(uid, info);
        if (p == null) {
            return p;
        }
        p.destroyProducer();
        return p;
    }

    protected abstract void destroyProducer();

    public synchronized void destroy() {
        valid = false;
    }

    public synchronized boolean isValid() {
        return valid;
    }

    public static ProducerSpi getProducer(String creator)
    {
        if (creator == null) return null;

        synchronized(allProducers) {
            Iterator<ProducerSpi> itr = allProducers.values().iterator();
            while (itr.hasNext()) {
                ProducerSpi c = itr.next();
                if (creator.equals(c.creator))
                    return c;
            }
        }
        return null;
    }

    public abstract Set getDestinations(); 

    class ResumeFlowSizes {
         int size = 0;
         long bytes = 0;
         long mbytes = 0;

         public ResumeFlowSizes(int s, long b, long mb) {
             size = s;
             bytes = b;
             mbytes = mb;
         }
   }

    public void sendResumeFlow(DestinationUID duid, int maxbatch) {
        resume();
        sendResumeFlow(duid, 0, 0, 0, ("Resuming " + this), true, maxbatch);
        logger.log(Logger.DEBUGHIGH,"Producer.sendResumeFlow("+duid+") resumed: "+this);
    }

    public void sendResumeFlow(DestinationUID duid,
                               int size, long bytes, long mbytes,
                               String reason, boolean uselast, int maxbatch) {

        ResumeFlowSizes rfs = null;
        if (uselast) {
            rfs = (ResumeFlowSizes)lastResumeFlowSizes.get(duid);
            if (rfs == null) {
                rfs = new ResumeFlowSizes(maxbatch, -1, Limitable.UNLIMITED_BYTES);
                lastResumeFlowSizes.put(duid, rfs);
            }
        } else {
            rfs = new ResumeFlowSizes(size, bytes, mbytes);
            lastResumeFlowSizes.put(duid, rfs);
        }

        ConnectionUID cuid = getConnectionUID();
        if (cuid == null) {
            logger.log(Logger.DEBUG,"cant resume flow[no con_uid] " + this);
            return;
        }

        IMQConnection con =(IMQConnection)Globals.getConnectionManager()
                                                 .getConnection(cuid);

        if (reason == null) reason = "Resuming " + this;

        Hashtable hm = new Hashtable();
        hm.put("JMQSize", rfs.size);
        hm.put("JMQBytes", new Long(rfs.bytes));
        hm.put("JMQMaxMsgBytes", new Long(rfs.mbytes));
        if (con != null) {
            Packet pkt = new Packet(con.useDirectBuffers());
            pkt.setPacketType(PacketType.RESUME_FLOW);
            hm.put("JMQProducerID", new Long(getProducerUID().longValue()));
            hm.put("JMQDestinationID", duid.toString());
            hm.put("Reason", reason);
            pkt.setProperties(hm);
            con.sendControlMessage(pkt);
        }
    }

}
