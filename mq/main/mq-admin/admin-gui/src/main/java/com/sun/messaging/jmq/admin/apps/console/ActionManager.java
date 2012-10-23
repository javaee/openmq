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
 * @(#)ActionManager.java	1.33 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.console;

import java.util.Hashtable;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.event.EventListenerList;

import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;
import com.sun.messaging.jmq.admin.event.AdminEvent;
import com.sun.messaging.jmq.admin.event.BrokerAdminEvent;
import com.sun.messaging.jmq.admin.event.AdminEventListener;
import com.sun.messaging.jmq.admin.apps.console.event.DialogEvent;
import com.sun.messaging.jmq.admin.apps.console.event.ConsoleActionEvent;


/**
 * This class manages the set of actions used by the admin console.
 * A task is made an 'action' because it can be trigerred from multiple
 * controls e.g. from the toolbar or from a menu. It helps to centralize
 * control of the task in an action so that things like enabling/disabling
 * is made easy.
 */
public class ActionManager  {
    /*
     * Bit flags to identify a particular action.
     * Bit flags are needed because in some cases we need
     * to specify more than one action to enable. These flags
     * are also conveniently used as action identifiers e.g.
     * in getAction().
     */
    public static final int	ADD		= 1 << 0;
    public static final int	DELETE		= 1 << 1;
    public static final int	PREFERENCES	= 1 << 2;
    public static final int	EXIT		= 1 << 3;
    public static final int	ABOUT		= 1 << 4;
    public static final int	PROPERTIES	= 1 << 5;
    public static final int	SHUTDOWN	= 1 << 6;
    public static final int	RESTART		= 1 << 7;
    public static final int	PAUSE		= 1 << 8;
    public static final int	RESUME		= 1 << 9;
    public static final int	CONNECT		= 1 << 10;
    public static final int	DISCONNECT	= 1 << 11;
    public static final int	EXPAND_ALL	= 1 << 12;
    public static final int	COLLAPSE_ALL	= 1 << 13;
    public static final int	REFRESH		= 1 << 14;
    public static final int	PURGE		= 1 << 15;
    public static final int	QUERY_BROKER	= 1 << 16;

    private Hashtable	actionTable = new Hashtable(20);

    /* 
     * Bit mask reflecting which actions are currently active.
     */
    private int		currentlyActive = 0;

    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();
    private EventListenerList	aListeners = new EventListenerList();

    public ActionManager()  {
	initActions();
    }

    /** Returns the relevant action object corresponding to the
     * flag passed.
     *
     */
    public Action getAction(int actionFlag)  {
	Action a;
	
	a = (Action)(actionTable.get(new Integer(actionFlag)));

	return (a);
    }


    /**
     * Set the enabled state of a particular action.
     */
    public void setEnabled(int actionFlag, boolean b)  {
	Action a = getAction(actionFlag);

	if (a == null)  {
	    return;
	}

	if (b)  {
	    if (isActive(currentlyActive, actionFlag))  {
		return;
	    }
	    a.setEnabled(b);

	    /*
	     * Set the flag's bit in the mask.
	     */
	    currentlyActive |= actionFlag;
	} else  {
	    if (!isActive(currentlyActive, actionFlag))  {
		return;
	    }
	    a.setEnabled(b);

	    /*
	     * Unset the flag's bit in the mask.
	     */
	    currentlyActive &= ~actionFlag;
	}
    }


    /**
     * Enable the actions specfied in the mask and disable the
     * ones that are not. This is to bring the current set
     * of actions to be in the state exactly specified by
     * the mask parameter.
     */
    public void setActiveActions(int mask)  {
	if (mask == currentlyActive)  {
	    return;
	}

	/*
	 * Make sure that the actions specified in the mask are enabled.
	 * Make sure that the actions not specified in the mask are disabled.
	 *
	 * Some actions are not specified below because they are to always
	 * remain active.
	 */
	matchActions(mask, ADD);
	matchActions(mask, DELETE);
	matchActions(mask, PROPERTIES);
	matchActions(mask, SHUTDOWN);
	matchActions(mask, RESTART);
	matchActions(mask, PAUSE);
	matchActions(mask, RESUME);
	matchActions(mask, CONNECT);
	matchActions(mask, DISCONNECT);
	matchActions(mask, REFRESH);
	matchActions(mask, PURGE);
	matchActions(mask, QUERY_BROKER);
    }

    public char getCharMnemonic(int actionFlag)  {
        if (actionFlag == ADD)  {
	    return (acr.getChar(acr.I_ADD_MNEMONIC));
	} else if (actionFlag == DELETE)  {
	    return (acr.getChar(acr.I_DELETE_MNEMONIC));
	/*
	} else if (actionFlag == PREFERENCES)  {
	    return (acr.getChar(acr.I_PREFERENCES_MNEMONIC));
	*/
	} else if (actionFlag == EXIT)  {
	    return (acr.getChar(acr.I_EXIT_MNEMONIC));
	} else if (actionFlag == ABOUT)  {
	    return (acr.getChar(acr.I_ABOUT_MNEMONIC));
	} else if (actionFlag == PROPERTIES)  {
	    return (acr.getChar(acr.I_PROPERTIES_MNEMONIC));
	} else if (actionFlag == SHUTDOWN)  {
	    return (acr.getChar(acr.I_SHUTDOWN_MNEMONIC));
	} else if (actionFlag == RESTART)  {
	    return (acr.getChar(acr.I_RESTART_MNEMONIC));
	} else if (actionFlag == PAUSE)  {
	    return (acr.getChar(acr.I_PAUSE_MNEMONIC));
	} else if (actionFlag == RESUME)  {
	    return (acr.getChar(acr.I_RESUME_MNEMONIC));
	} else if (actionFlag == CONNECT)  {
	    return (acr.getChar(acr.I_CONNECT_MNEMONIC));
	} else if (actionFlag == DISCONNECT)  {
	    return (acr.getChar(acr.I_DISCONNECT_MNEMONIC));
	} else if (actionFlag == EXPAND_ALL)  {
	    return (acr.getChar(acr.I_EXPAND_ALL_MNEMONIC));
	} else if (actionFlag == COLLAPSE_ALL)  {
	    return (acr.getChar(acr.I_COLLAPSE_ALL_MNEMONIC));
	} else if (actionFlag == REFRESH)  {
	    return (acr.getChar(acr.I_REFRESH_MNEMONIC));
	} else if (actionFlag == PURGE)  {
	    return (acr.getChar(acr.I_PURGE_MNEMONIC));
	} else if (actionFlag == QUERY_BROKER)  {
	    return (acr.getChar(acr.I_QUERY_BROKER_MNEMONIC));
	}

	return ((char)0);
    }

    /*
     * Checks the action flag in the specified mask and
     * makes sure the corresponding action managed by this
     * class matches it's enabled state.
     */
    private void matchActions(int mask, int actionFlag)  {
	if (isActive(mask, actionFlag))  {
	    setEnabled(actionFlag, true);
	} else  {
	    setEnabled(actionFlag, false);
	}
    }

    /*
     * Returns the enabled state of a particular action in
     * the specified mask.
     */
    private boolean isActive(int mask, int actionFlag)  {
	return ((mask & actionFlag) == actionFlag);
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

    /*
     * Fire off/dispatch an admin event to all the listeners.
     */
    private void fireAdminEventDispatched(AdminEvent ae)  {
	Object[] l = aListeners.getListenerList();

	for (int i = l.length-2; i>=0; i-=2)  {
	    if (l[i] == AdminEventListener.class)  {
		((AdminEventListener)l[i+1]).adminEventDispatched(ae);
	    }
	}
    }

    /*
     * Creates all actions
     */
    private void initActions()  {
	Action tmpAction;

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_ADD),
	        AGraphics.adminImages[AGraphics.ADD])  {
	            public void actionPerformed(ActionEvent e) {
            		DialogEvent de = new DialogEvent(this);
            		de.setDialogType(DialogEvent.ADD_DIALOG);
            		fireAdminEventDispatched(de);
	            }
	        };
        addAction(ADD, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_DELETE),
	        AGraphics.adminImages[AGraphics.DELETE])  {
	            public void actionPerformed(ActionEvent e) {
            		DialogEvent de = new DialogEvent(this);
            		de.setDialogType(DialogEvent.DELETE_DIALOG);
            		fireAdminEventDispatched(de);
	            }
	        };
        addAction(DELETE, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_PREFERENCES),
	        AGraphics.adminImages[AGraphics.PREFERENCES])  {
	            public void actionPerformed(ActionEvent e) {
	                System.err.println("Preferences");
	            }
	        };
        addAction(PREFERENCES, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_EXIT))  {
	            public void actionPerformed(ActionEvent e) {
            		ConsoleActionEvent cae = new 
				ConsoleActionEvent(this, ConsoleActionEvent.EXIT);
            		fireAdminEventDispatched(cae);
	            }
	        };
        addAction(EXIT, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_ABOUT)) {
	            public void actionPerformed(ActionEvent e) {
            		ConsoleActionEvent cae = new 
				ConsoleActionEvent(this, ConsoleActionEvent.ABOUT);
            		fireAdminEventDispatched(cae);
	            }
	        };
        addAction(ABOUT, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_PROPERTIES),
		    AGraphics.adminImages[AGraphics.PROPERTIES])  {
	            public void actionPerformed(ActionEvent e) {
            		DialogEvent de = new DialogEvent(this);
            		de.setDialogType(DialogEvent.PROPS_DIALOG);
            		fireAdminEventDispatched(de);
	            }
	        };
        addAction(PROPERTIES, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_SHUTDOWN_BROKER),
			AGraphics.adminImages[AGraphics.SHUTDOWN])  {
	            public void actionPerformed(ActionEvent e) {
            		DialogEvent de = new DialogEvent(this);
            		de.setDialogType(DialogEvent.SHUTDOWN_DIALOG);
            		fireAdminEventDispatched(de);
	            }
	        };
        addAction(SHUTDOWN, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_RESTART_BROKER),
			AGraphics.adminImages[AGraphics.RESTART])  {
	            public void actionPerformed(ActionEvent e) {
            		DialogEvent de = new DialogEvent(this);
            		de.setDialogType(DialogEvent.RESTART_DIALOG);
            		fireAdminEventDispatched(de);
	            }
	        };
        addAction(RESTART, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_PAUSE),
			AGraphics.adminImages[AGraphics.PAUSE])  {
	            public void actionPerformed(ActionEvent e) {
            		DialogEvent de = new DialogEvent(this);
            		de.setDialogType(DialogEvent.PAUSE_DIALOG);
            		fireAdminEventDispatched(de);
	            }
	        };
        addAction(PAUSE, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_RESUME),
			AGraphics.adminImages[AGraphics.RESUME])  {
	            public void actionPerformed(ActionEvent e) {
            		DialogEvent de = new DialogEvent(this);
            		de.setDialogType(DialogEvent.RESUME_DIALOG);
            		fireAdminEventDispatched(de);
	            }
	        };
        addAction(RESUME, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_CONNECT), 
			AGraphics.adminImages[AGraphics.CONNECT_TO_OBJSTORE])  {
	            public void actionPerformed(ActionEvent e) {
            		DialogEvent de = new DialogEvent(this);
            		de.setDialogType(DialogEvent.CONNECT_DIALOG);
            		fireAdminEventDispatched(de);
	            }
	        };
        addAction(CONNECT, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_DISCONNECT), 
			AGraphics.adminImages[AGraphics.DISCONNECT_FROM_OBJSTORE])  {
	            public void actionPerformed(ActionEvent e) {
            		DialogEvent de = new DialogEvent(this);
            		de.setDialogType(DialogEvent.DISCONNECT_DIALOG);
            		fireAdminEventDispatched(de);
	            }
	        };
        addAction(DISCONNECT, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_EXPAND_ALL),
			AGraphics.adminImages[AGraphics.EXPAND_ALL])  {
	            public void actionPerformed(ActionEvent e) {
            		ConsoleActionEvent cae = new 
				ConsoleActionEvent(this, ConsoleActionEvent.EXPAND_ALL);
            		fireAdminEventDispatched(cae);
	            }
	        };
        addAction(EXPAND_ALL, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_COLLAPSE_ALL),
			AGraphics.adminImages[AGraphics.COLLAPSE_ALL])  {
	            public void actionPerformed(ActionEvent e) {
            		ConsoleActionEvent cae = new 
				ConsoleActionEvent(this, ConsoleActionEvent.COLLAPSE_ALL);
            		fireAdminEventDispatched(cae);
	            }
	        };
        addAction(COLLAPSE_ALL, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_REFRESH),
		AGraphics.adminImages[AGraphics.REFRESH])  {
	            public void actionPerformed(ActionEvent e) {
            		ConsoleActionEvent cae = new 
				ConsoleActionEvent(this, ConsoleActionEvent.REFRESH);
            		fireAdminEventDispatched(cae);
	            }
	        };
        addAction(REFRESH, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_PURGE_BROKER_DEST),
		AGraphics.adminImages[AGraphics.PURGE])  {
                    public void actionPerformed(ActionEvent e) {
                        DialogEvent de = new DialogEvent(this);
                        de.setDialogType(DialogEvent.PURGE_DIALOG);
                        fireAdminEventDispatched(de);
                    }
	        };
        addAction(PURGE, tmpAction);

	tmpAction =
	    new AbstractAction(acr.getString(acr.I_QUERY_BROKER),
		AGraphics.adminImages[AGraphics.QUERY_BROKER])  {
                    public void actionPerformed(ActionEvent e) {
            		BrokerAdminEvent bae = new 
				BrokerAdminEvent(this, BrokerAdminEvent.QUERY_BROKER);
            		fireAdminEventDispatched(bae);
                    }
	        };
        addAction(QUERY_BROKER, tmpAction);

    }

    private void addAction(int actionFlag, Action a)  {
	actionTable.put(new Integer(actionFlag), a);
	currentlyActive |= actionFlag;
    }

}
