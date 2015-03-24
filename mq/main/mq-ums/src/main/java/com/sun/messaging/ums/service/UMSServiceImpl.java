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

package com.sun.messaging.ums.service;

import com.sun.messaging.ums.simple.SimpleMessage;
import com.sun.messaging.ums.common.MessageUtil;
import com.sun.messaging.ums.common.Constants;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class UMSServiceImpl {

    //private SendService sendService = null;
    //private ReceiveService receiveService = null;
    
    public static final Logger logger = Logger.getLogger("ums.service");
    
    private Properties props = null;
    
    public static volatile boolean debug = false;
    
    private CacheSweeper sweeper = null;
    
    //Service element, provider attribute name - default attribute value=openmq
    public static final String SERVICE_PROVIDER_ATTR_NAME = "mom";
    
    private String DEFAULT_PROVIDER = "openmq";
    
    private static String DEFAULT_PROVIDER_ALIAS = "mq";

    private Hashtable <String, SendService> sendServices = new Hashtable<String, SendService>();
    
    private Hashtable<String, ReceiveService> receiveServices = new Hashtable <String, ReceiveService>();
    
    private Hashtable<String, ClientPool> cacheTable = new Hashtable <String, ClientPool>();
    
    private static final String PROVIDER_PREFIX = "mom.provider."; 
    
    private static final String GUEST = "guest";
    
    public static final String SERVICE_NAME = "UMS-Service";
    
    //default constructor
    public UMSServiceImpl(Properties p) throws SOAPException {
        this.props = p;
    }

    /**
     * 
     * XXX chiaming: Multi-vendor support. 
     * 
     * Here we construct all available messaging providers to UMS.
     * 
     * A HashMap that can be searched with vendorID.  Each Send/Receive service
     * is mapped to a vendorID.
     * 
     * @throws javax.xml.soap.SOAPException
     */
    public void init() throws SOAPException {

        try {
            
            sweeper = new CacheSweeper(props);
            
            List<String> list = getProviders();
            
            for (int i=0; i< list.size(); i++) {
                
                String provider = list.get(i);
            
                //Lock lock = new Lock();

                //cache = new JMSCache(MY_NAME, props, lock, logger);
                ClientPool cache = new ClientPool(provider, props);
                
                //add cache to cache table
                cacheTable.put(provider, cache);
                
                //add my cache to the sweeper
                sweeper.addClientPool(cache);
                
                SendService sendService = new SendServiceImpl(provider, cache, sweeper, props);
            
                sendServices.put(provider, sendService);

                ReceiveService receiveService = new ReceiveServiceImpl(provider, cache, sweeper, props);
            
                receiveServices.put(provider, receiveService);
            }
            
            sweeper.start();
            
        } catch (Exception e) {
            SOAPException soape = new SOAPException(e);

            throw soape;
        }
    }
      
    /**
     * get a list of provider names
     * 
     * @return
     */
    private List<String> getProviders() {
        
        List<String> list = new ArrayList<String>();
        
        int i = 0;
        
        boolean moreProvider = true;
        
        while (moreProvider == true) {
            
            String pname = PROVIDER_PREFIX + i;
            String pvalue = props.getProperty(pname);
            
            if (UMSServiceImpl.debug) {
                logger.info ("Looking up provider: " + pvalue);
            }
            
            if (pvalue != null) {
                
                if (i == 0) {
                    this.DEFAULT_PROVIDER = pvalue;
                }
                
                list.add(pvalue);
                
                if (UMSServiceImpl.debug) {
                    logger.info ("Found provider: " + pvalue);
                }
                
            } else {
                moreProvider = false;
            }
            
            i ++;
        }
        
        if (list.size() == 0) {
            list.add(DEFAULT_PROVIDER);
        }
        
        return list;
    }

    public SOAPMessage receive(SOAPMessage request) throws JMSException {

        String provider = this.getProvider(request);
        
        ReceiveService service = getReceiveService(provider);
        
        SOAPMessage respond = service.receive(request);

        return respond;
    }

    public void send(SOAPMessage message) throws JMSException {
        
        String provider = this.getProvider(message);
        
        SendService service = getSendService(provider);

        service.send(message);
    }
    
    public void commit(SOAPMessage message) throws JMSException {
        
        String provider = this.getProvider(message);
        
        SendService service = getSendService(provider);

        service.commit (message);
    }
    
    public void rollback (SOAPMessage message) throws JMSException {
        
        String provider = this.getProvider(message);
        
        SendService service = getSendService(provider);

        service.rollback (message);
    }
    
    public void commit(SimpleMessage message) throws JMSException {
        
        Map map = message.getMessageProperties();
        
        String provider = this.getProvider(map);
        
        SendService service = getSendService(provider);

        service.commit (message);
    }
    
    public void rollback (SimpleMessage message) throws JMSException {
        
        Map map = message.getMessageProperties();
        
        String provider = this.getProvider(map);
        
        SendService service = getSendService(provider);

        service.rollback (message);
    }
    
    public String authenticate (Map map) throws JMSException {
        
      String provider = this.getProvider(map);
      
      String user = null;
      String password = null;
      
      String[] ua = (String[]) map.get(Constants.USER);
      
      if (ua!= null && ua.length == 1) {
        user = ua[0];
      }
      
      String[] pa = (String[]) map.get(Constants.PASSWORD);
      if (pa!= null && pa.length == 1) {
        password = pa[0];
      }
      
      String[] tmp = (String[]) map.get(Constants.TRANSACTED);
      boolean transacted = false;
      
      if (tmp!= null && tmp.length == 1) {
        transacted = Boolean.valueOf(tmp[0]);
      }
      
      String sid = cacheTable.get(provider).authenticate(user, password, transacted);
      
      return sid;
    }
    
    public String authenticate(SOAPMessage sm) throws JMSException {

        String provider = this.getProvider(sm);

        String user = null;
        String password = null;
        String sid = null;

        try {
            
            user = MessageUtil.getServiceAttribute(sm, Constants.USER);
            
            password = MessageUtil.getServiceAttribute(sm, Constants.PASSWORD);
            
            String tmp = MessageUtil.getServiceAttribute(sm, Constants.TRANSACTED);
            boolean transacted = Boolean.valueOf(tmp);
            
            sid = cacheTable.get(provider).authenticate(user, password, transacted);
            
        } catch (SOAPException soape) {
            
            JMSException jmse = new JMSException(soape.getMessage());
            jmse.setLinkedException(soape);
            
            throw jmse;
        }

        return sid;
    }

    public void closeClient (SOAPMessage sm) throws Exception {
        
        String provider = this.getProvider(sm);
        
        //get sid
        String sid = MessageUtil.getServiceClientId(sm);
        
        if (sid != null) {
            this.cacheTable.get(provider).closeClient(sid);
        }
    }
    
    public String closeClient2 (Map map) throws Exception {
        
        String provider = this.getProvider(map);
        
        //get sid
        String sid = null;
        String[] tmp = (String[]) map.get(Constants.CLIENT_ID);
        if (tmp != null) {
            sid = tmp[0];
        }
        
        if (sid != null) {
            this.cacheTable.get(provider).closeClient(sid);
        }
        
        return sid;
    }
    

    public void sendText(String sid, boolean isTopic, String destName, String text, Map map) throws JMSException {

        String provider = this.getProvider(map);

        SendService service = getSendService(provider);

        ((SendServiceImpl) service).sendText(sid, isTopic, destName, text, map);
    }

    public String receiveText(String sid, String destName, boolean isTopic, long timeout, Map map) throws JMSException {
        
        String provider = this.getProvider(map);
        
        ReceiveService service = getReceiveService(provider);

        String text = ((ReceiveServiceImpl) service).receiveText(sid, destName, isTopic, timeout, map);

        return text;
    }
    
    private SendService getSendService (String provider) throws JMSException{
        
        SendService service = this.sendServices.get(provider);
        
        if (service == null) {
            throw new JMSException ("Provider not supported in this UMS service, provider=" + provider);
        }
        
        return service;
    }
    
    private ReceiveService getReceiveService (String provider) throws JMSException {
        
        ReceiveService service = this.receiveServices.get(provider);
        
        if (service == null) {
            throw new JMSException ("Provider not supported in this UMS service, provider=" + provider);
        }
        
        return service;
    }
    
    public String getProvider (SOAPMessage m) throws JMSException {
        String provider = null;
        
        try {
            
            provider = MessageUtil.getServiceAttribute(m, SERVICE_PROVIDER_ATTR_NAME);
            
            if (provider == null) {
                provider = DEFAULT_PROVIDER;
            }
            
        } catch (Exception e) {
            
            e.printStackTrace();
            
            JMSException jmse = new JMSException (e.getMessage());
            jmse.setLinkedException(e);
            
            throw jmse;
        }
        
        return provider;
        
    }
    
    public String getProvider (Map map) throws JMSException {
        
        String provider = null;
        
        try {
            
            String[]  pv = (String[]) map.get(Constants.SERVICE_PROVIDER_ATTR_NAME);
            //provider = MessageUtil.getServiceAttribute(m, SERVICE_PROVIDER_ATTR_NAME);
            
            if (pv!= null && pv.length ==1) {
                provider = pv[0];
            } else {
                provider = DEFAULT_PROVIDER;
            }
            
        } catch (Exception e) {
            
            e.printStackTrace();
            
            JMSException jmse = new JMSException (e.getMessage());
            jmse.setLinkedException(e);
            
            throw jmse;
        }
        
        return provider;
        
        
    }

    /**
     * Servlet life cycle.
     */
    public void destroy() {

        try {
            
            //stop the sweeper
            this.sweeper.close();

            closeSendService();

            closeReceiveService();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void closeSendService() {
        
        Iterator it = this.sendServices.values().iterator();
        
        while (it.hasNext()) {
            
            try {
                
                SendService service = (SendService) it.next();
                service.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void closeReceiveService() {

        Iterator it = this.receiveServices.values().iterator();

        while (it.hasNext()) {

            try {

                ReceiveService service = (ReceiveService) it.next();
                service.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static synchronized boolean getDebug() {
        return debug;
    }
    
    public static synchronized void setDebug (boolean flag) {
        debug = flag;
    }
}
