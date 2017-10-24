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

package com.sun.messaging.visualvm.ui;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import com.sun.messaging.jms.management.server.ConsumerInfo;
import com.sun.messaging.jms.management.server.TransactionInfo;
import com.sun.messaging.visualvm.chart.ChartPanel;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;

/**
 * This is the abstract superclass for those resource lists 
 * for which a single manager MBean, holds information about all the individual resources in the list
 * (i.e. there are not individual MBeans for each resource)
 * 
 * In practice this means:
 * 
 * The producer manager monitor MBean  
 * The consumer manager monitor MBean
 * The transaction manager monitor MBean
 *
 */
@SuppressWarnings("serial")
public abstract class SingleMBeanResourceList extends MQResourceList {
	
    public SingleMBeanResourceList(DataViewComponent dvc) {
		super(dvc);
	}

	/**
     * Returns the name of the manager MBean for this resource list.
     * @return 
     */
    protected abstract String getManagerMBeanName();
    
    /**
     * Returns the name of the operation which, if applied to the manager MBean,
     * will return a CompositeInfo[] containing information about the individual resources
     * (e.g. getConsumerInfo, getProducerInfo, getTransactionInfo etc)
     * @return
     */
    protected abstract String getGetSubitemInfoOperationName();
    
    /**
     * Return the name of the lookup key in the CompositeInfo[] that can be used as a key
     * @return
     */
    protected abstract String getSubitemIdName();

	@Override
	public void initTableModel() {
        if (getTableModel() == null) {
            MQResourceListTableModel tm = new MQResourceListTableModel() {

                @Override
                public List loadData() {
                    List<Map.Entry<String, CompositeData>> list = null;

                    try {
                        ObjectName mgrMonitorObjName = new ObjectName(getManagerMBeanName());

                        CompositeData cds[]=null;
                        try {
                            cds = (CompositeData[]) getMBeanServerConnection().invoke(mgrMonitorObjName,
                            		getGetSubitemInfoOperationName(), null, null);
                        } catch (InstanceNotFoundException ex) {
                            // manager monitor MBean not found, probably because there is no broker running in this JVM
                             return new ArrayList();
                        }
                        if (cds == null) {
                        	return new ArrayList();
                        }

                        // build a map with key = the defined resource ID and value = CompositeData
                        SortedMap<String, CompositeData> map = new TreeMap<String, CompositeData>();
                         
                        for (int i = 0; i < cds.length; i++) {
                            if (cds[i] != null) {
                                String id = (String) cds[i].get(getSubitemIdName());
                                map.put(id, cds[i]);
                            }
                        }
                        
                        Set<Map.Entry<String, CompositeData>> set = map.entrySet();
                        list = new ArrayList<Map.Entry<String, CompositeData>>(set);
                    } catch (MBeanException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ReflectionException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        // we've probably lost connection to the broker
                    	return new ArrayList();
                    } catch (MalformedObjectNameException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (NullPointerException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    return list;
                }

                @Override
                public Object getDataValueAt(List l, int row, int col) {
                    if (l == null) {
                        return (null);
                    }

                    Map.Entry me = (Map.Entry) l.get(row);
                    CompositeData cd = (CompositeData) me.getValue();
                    String attrName = getColumnName(col);
                    Object v=null;
                    Object obj = null;

                    obj = cd.get(attrName);
                    if (obj != null) {
                        if ((attrName.equals(ConsumerInfo.LAST_ACK_TIME))|attrName.equals(TransactionInfo.CREATION_TIME)) {
                            Long ackTime = (Long) obj;
                            if (ackTime.longValue() == 0) {
                                v = "0";
                            } else {
                                v = checkNullAndPrintTimestamp(ackTime);
                            }
                        } else if (obj instanceof String[]) {
                        	// String array
                            String sa[] = (String[]) obj;

                            v = "";

                            for (int i = 0; i < sa.length; ++i) {
                                v = v + sa[i];

                                if (i < (sa.length - 1)) {
                                    v = v + ",";
                                }
                            }
                        } else {
                            v = obj;
                        }
                    }
                    return (v);
                }

				@Override
				public void updateCharts() {
					updateRegisteredCharts();
				}
            };
            tm.setAttributes(getinitialDisplayedAttrsList());
            setTableModel(tm);
        }
    }
	
    protected static String checkNullAndPrintTimestamp(Long timestamp) {
        if (timestamp != null) {
            String ts;
            Date d = new Date(timestamp.longValue());
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
            ts = df.format(d);
            return (ts);
        } else {
            return ("");
        }
    }
    
    public String getTooltipForColumn(int columnIndex){
    	String columnName = getTableModel().getColumnName(columnIndex);
    	String key = this.getClass().getName() + "." + columnName;

    	String tooltip ="";
    	try {
    		tooltip = NbBundle.getMessage (SingleMBeanResourceList.class, key);
    	} catch (MissingResourceException mre){
    		tooltip = "Cannot find text for "+mre.getKey();
        	System.out.println(tooltip);
    	}

    	if (tooltip!=null){
    		return tooltip;
    	} else {
    		return "";
    	}
    }
        
}

