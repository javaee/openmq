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
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import com.sun.messaging.jms.management.server.BrokerOperations;
import com.sun.messaging.jms.management.server.MQObjectName;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;

/**
 * This panel displays a list of property values for the broker
 * 
 * There are two columns, one showing property description and one showing the corresponding property values
 *
 */
@SuppressWarnings("serial")
public class BrokerPropertyList extends MQAttributeList {
    
	public BrokerPropertyList(DataViewComponent dvc) {
		super(dvc);
	}


	private static String[] propertyNames = {
		"imq.varhome",
		"imq.authentication.basic.user_repository",
		"imq.persist.store",
		"imq.cluster.ha",
		"imq.cluster.brokerlist.active"
		};
    
	@Override
	public void initTableModel() {
        if (getTableModel() == null) {
            MQResourceListTableModel tm = new MQResourceListTableModel() {

				@Override
                public List loadData() {
                  
                    MBeanServerConnection mbsc = getMBeanServerConnection();
                    if ((mbsc == null)) {
                        return null;
                    }
                    return constructPropertyList(mbsc);
                }

                @Override
                public Object getDataValueAt(List l, int row, int col) {
                    Object value=null;
                	String[] entry = (String[]) l.get(row);
                    if (col == 0) {
                        value = entry[0];
                    } else if (col == 1) {
                        value = entry[1];
                    }
                    return value;
                }

				@Override
				public void updateCharts() {
					updateRegisteredCharts();
				}
            };
            
            tm.setAttrsInColumn(false);
            setTableModel(tm);
        }
    }   
	

	private List constructPropertyList(MBeanServerConnection mbsc) {
		List list = new ArrayList();
		try {
			for (int i = 0; i < propertyNames.length; i++) {
	        	String thisPropertyName = propertyNames[i];
				String thisPropertyValue = getBrokerPropValue(mbsc,thisPropertyName);
				addEntry(list, thisPropertyName, thisPropertyValue);
				// special handling for certain properties
				if (thisPropertyName.equals("imq.authentication.basic.user_repository")){
					if (thisPropertyValue.equals("ldap")){
						String ldapServerPropName = "imq.user_repository.ldap.server";
		            	String ldapServer = getBrokerPropValue(mbsc, ldapServerPropName);
		            	addEntry(list, ldapServerPropName, ldapServer);
					}
				} else if (thisPropertyName.equals("imq.persist.store")){
					if (thisPropertyValue.equals("jdbc")){
						String jdbcVendorPropName =  "imq.persist.jdbc.dbVendor";
		            	String jdbcVendor = getBrokerPropValue(mbsc, jdbcVendorPropName);
		            	addEntry(list, jdbcVendorPropName, jdbcVendor);
		            	if ((jdbcVendor != null) && (!jdbcVendor.equals(""))) {
							String dburlPropName = "imq.persist.jdbc." + jdbcVendor + ".opendburl";
			            	String dburl = getBrokerPropValue(mbsc, dburlPropName);
			            	addEntry(list, dburlPropName, dburl);
		            	}
					}
				}
			}
		} catch (IOException e){
			// we can't connect to the broker: broker has probably terminated
			return new ArrayList();
		}
		return list;
	}


	private void addEntry(List list, String thisPropertyName, String thisPropertyValue) {
		String[] entry = new String[2];
		entry[0]=thisPropertyName;
		entry[1]=thisPropertyValue;
		list.add(entry);
	}

    private String getBrokerPropValue(MBeanServerConnection mbsc, String propName) throws IOException {
        String ret = "";

        try {
            ObjectName bkrCfg = new ObjectName(MQObjectName.BROKER_CONFIG_MBEAN_NAME);

            //Setup parameters and signature for getProperty operation.
            Object params[] = {propName};
            String signature[] = {String.class.getName()};
            
            try {
                ret = (String) mbsc.invoke(bkrCfg, BrokerOperations.GET_PROPERTY,  params, signature);
            } catch (InstanceNotFoundException ex) {
                return "";
            } catch (MBeanException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ReflectionException ex) {
                Exceptions.printStackTrace(ex);
            }

        } catch (MalformedObjectNameException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NullPointerException ex) {
            Exceptions.printStackTrace(ex);
        }

        return (ret);
    }
    
    @Override
    public void handleItemQuery(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


	@Override
	protected String getDescriptionForAttribute(String attributeName) {
		
		 //look up the tooltip for this attribute name
		 String key = this.getClass().getName() + "." + attributeName;
		 String tooltip ="";
		 try {
			 tooltip = NbBundle.getMessage (MQAttributeList.class, key);
		 } catch (MissingResourceException mre){
			 tooltip = "Cannot find text for "+mre.getKey();
			 System.out.println(tooltip);
		 }
		
		return tooltip;
	}


	@Override
	public int getCorner() {
		return DataViewComponent.BOTTOM_LEFT;
	}

}

