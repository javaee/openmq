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
 * @(#)LogConfig.java	1.13 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.management.mbeans;

import java.util.HashMap;
import java.util.Properties;
import java.util.Date;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.AttributeChangeNotification;
import javax.management.MBeanException;

import com.sun.messaging.jms.management.server.*;

import com.sun.messaging.jmq.Version;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.ConfigListener;
import com.sun.messaging.jmq.jmsserver.config.PropertyUpdateException;

import com.sun.messaging.jmq.jmsserver.management.util.LogUtil;

public class LogConfig extends MQMBeanReadWrite
					implements ConfigListener  {
    private Properties brokerProps = null;
    private static final String ROLLOVER_BYTES_PROP = "java.util.logging.FileHandler.limit";
    private static final String LOGLEVEL_PROP =".level";
    private static MBeanAttributeInfo[] attrs = {
	    new MBeanAttributeInfo(LogAttributes.LEVEL,
					String.class.getName(),
					mbr.getString(mbr.I_LOG_ATTR_LEVEL),
					true,
					true,
					false),

	    new MBeanAttributeInfo(LogAttributes.ROLL_OVER_BYTES,
					Long.class.getName(),
					mbr.getString(mbr.I_LOG_ATTR_ROLL_OVER_BYTES),
					true,
					true,
					false),

	    new MBeanAttributeInfo(LogAttributes.ROLL_OVER_SECS,
					Long.class.getName(),
					mbr.getString(mbr.I_LOG_ATTR_ROLL_OVER_SECS),
					true,
					true,
					false),
			};

    private static String[] attrChangeTypes = {
		    AttributeChangeNotification.ATTRIBUTE_CHANGE
		};

    private static MBeanNotificationInfo[] notifs = {
	    new MBeanNotificationInfo(
		    attrChangeTypes,
		    AttributeChangeNotification.class.getName(),
	            mbr.getString(mbr.I_ATTR_CHANGE_NOTIFICATION)
		    )
		};

    public LogConfig()  {
	super();
	initProps();

	com.sun.messaging.jmq.jmsserver.config.BrokerConfig cfg = Globals.getConfig();
	cfg.addListener(LOGLEVEL_PROP, this);
	cfg.addListener(ROLLOVER_BYTES_PROP, this);
	cfg.addListener("imq.log.file.rolloversecs", this);
    }

    public void setLevel(String s) throws MBeanException  {
	Properties p = new Properties();
	p.setProperty(LOGLEVEL_PROP, 
		LogUtil.toInternalLogLevel(s));

	try  {
	    com.sun.messaging.jmq.jmsserver.config.BrokerConfig cfg = Globals.getConfig();
	    cfg.updateProperties(p, true);
	} catch (Exception e)  {
	    handleSetterException(LogAttributes.LEVEL, e);
	}
    }
    public String getLevel()  {
	String s = brokerProps.getProperty(LOGLEVEL_PROP);

	return(LogUtil.toExternalLogLevel(s));
    }

    public void setRolloverBytes(Long l) throws MBeanException  {
	Properties p = new Properties();
	p.setProperty(ROLLOVER_BYTES_PROP, l.toString());

	try  {
	    com.sun.messaging.jmq.jmsserver.config.BrokerConfig cfg = Globals.getConfig();
	    cfg.updateProperties(p, true);
	} catch (Exception e)  {
	    handleSetterException(LogAttributes.ROLL_OVER_BYTES, e);
	}
    }
    public Long getRolloverBytes() throws MBeanException  {
	String s = brokerProps.getProperty(ROLLOVER_BYTES_PROP);
	Long l = null;

	try  {
	    l = new Long(s);
	} catch (Exception e)  {
	    handleGetterException(LogAttributes.ROLL_OVER_BYTES, e);
	}

	return (l);
    }

    public void setRolloverSecs(Long l) throws MBeanException  {
	Properties p = new Properties();
	p.setProperty("imq.log.file.rolloversecs", l.toString());

	try  {
	    com.sun.messaging.jmq.jmsserver.config.BrokerConfig cfg = Globals.getConfig();
	    cfg.updateProperties(p, true);
	} catch (Exception e)  {
	    handleSetterException(LogAttributes.ROLL_OVER_SECS, e);
	}
    }
    public Long getRolloverSecs() throws MBeanException  {
	String s = brokerProps.getProperty(Globals.IMQ + ".log.file.rolloversecs");
	Long l = null;

	try  {
		if(s !=null && !s.trim().equals("")){
			l = new Long(s);
		} else {
			l = 0l;
		}
	} catch (Exception e)  {
	    handleGetterException(LogAttributes.ROLL_OVER_SECS, e);
	}

	return (l);
    }

    public String getMBeanName()  {
	return ("LogConfig");
    }

    public String getMBeanDescription()  {
	return (mbr.getString(mbr.I_LOG_CFG_DESC));
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfo()  {
	return (attrs);
    }

    public MBeanOperationInfo[] getMBeanOperationInfo()  {
	return (null);
    }

    public MBeanNotificationInfo[] getMBeanNotificationInfo()  {
	return (notifs);
    }

    public void validate(String name, String value)
            throws PropertyUpdateException {
    }
            
    public boolean update(String name, String value) {
	Object newVal, oldVal;

	/*
        System.err.println("### cl.update called: "
            + name
            + "="
            + value);
	*/

	if (name.equals(LOGLEVEL_PROP))  {
	    newVal = LogUtil.toExternalLogLevel(value);
	    oldVal = getLevel();
            notifyAttrChange(LogAttributes.LEVEL, 
				newVal, oldVal);
	} else if (name.equals(ROLLOVER_BYTES_PROP))  {
	    try  {
	        newVal = Long.valueOf(value);
	    } catch (NumberFormatException nfe)  {
	        logger.log(Logger.ERROR,
		    getMBeanName()
		    + ": cannot parse internal value of "
		    + LogAttributes.ROLL_OVER_BYTES
		    + ": " 
		    + nfe);
                newVal = null;
	    }

	    try  {
	        oldVal = getRolloverBytes();
	    } catch(Exception e)  {
		logProblemGettingOldVal(LogAttributes.ROLL_OVER_BYTES, e);
	        oldVal = null;
	    }

            notifyAttrChange(LogAttributes.ROLL_OVER_BYTES, 
				newVal, oldVal);
	} else if (name.equals("imq.log.file.rolloversecs"))  {
	    try  {
	        newVal = Long.valueOf(value);
	    } catch (NumberFormatException nfe)  {
	        logger.log(Logger.ERROR,
		    getMBeanName()
		    + ": cannot parse internal value of "
		    + LogAttributes.ROLL_OVER_SECS
		    + ": " 
		    + nfe);
                newVal = null;
	    }

	    try  {
	        oldVal = getRolloverSecs();
	    } catch(Exception e)  {
		logProblemGettingOldVal(LogAttributes.ROLL_OVER_SECS, e);
	        oldVal = null;
	    }

            notifyAttrChange(LogAttributes.ROLL_OVER_SECS, 
				newVal, oldVal);
	}

        initProps();
        return true;
    }

    public void notifyAttrChange(String attrName, Object newVal, Object oldVal)  {
	sendNotification(
	    new AttributeChangeNotification(this, sequenceNumber++, new Date().getTime(),
	        "Attribute change", attrName, (newVal == null ? "" : newVal.getClass().getName()),
	        oldVal, newVal));
    }

    private void initProps() {
	brokerProps = Globals.getConfig().toProperties();
	Version version = Globals.getVersion();
	brokerProps.putAll(version.getProps());
    }
}
