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
 * @(#)MemoryLevelHandler.java	1.12 06/29/07
 */ 
 
package com.sun.messaging.jmq.jmsserver.memory;


import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.config.*;
import com.sun.messaging.jmq.util.log.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Parent of all level (green,red, etc) memory managers.<P>
 *
 * Assumes that it is never called from two different threads
 * at the same time !!!
 */

public abstract class MemoryLevelHandler
{
    protected Logger logger = Globals.getLogger();

    private static boolean DEBUG = false;

    protected String MEMORY_NAME_KEY = null; // localization

    protected String localLevelName = null;

    protected int threshold = 0;

    protected int timeBetweenChecks = 0;


    /**
     * reflects if the broker is still in 
     * this level
     */
    protected boolean inLevel = false;

    /**
     * The time this level was entered (entered
     * was called 
     */
    protected long enteredLevelTime = 0;

    /**
     * Cumulitive time in this level 
     */
    protected long totalTimeInLevel = 0;

    /**
     * total time cleanup has been called
     */
    protected long totalCleanupCount = 0;

    /**
     * total # of times we have been in this
     * level (since broker started)
     */
    protected long totalTimesEnteredLevel = 0;


    protected String levelName = "none";

    protected long MAX_MEMORY_DELTA = 1024*10; 

    protected long LEVEL_DELTA = 1024; 

    protected final static int NEVER_GC = 0;
    protected final static int PAUSED = 0;

    public MemoryLevelHandler(String levelName) {
        this.levelName = levelName;
        threshold = Globals.getConfig().getIntProperty(
                Globals.IMQ + "." + levelName + ".threshold", 0);
        timeBetweenChecks = Globals.getConfig().getIntProperty(
                Globals.IMQ + "." + levelName + ".seconds", 5)
                 * 1000;

    }

    public Hashtable getDebugState() {
        Hashtable ht = new Hashtable();
        ht.put("levelName", levelName);
        ht.put("threshold", new Integer(threshold));
        ht.put("timeBetweenChecks", new Integer(timeBetweenChecks));
        ht.put("threshold", new Integer(threshold));
        ht.put("enteredLevelTime", new Long(enteredLevelTime));
        ht.put("totalTimeInLevel", new Long(totalTimeInLevel));
        ht.put("totalCleanupCount", new Long(totalCleanupCount));
        ht.put("totalTimesEnteredLevel", new Long(totalTimesEnteredLevel));
        ht.put("MAX_MEMORY_DELTA", new Long(MAX_MEMORY_DELTA));
        ht.put("LEVEL_DELTA", new Long(LEVEL_DELTA));
        ht.put("NEVER_GC", new Integer(NEVER_GC));
        ht.put("PAUSED", new Integer(PAUSED));
        ht.put("gcCount", new Integer( gcCount()));
        ht.put("gcIteration", new Integer( gcIteration()));
        ht.put("inLevel", Boolean.valueOf( inLevel));
        return ht;
    }


    public int getThresholdPercent() {
        return threshold;
    }

    public int getTimeBetweenChecks() {
        return timeBetweenChecks;
    }


    /**
     * Returns the current message count (JMQSize) per connection,
     * at this time, for this level. The routine may (or may not) 
     * use the passed in parameters.
     * 
     * @param freeMem the current free memory available in the system
     * @param producers the current number of producers available in the system
     */ 
    public abstract int getMessageCount(long freeMem, int producers);

    /**
     * Returns the current message bytes (JMQBytes) per connection, 
     * at this time, for this level. The routine may (or may not) 
     * use the passed in parameters.
     * 
     * @param freeMem the current free memory available in the system
     * @param producers the current number of producers available in the system
     */ 
    public abstract long getMemory(long freeMem, int producers);


    /**
     * method called when the broker initially enters the memory level, allows
     * the broker to clean up memory.  This routine will be called until either:
     * <UL><LI>prepare returns true (indicating its done all it can) </LI>
     *     <LI>enough memory is freed to return to the previous level</LI></UL>
     *
     * @returns true if all freeing has completed, false there may be 
     *                more work to do
     */
    public boolean cleanup(int iteration) {

        if (DEBUG) {
            logger.log(Logger.DEBUG, "MM: cleanup() " + toDebugString());
        }
        totalCleanupCount ++;
        return true; // done all it can
    }

    /**
     * Client has officially entered this level (prepare has completed and the
     * system is still in the same state). (e.g. entered Yellow from Green)
     *
     * @returns if true, tells client to send out state change notification
     */
    public boolean enter(boolean fromHigherLevel) {
        if (DEBUG) {
            logger.log(Logger.DEBUG, "MM: enter(" + fromHigherLevel 
                           + ") " + toDebugString());
        }

        enteredLevelTime = System.currentTimeMillis();
        totalTimesEnteredLevel ++;
        inLevel = true;
        return true;
    }

    /**
     * Client has left the state and moved to a different state (e.g. entered
     * Green from Yellow)
     * @param higherLevel true if we have moved to a higher level, false
     *        otherwise
     *
     * @returns if true, tells client to send out state change notification
     */
    public boolean leave(boolean toHigherLevel) {
        if (DEBUG) {
            logger.log(Logger.DEBUG, "MM: leave(" + toHigherLevel + ") " 
                      + toDebugString());
        }
        inLevel = false;
        totalTimeInLevel += System.currentTimeMillis() - enteredLevelTime;
        enteredLevelTime = 0;
        return false;
    }

    /**
     * number of gc's to call when the broker enters the level, to make sure 
     * that the memory footprint is accurate
     *
     * @returns number of gc's to call when state changes
     */
    public abstract int gcCount();

    /**
     * how often to call gc (based on the memory mgr thread iterations)
     * in this level (0 indicated never -> which means gc is called at most
     * during the calculations to determine if we have entered the level).
     *
     * @returns how many iterations between gc (0 indicates never gc)
     */
    public abstract int gcIteration();


    /**
     * string representing  the object
     */
    public String toString()
    {
        return "MemoryLevelHandler["+levelName() + "]";
    }


    /**
     * name of the level (for debug/diag purposes)
     */
    public String levelName() {
        return levelName;
    }

    public String localizedLevelName() {
        if (localLevelName == null) {
            if (MEMORY_NAME_KEY != null) {
                localLevelName = Globals.getBrokerResources().getString(
                    MEMORY_NAME_KEY, levelName);
            } else {
                localLevelName = levelName;
            }
        }
        return localLevelName;
    }

// diag get/set methods

    public long getTotalTimeInLevel() {
        return totalTimeInLevel + getCurrentTimeInLevel();
    }
    public boolean getIsInLevel() {
        return inLevel;
    }

    public long getTotalCleanupCount() {
        return totalCleanupCount;
    }

    public long getTotalTimesEnteredLevel() {
        return totalTimesEnteredLevel;
    }

    public long getCurrentTimeInLevel() {
        if (enteredLevelTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - enteredLevelTime;    
    }


    public String toDebugString() {
        return toString() +"\n\t"+ " inLevel=" +inLevel+"\n\t"
              + ", ThresholdPercent " + getThresholdPercent() +"\n\t"
              + ", totalTimeInLevel " + getTotalTimeInLevel() +"\n\t"
              + ", TotalCleanupCount " + getTotalCleanupCount() +"\n\t"
              + ", totalTimesEnteredLevel " 
                       + getTotalTimesEnteredLevel() +"\n\t"
              + ", CurrentTimeInLevel " + getCurrentTimeInLevel()+"\n\t" 
              + ", TotalCleanupCount " + getTotalCleanupCount() +"\n\t"
              + ", gcCount " + gcCount() +"\n\t"
              + ", timeBetweenChecks " + (timeBetweenChecks/1000) +" sec\n\t"
              + ", gcIteration " + gcIteration();
    }
}
