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

/*
 * @(#)TimeField.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.admin.apps.console.util;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import com.sun.messaging.jmq.admin.util.Globals;
import com.sun.messaging.jmq.admin.resources.AdminConsoleResources;

/**
 * This class implements a field used to enter some amount of
 * time. It supports the selection of the time unit:
 *
 * <UL>
 * <LI>Milliseconds
 * <LI>Seconds
 * <LI>Minutes
 * <LI>Hours
 * <LI>Days
 * </UL>
 * The default unit displayed is Seconds.
 * <P>
 *
 * The getValue() method returns the value entered in <STRONG>Milliseconds</STRONG>.
 * (because this class gives you the option of displaying the Milliseconds unit 
 * or not).
 */
public class TimeField extends JPanel {
    /*
     * Unit types. These are not indices
     */
    public final static int	MILLISECONDS	= 0;
    public final static int	SECONDS		= 1;
    public final static int	MINUTES		= 2;
    public final static int	HOURS		= 3;
    public final static int	DAYS		= 4;

    /*
     * Indices that indicate what string is displayed in the unit
     * combobox
     */
    private int		msecPos = 0,
			secPos = 1,
			minPos = 2,
			hrPos = 3,
			dayPos = 4;

    private boolean	showMillis = false;

    private static AdminConsoleResources acr = Globals.getAdminConsoleResources();

    private IntegerField	intF;
    private JComboBox	unitCB;

    public TimeField(long max, String text) {
	this(max, text, 0);
    }

    public TimeField(long max, int columns) {
	this(max, null, columns);
    }

    public TimeField(long max, String text, int columns) {
	this(max, text, columns, false);
    }

    public TimeField(long max, String text, int columns, boolean showMillis) {
        this.showMillis = showMillis;
	initGui(max, text, columns);
	setUnit(SECONDS);
    }

    public void addActionListener(ActionListener l)  {
	intF.addActionListener(l);
    }

    public void setText(String s)  {
	intF.setText(s);
    }

    public String getText()  {
	return (intF.getText());
    }

    public void setEnabled(boolean b)  {
	intF.setEnabled(b);
	unitCB.setEnabled(b);
    }

    /**
     * Set the byte unit.
     *
     * @param unit	byte unit. Can be a number from
     *			0 to 4 (inclusive) depending if Milliseconds
     *			is shown or not.
     */
    public void setUnit(int unit)  {
	int	index = secPos;

	switch (unit)  {
	case MILLISECONDS:
	    index = msecPos;
	break;

	case SECONDS:
	    index = secPos;
	break;

	case MINUTES:
	    index = minPos;
	break;

	case HOURS:
	    index = hrPos;
	break;

	case DAYS:
	    index = dayPos;
	break;
	}

	if ((index < 0) || (index > dayPos))  {
	    return;
	}

	unitCB.setSelectedIndex(index);
    }

    /**
     * Return the value in milliseconds.
     *
     * @return	The value entered in milliseconds.
     */
    public long getValue()  {
	String  s;
	int	selIndex;
	long	tmpLong;

	s = intF.getText();

	try  {
	    tmpLong = Long.parseLong(s);
	} catch (Exception e)  {
	    return (-1);
	}

	selIndex = unitCB.getSelectedIndex();

	if (showMillis)  {
	    if (selIndex == msecPos)  {
	        return (tmpLong);
	    } else if (selIndex == secPos)  {
	        return (tmpLong * 1000);
	    } else if (selIndex == minPos)  {
	        return (tmpLong * 1000 * 60);
	    } else if (selIndex == hrPos)  {
	        return (tmpLong * 1000 * 60 * 60);
	    } else if (selIndex == dayPos)  {
	        return (tmpLong * 1000 * 60 * 60 * 24);
	    }
	} else  {
	    if (selIndex == secPos)  {
	        return (tmpLong * 1000);
	    } else if (selIndex == minPos)  {
	        return (tmpLong * 1000 * 60);
	    } else if (selIndex == hrPos)  {
	        return (tmpLong * 1000 * 60 * 60);
	    } else if (selIndex == dayPos)  {
	        return (tmpLong * 1000 * 60 * 60 * 24);
	    }
	}

	return (-1);
    }

    private void initGui(long max, String text, int collumns)  {
        String[] units;

	intF = new IntegerField(0, max, text, collumns);

	if (showMillis)  {
    	    msecPos = 0;
	    secPos = 1;
	    minPos = 2;
	    hrPos = 3;
	    dayPos = 4;

	    units = new String[ 5 ];

	    units[msecPos] = acr.getString(acr.I_MILLISECONDS);

	} else  {
	    secPos = 0;
	    minPos = 1;
	    hrPos = 2;
	    dayPos = 3;

	    units = new String[ 4 ];
	}

	units[secPos] = acr.getString(acr.I_SECONDS);
	units[minPos] = acr.getString(acr.I_MINUTES);
	units[hrPos] = acr.getString(acr.I_HOURS);
	units[dayPos] = acr.getString(acr.I_DAYS);

	unitCB = new JComboBox(units);
	setLayout(new BorderLayout());

	add(intF, "Center");
	add(unitCB, "East");
    }

}

