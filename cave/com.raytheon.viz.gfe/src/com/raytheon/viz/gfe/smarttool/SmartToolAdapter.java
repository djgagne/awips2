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
package com.raytheon.viz.gfe.smarttool;

import org.eclipse.jface.action.IMenuManager;

import com.raytheon.uf.viz.localization.perspective.adapter.LocalizationPerspectiveAdapter;
import com.raytheon.uf.viz.localization.perspective.view.FileTreeEntryData;
import com.raytheon.uf.viz.localization.perspective.view.LocalizationFileEntryData;
import com.raytheon.uf.viz.localization.perspective.view.LocalizationFileGroupData;
import com.raytheon.viz.gfe.smarttool.action.NewAction;

/**
 * Localization Adapter for Smart Tools
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 27, 2010            mpduff     Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 */

public class SmartToolAdapter extends LocalizationPerspectiveAdapter {

    @Override
    public boolean addContextMenuItems(IMenuManager menuMgr,
            FileTreeEntryData[] selectedData) {
        if (selectedData.length == 1) {
            FileTreeEntryData data = selectedData[0];
            String name = data.getName();

            NewAction newAction = new NewAction(name, false);
            menuMgr.add(newAction);

            if (selectedData[0] instanceof LocalizationFileGroupData
                    || selectedData[0] instanceof LocalizationFileEntryData) {
                menuMgr.add(new InfoAction(name));
            }

            return true;
        }
        return false;
    }
}
