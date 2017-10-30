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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;

import javax.management.*;
import javax.management.remote.*;

import com.sun.messaging.AdminConnectionFactory;
import com.sun.messaging.AdminConnectionConfiguration;
import com.sun.messaging.jms.management.server.*;


public class MQLogViewer implements ActionListener,
				NotificationListener {

    JFrame f;
    MQConnectDialog connectDialog = null;
    JMenuItem exit, connect, disconnect, clearLog;
    JCheckBoxMenuItem	info, warning, error;
    JTextArea logMsgArea, statusTextArea;
    JLabel brokerAddress;

    String address = null, adminUser = "admin", adminPasswd = "admin";
    String	logLevelStrings[] = { LogLevel.INFO, 
					LogLevel.WARNING, 
					LogLevel.ERROR };

    AdminConnectionFactory acf;
    JMXConnector jmxc;
    MBeanServerConnection mbsc;
    ObjectName	logCfg = null;
    NotificationFilterSupport myFilter = null;

    public MQLogViewer(JFrame f, String address, 
			String adminUser, String adminPasswd) {
	this.f = f;
	this.address = address;
	this.adminUser = adminUser;
	this.adminPasswd = adminPasswd;

	try  {
	    logCfg = new ObjectName(MQObjectName.LOG_MONITOR_MBEAN_NAME);
	} catch (Exception e)  {
	    addStatusText("Caught exception while creating Log MBean ObjectName: " + e);
	}

	initGUI();

	if ((address != null) && (adminUser != null) && (adminPasswd != null))  {
	    doConnect();
	}
    }

    private void initGUI()  {
	JMenuBar menubar = createMenubar();
	JComponent toolbar = createToolBar();
	JPanel mainPanel = createMainPanel();
	JPanel statusArea = createStatusArea();

	f.setJMenuBar(menubar);
	f.getContentPane().add(toolbar, BorderLayout.NORTH);
	f.getContentPane().add(mainPanel, BorderLayout.CENTER);
	f.getContentPane().add(statusArea, BorderLayout.SOUTH);
    }

    public void logMessage(LogNotification n) {
        logMsgArea.append(n.getMessage());
        logMsgArea.setCaretPosition(logMsgArea.getText().length());
    }

    public void clearLogArea()  {
        logMsgArea.setText("");
    }

    public void addStatusText(String statusText) {
        statusTextArea.append(statusText);
        statusTextArea.setCaretPosition(statusTextArea.getText().length());
        statusTextArea.append("\n");
    }

    public void clearStatus()  {
        statusTextArea.setText("");
    }

    public void doConnect()  {
	try  {
	    acf = new AdminConnectionFactory();
	    if (address != null)  {
	        acf.setProperty(AdminConnectionConfiguration.imqAddress,
				address);
	    }
	    jmxc = acf.createConnection(adminUser, adminPasswd);
	    jmxc.addConnectionNotificationListener(this, null, null);
	    mbsc = jmxc.getMBeanServerConnection();

	    addStatusText("Connected to broker at: " 
		+ acf.getProperty(AdminConnectionConfiguration.imqAddress));
	    
	    brokerAddress.setText(
		acf.getProperty(AdminConnectionConfiguration.imqAddress));

	    logOn();

	    connect.setEnabled(false);
	    disconnect.setEnabled(true);
	} catch (Exception e)  {
	    addStatusText("Caught exception while connecting: " + e);
	}
    }

    public void doDisconnect()  {
	try  {
	    logOff();

	    addStatusText("Disconnecting from broker at: " 
		+ acf.getProperty(AdminConnectionConfiguration.imqAddress));

	    brokerAddress.setText("<none>");

	    if (jmxc != null)  {
		jmxc.close();
	    }
	    jmxc = null;
	    mbsc = null;
	    acf = null;
	    connect.setEnabled(true);
	    disconnect.setEnabled(false);
	    clearLogArea();
	} catch (Exception e)  {
	    addStatusText("Caught exception while disconnecting: " + e);
	}
    }

    public void logOff()  {
	if (myFilter != null)  {
	    try  {
		if (mbsc != null)  {
	            mbsc.removeNotificationListener(logCfg, this, myFilter, null);
		}
		myFilter = null;
	        addStatusText("Unregistered log listener");
	    } catch(Exception e)  {
	        addStatusText("Caught exception while removing log listener: " + e);
	    }
	}
    }

    public void logOn()  {
	String logLevels = getLogLevel();

	logOff();

	if (logLevels.equals(""))  {
	    addStatusText("No log levels selected.");
	    return;
	}

	myFilter = new NotificationFilterSupport();

	StringTokenizer st = new StringTokenizer(logLevels, "|");
	while (st.hasMoreTokens()) {
	    String oneLevel = st.nextToken();
	    myFilter.enableType(LogNotification.LOG_LEVEL_PREFIX + oneLevel);
	}

	try  {
	    mbsc.addNotificationListener(logCfg, this, myFilter, null);
	    addStatusText("Registered listener at log levels: " + logLevels);
	} catch(Exception e)  {
	    addStatusText("Caught exception while addind log listener: " + e);
	}
    }

    public String getLogLevel()  {
	String s = null;

	if (info.getState())  {
	    s = info.getText();
	}

	if (warning.getState())  {
	    if (s == null)  {
	        s = warning.getText();
	    } else  {
		s = s + "|" + warning.getText();
	    }
	}

	if (error.getState())  {
	    if (s == null)  {
	        s = error.getText();
	    } else  {
		s = s + "|" + error.getText();
	    }
	}

	if (s == null)  {
	    return ("");
	}

	return (s);
    }

    public void handleNotification(Notification notification, Object handback)  {
	if (notification instanceof LogNotification)  {
            logMessage((LogNotification)notification);
	} else if (notification instanceof JMXConnectionNotification)  {
	    JMXConnectionNotification jcn = (JMXConnectionNotification)notification;

	    /*
	     * TBD: handle server shutdown
	     */
	    if (jcn.getType().equals(JMXConnectionNotification.CLOSED) ||
	       jcn.getType().equals(JMXConnectionNotification.FAILED))  {
	    }
	}
    }

    private JMenuBar createMenubar()  {
	JMenuBar menubar;
	JMenu menu, logLevelMenu;

	menubar = new JMenuBar();

	menu = new JMenu("LogViewer");
	logLevelMenu = new JMenu("Log Levels");
	menubar.add(menu);
	menubar.add(logLevelMenu);

	connect = new JMenuItem("Connect");
	connect.addActionListener(this);
	menu.add(connect);

	disconnect = new JMenuItem("Disconnect");
	disconnect.addActionListener(this);
	disconnect.setEnabled(false);
	menu.add(disconnect);

	menu.addSeparator();

	clearLog = new JMenuItem("Clear Log Display");
	clearLog.addActionListener(this);
	menu.add(clearLog);

	menu.addSeparator();

	exit = new JMenuItem("Exit");
	exit.addActionListener(this);
	menu.add(exit);

	info = new JCheckBoxMenuItem(LogLevel.INFO);
	info.addActionListener(this);
	info.setState(true);
	logLevelMenu.add(info);

	warning = new JCheckBoxMenuItem(LogLevel.WARNING);
	warning.addActionListener(this);
	warning.setState(true);
	logLevelMenu.add(warning);

	error = new JCheckBoxMenuItem(LogLevel.ERROR);
	error.addActionListener(this);
	error.setState(true);
	logLevelMenu.add(error);

	return (menubar);
    }

    private JPanel createMainPanel()  {
	JPanel p = new JPanel();

	p.setLayout(new BorderLayout());
	logMsgArea = new JTextArea(12, 80);
	logMsgArea.setEditable(false);
	JScrollPane tablePane = new JScrollPane(logMsgArea);
	p.add(BorderLayout.CENTER, tablePane);


	return (p);
    }

    private JComponent createToolBar()  {
	JPanel p = new JPanel();
	JLabel l;

	p.setLayout(new FlowLayout(FlowLayout.LEFT));

	l = new JLabel("Log messages for broker at address: ");
	p.add(l);

	brokerAddress = new JLabel("<none>");
	p.add(brokerAddress);

	return (p);
    }

    private JPanel createStatusArea()  {
	JPanel p = new JPanel();
	p.setLayout(new BorderLayout());
	statusTextArea = new JTextArea(3, 80);
	statusTextArea.setLineWrap(true);
	statusTextArea.setEditable(false);
	JScrollPane statusTextPane = new JScrollPane(statusTextArea);
	p.add(statusTextPane,  BorderLayout.CENTER);

	return (p);
    }

    public void actionPerformed(ActionEvent e)  {
	Object src = e.getSource();

	if (src instanceof JCheckBoxMenuItem)  {
	    JCheckBoxMenuItem cb = (JCheckBoxMenuItem)src;

	    if (src == info)  {
	        logOn();
	    } else if (src == warning)  {
	        logOn();
	    } else if (src == error)  {
	        logOn();
	    }
	} else if (src instanceof JMenuItem)  {
	    JMenuItem mi = (JMenuItem)src;

	    if (src == exit)  {
	        System.exit(0);
	    } else if (src == connect)  {
		showConnectDialog();
	    } else if (src == disconnect)  {
		doDisconnect();
	    } else if (src == clearLog)  {
		clearLogArea();
	    }
	} else if (src instanceof JButton) {
	    address = connectDialog.getAddress();
	    adminUser = connectDialog.getUserName();
	    adminPasswd = connectDialog.getPassword();

	    doConnect();
	}
    }

    private void showConnectDialog()  {
	if (connectDialog == null)  {
	    connectDialog = new MQConnectDialog(f, "Connect to Broker", this);

	    connectDialog.setAddress((address == null) ?
	                    getDefaultAddress() : address);
	    connectDialog.setUserName((adminUser == null) ?
	                    getDefaultUserName() : adminUser);
	    connectDialog.setPassword((adminPasswd == null) ?
	                    getDefaultPassword() : adminPasswd);
	}
	connectDialog.setLocationRelativeTo(f);
	connectDialog.setVisible(true);
    }

    private static void doExit()  {
	System.exit(0);
    }

    private String getDefaultUserName()  {
	AdminConnectionFactory acf = new AdminConnectionFactory();
	String addr;

	try  {
	    addr = acf.getProperty(AdminConnectionConfiguration.imqDefaultAdminUsername);
	} catch(Exception e)  {
	    addr = null;
	}

	return (addr);
    }

    private String getDefaultPassword()  {
	AdminConnectionFactory acf = new AdminConnectionFactory();
	String addr;

	try  {
	    addr = acf.getProperty(AdminConnectionConfiguration.imqDefaultAdminPassword);
	} catch(Exception e)  {
	    addr = null;
	}

	return (addr);
    }

    private String getDefaultAddress()  {
	/*
	AdminConnectionFactory acf = new AdminConnectionFactory();
	String addr;

	try  {
	    addr = acf.getProperty(AdminConnectionConfiguration.imqAddress);
	} catch(Exception e)  {
	    addr = null;
	}

	return (addr);
	*/

	return ("localhost:7676");
    }

    public static void main(String[] args)  {
	JFrame frame;
	MQLogViewer s;
	String address = null, adminUser = null, adminPasswd = null,
		secondStr = null;
	long seconds = 5;

	for (int i = 0; i < args.length; ++i)  {
	    if (args[i].equals("-b"))  {
		if (++i >= args.length)  {
		    usage();
		}
		address = args[i];
	    } else if (args[i].equals("-u"))  {
		if (++i >= args.length)  {
		    usage();
		}
		adminUser = args[i];
	    } else if (args[i].equals("-p"))  {
		if (++i >= args.length)  {
		    usage();
		}
		adminPasswd = args[i];
	    } else  {
		usage();
	    }
	}

	frame = new JFrame("MQ Log Viewer");
	s = new MQLogViewer(frame, address, adminUser, adminPasswd);

	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		doExit();
	    }
	});
	
	frame.pack(); 
	frame.setVisible(true);
    }

    public static void usage()  {
	usage(null);
    }

    public static void usage(String msg)  {
	if (msg != null)  {
            System.err.println(msg);
	}

        System.err.println("java MQLogViewer"
	+ "[-b <host:port>] [-u <admin user name>] [-p <admin password>]");
	doExit();
    }
}
