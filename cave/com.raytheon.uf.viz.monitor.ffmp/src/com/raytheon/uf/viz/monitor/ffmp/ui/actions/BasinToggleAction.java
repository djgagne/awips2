/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.monitor.ffmp.ui.actions;

import com.raytheon.uf.viz.monitor.ffmp.FFMPMonitor;
import com.raytheon.uf.viz.monitor.ffmp.ui.rsc.FFMPResource;
import com.raytheon.viz.ui.cmenu.AbstractRightClickAction;

/**
 * FFMP Color Display toggle action.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 13, 2011            mpduff     Initial creation
 *
 * </pre>
 *
 * @author mpduff
 * @version 1.0	
 */

public class BasinToggleAction extends AbstractRightClickAction {
    @Override
    public void run() {
        FFMPResource resource = ((FFMPResource) getSelectedRsc());
        if (resource.isBasinToggle()) {
            resource.setBasinToggle(false);
            this.setChecked(false);
        } else {
            resource.setBasinToggle(true);
            this.setChecked(true);
        }
        FFMPMonitor.getInstance().fireMonitorEvent();
        resource.refresh();
    }
      
    @Override
    public boolean isHidden() {
        if (getSelectedRsc() instanceof FFMPResource) {
            return false;
        }
        return true;
    }
  
    @Override
    public String getText() {
        return "FFMP Basin Display";
    }
}
