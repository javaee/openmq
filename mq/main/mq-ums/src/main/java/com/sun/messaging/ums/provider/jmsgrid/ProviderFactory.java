/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.messaging.ums.provider.jmsgrid;

    //import com.sun.messaging.xml.imq.soap.common.Constants;
//import com.sun.messaging.ums.openmq.*;
import com.sun.messaging.ums.factory.UMSConnectionFactory;
import java.lang.reflect.Constructor;
import java.util.Properties;
import javax.jms.Connection;
//import javax.jms.ConnectionFactory;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 *
 * @author chiaming
 */
public class ProviderFactory implements UMSConnectionFactory {
    
    private ConnectionFactory factory = null;
    private Properties props = null;
    
    /**
     * Called by UMS immediately after constructed.
     * 
     * @param props properties used by the connection factory.
     * @throws javax.jms.JMSException
     */
    
    public void init(Properties props) throws JMSException {
        this.props = props;

        try {
            String hostname = props.getProperty("grid.host", "localhost");
            String portstr = props.getProperty("grid.port", "50607");

            Properties factProps = new Properties();

            factProps.setProperty("driverName", "SpiritWave");

            String url = "tcp://" + hostname + ":" + portstr;
            factProps.setProperty("messageChannels", url);

            //factory = new com.spirit.wave.jms.WaveConnectionFactory (factProps);
            
            String cname = "com.spirit.wave.jms.WaveConnectionFactory";
            Class cf_class = Class.forName(cname);
            Class[] conargs = {Properties.class};

            Object[] convalues = {factProps};

            Constructor con = cf_class.getConstructor(conargs);
            
            factory = (ConnectionFactory) con.newInstance(convalues);
            
        } catch (Exception e) {
            
            JMSException jmse = new JMSException(e.getMessage());
            jmse.setLinkedException(e);

            throw jmse;
        }

    }
    
    /**
     * Same as JMS ConnectionFactory.createConnection();
     * 
     * @return
     * @throws javax.jms.JMSException
     */
    public Connection createConnection() throws JMSException {
        return factory.createConnection();
    }
    
    /**
     * Same as JMS ConnectionFactory.createConnection(String user, String password);
     * 
     * @param user
     * @param password
     * @return
     * @throws javax.jms.JMSException
     */
    public Connection createConnection(String user, String password) throws JMSException {
        return factory.createConnection(user, password);
    }

}
