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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.event.EventListenerList;

public class TableMouseAdapter extends MouseAdapter {

    private JTable table = null;
    private EventListenerList mqUIListeners = new EventListenerList();

    public TableMouseAdapter(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            if (table != null) {
                int selectedIndex = -1;

                selectedIndex = table.getSelectedRow();

                if (selectedIndex != -1) {
                    MQResourceListTableModel model = (MQResourceListTableModel) table.getModel();

                    if (model != null) {
                        Object obj = model.getRowData(selectedIndex);
                        MQUIEvent me = new MQUIEvent(this, MQUIEvent.ITEM_SELECTED);
                        me.setSelectedObject(obj);
                        fireMQUIEventDispatched(me);
                    }
                }
            }
        }
    }

    public void addMQUIEventListener(MQUIEventListener l) {
        mqUIListeners.add(MQUIEventListener.class, l);
    }

    public void removeMQUIEventListener(MQUIEventListener l) {
        mqUIListeners.remove(MQUIEventListener.class, l);
    }

    public void fireMQUIEventDispatched(MQUIEvent me) {
        Object[] l = mqUIListeners.getListenerList();

        for (int i = l.length - 2; i >= 0; i -= 2) {
            if (l[i] == MQUIEventListener.class) {
                ((MQUIEventListener) l[i + 1]).mqUIEventDispatched(me);
            }
        }
    }
}
