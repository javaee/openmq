/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2013 Oracle and/or its affiliates. All rights reserved.
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
 * @(#)AInspector.java	1.16 06/27/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.awt.Dimension;
import java.awt.CardLayout;
import java.util.Hashtable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;

import com.sun.messaging.jmq.admin.event.AdminEvent;
import com.sun.messaging.jmq.admin.event.AdminEventListener;

/** 
 * The inspector component of the admin GUI displays attributes
 * of a specified console object.
 *
 * <P>
 * There are a variety of objects that can be <EM>inspected</EM>:
 * <UL>
 * <LI>ObjStore List
 * <LI>ObjStore
 * <LI>ObjStore Destination List
 * <LI>ObjStore ConnectionFactory List
 * <LI>Broker List
 * <LI>Broker
 * <LI>Broker Service List
 * <LI>Broker Destination List
 * <LI>Broker Log List
 * </UL>
 *
 * <P>
 * For each of the object types above, a different inspector panel
 * is needed for displaying the object's attributes.
 *
 * <P>
 * This is implemented by having a main panel stacking all the 
 * different InspectorPanels in CardLayout. Each console object
 * that can be inspected will contain information specifying
 * which inspector panel to use to inspect it.
 */
public class AInspector extends JScrollPane  {

    private static String		SPLASH_SCREEN	= "SplashScreen";
    private static String		BLANK		= "Blank";

    private EventListenerList		aListeners = new EventListenerList();
    private InspectorPanel		currentCard = null;
    private CardLayout			cardLayout;
    private JPanel			cardPanel;
    private Hashtable			cardList;

    /**
     * Create/initialize the admin inspector GUI component.
     */
    public AInspector() {
	initGui();
    } 

    /**
     * Add an admin event listener to this admin UI component. 
     * @param l	admin event listener to add.
     */
    public void addAdminEventListener(AdminEventListener l)  {
	aListeners.add(AdminEventListener.class, l);
    }


    /**
     * Remove an admin event listener for this admin UI component. 
     * @param l	admin event listener to remove.
     */
    public void removeAdminEventListener(AdminEventListener l)  {
	aListeners.remove(AdminEventListener.class, l);
    }

    public void inspect(ConsoleObj conObj)  {
	/*
        System.err.println("AInspector: inspecting: " + conObj);
        System.err.println("\tClass: " + conObj.getClass().getName());
	*/

	if (conObj == null)  {
	    cardLayout.show(cardPanel, BLANK);
	    return;
	}

	InspectorPanel	ip = getInspectorPanel(conObj);

	if (ip == null)  {
	    ip = addInspectorPanel(conObj);

	    if (ip == null)  {
	        System.err.println("Cannot inspect object: "
	                + conObj
	                + "\nFailed to create inspector panel");
	        return;
	    }
	}

	currentCard = ip;

	currentCard.inspectConsoleObj(conObj, aListeners);
	showInspectorPanel(conObj);
    }

    public void refresh()  {
	if (currentCard != null)  {
	    currentCard.refresh();
	}
    }

    public void selectedObjectUpdated()  {
	if (currentCard != null)  {
	    currentCard.selectedObjectUpdated();
	}
    }

    public void clearSelection()  {
	if (currentCard != null)  {
	    currentCard.clearSelection();
	}
    }

    private void showInspectorPanel(ConsoleObj conObj)  {
        cardLayout.show(cardPanel, conObj.getInspectorPanelId());
    }

    private InspectorPanel getInspectorPanel(ConsoleObj conObj)  {
	String	panelId = conObj.getInspectorPanelId();
	Object obj = cardList.get(panelId);

	if ((obj != null) && (obj instanceof InspectorPanel))  {
	    return ((InspectorPanel)obj);
	}

	return (null);
    }

    private InspectorPanel addInspectorPanel(ConsoleObj conObj)  {
	String	panelId = conObj.getInspectorPanelId();
	String panelClassName = conObj.getInspectorPanelClassName();
	InspectorPanel ip = null;

	try  {
	    ip = (InspectorPanel)Class.forName(panelClassName).newInstance();
	    /*
	    System.err.println("Class: " + panelClassName + " instantiated !!");
	    */
	} catch (ClassNotFoundException cnfEx)  {
	    System.err.println("ConsoleObj does not name a valid inspector panel classname: "
			+ cnfEx);
	} catch (InstantiationException ie)  {
	    System.err.println("Failed to intantiate inspector panel : "
			+ ie);
	} catch (IllegalAccessException iae)  {
	    System.err.println("Illegal Access Exception while trying to intantiate inspector panel : "
			+ iae);
	}

	if (ip == null)
	    return (null);

	cardPanel.add(ip, panelId);
	cardList.put(panelId, ip);

	return (ip);
    }

    /*
     * Fire off/dispatch an admin event to all the listeners.
     
    private void fireAdminEventDispatched(AdminEvent ae)  {
	Object[] l = aListeners.getListenerList();

	for (int i = l.length-2; i>=0; i-=2)  {
	    if (l[i] == AdminEventListener.class)  {
		((AdminEventListener)l[i+1]).adminEventDispatched(ae);
	    }
	}
    }
    */
   
    private void initGui()  {

	cardPanel = new JPanel();
	cardLayout = new CardLayout();
	cardPanel.setLayout(cardLayout);

        initLayers(cardPanel);

	setViewportView(cardPanel);

        Dimension minimumSize = new Dimension(100, 50);
        setMinimumSize(minimumSize);

    }

    private void initLayers(JPanel parent)  {
	JPanel p = new JPanel(); 
	cardList = new Hashtable();

	p = new SplashScreenInspector(); 
	parent.add(p, SPLASH_SCREEN);
	cardList.put(SPLASH_SCREEN, p);

	p = new BlankInspector(); 
	parent.add(p, BLANK);
	cardList.put(BLANK, p);

	cardLayout.show(parent, SPLASH_SCREEN);
    }
}

