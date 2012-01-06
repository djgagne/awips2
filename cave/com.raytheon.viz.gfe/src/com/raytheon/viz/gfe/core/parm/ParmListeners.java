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
package com.raytheon.viz.gfe.core.parm;

import org.apache.commons.lang.Validate;
import org.eclipse.core.runtime.ListenerList;

import com.raytheon.uf.common.dataplugin.gfe.db.objects.ParmID;
import com.raytheon.uf.common.dataplugin.gfe.server.lock.LockTable;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.viz.gfe.core.msgs.GridDataChangedMsg;
import com.raytheon.viz.gfe.core.msgs.IColorTableModifiedListener;
import com.raytheon.viz.gfe.core.msgs.ICombineModeChangedListener;
import com.raytheon.viz.gfe.core.msgs.IGridDataChangedListener;
import com.raytheon.viz.gfe.core.msgs.ILockTableChangedListener;
import com.raytheon.viz.gfe.core.msgs.IParameterSelectionChangedListener;
import com.raytheon.viz.gfe.core.msgs.IParmIDChangedListener;
import com.raytheon.viz.gfe.core.msgs.IParmInventoryChangedListener;
import com.raytheon.viz.gfe.core.msgs.IPickupValueChangedListener;
import com.raytheon.viz.gfe.core.msgs.ISelectionTimeRangeChangedListener;
import com.raytheon.viz.gfe.core.msgs.IVectorModeChangedListener;
import com.raytheon.viz.gfe.core.parm.ParmState.CombineMode;
import com.raytheon.viz.gfe.core.parm.ParmState.VectorMode;
import com.raytheon.viz.gfe.core.wxvalue.WxValue;

/**
 * 
 * Contains all of the listeners for parms
 * 
 * This is an attempt to reduce the size of Parm by putting all parm-related
 * messaging in one place. There is one instance of this class per Parm.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date			Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * Jun 13, 2008				chammack	Initial creation
 * Sep 01, 2009       #2788 randerso    Changed listener lists to sets to prevent
 *                                      multiple registration
 * 
 * </pre>
 * 
 * @author chammack
 * @version 1.0
 */

public class ParmListeners {

    private final ListenerList gridChangedListeners;

    private final ListenerList parmInventoryChangedListeners;

    private final ListenerList parmIDChangedListeners;

    private final ListenerList selectionTimeRangeChangedListeners;

    private final ListenerList parameterSelectionChangedListeners;

    private final ListenerList combineModeChangedListeners;

    private final ListenerList vectorModeChangedListeners;

    private final ListenerList pickupValueChangedListeners;

    private final ListenerList colorTableModifiedListeners;

    private final ListenerList lockTableChangedListeners;

    protected ParmListeners() {
        this.gridChangedListeners = new ListenerList();
        this.parmInventoryChangedListeners = new ListenerList();
        this.parmIDChangedListeners = new ListenerList();
        this.selectionTimeRangeChangedListeners = new ListenerList();
        this.parameterSelectionChangedListeners = new ListenerList();
        this.combineModeChangedListeners = new ListenerList();
        this.vectorModeChangedListeners = new ListenerList();
        this.pickupValueChangedListeners = new ListenerList();
        this.colorTableModifiedListeners = new ListenerList();
        this.lockTableChangedListeners = new ListenerList();
    }

    public void fireGridChangedListener(ParmID parmID, TimeRange validTime) {
        for (Object listener : this.gridChangedListeners.getListeners()) {
            ((IGridDataChangedListener) listener).gridDataChanged(parmID,
                    validTime);
        }

        new GridDataChangedMsg(parmID, validTime).send();
    }

    protected void fireParmInventoryChangedListener(Parm parm,
            TimeRange validTime) {
        for (Object listener : this.parmInventoryChangedListeners
                .getListeners()) {
            ((IParmInventoryChangedListener) listener).parmInventoryChanged(
                    parm, validTime);
        }
    }

    protected void fireParmIDChangedListener(Parm parm, ParmID newParmID) {
        for (Object listener : this.parmIDChangedListeners.getListeners()) {
            ((IParmIDChangedListener) listener).parmIDChanged(parm, newParmID);
        }
    }

    public void fireSelectionTimeRangeChanged(Parm parm,
            TimeRange selectionTimeRange) {
        for (Object listener : this.selectionTimeRangeChangedListeners
                .getListeners()) {
            ((ISelectionTimeRangeChangedListener) listener)
                    .selectionTimeRangeChanged(parm, selectionTimeRange);
        }
    }

    public void fireParameterSelectionChangedListener(Parm parm,
            boolean selected) {
        for (Object listener : this.parameterSelectionChangedListeners
                .getListeners()) {
            ((IParameterSelectionChangedListener) listener)
                    .parameterSelectionChanged(parm, selected);
        }
    }

    public void fireCombineModeChangedListener(Parm parm, CombineMode mode) {
        for (Object listener : this.combineModeChangedListeners.getListeners()) {
            ((ICombineModeChangedListener) listener).combineModeChanged(parm,
                    mode);
        }
    }

    public void fireVectorModeChangedListener(Parm parm, VectorMode mode) {
        for (Object listener : this.vectorModeChangedListeners.getListeners()) {
            ((IVectorModeChangedListener) listener).vectorModeChanged(parm,
                    mode);
        }
    }

    public void firePickupValueChangedListener(Parm parm, WxValue pickupValue) {
        for (Object listener : this.pickupValueChangedListeners.getListeners()) {
            ((IPickupValueChangedListener) listener).pickupValueChanged(parm,
                    pickupValue);
        }
    }

    public void fireColorTableModified(Parm parm) {
        for (Object listener : this.colorTableModifiedListeners.getListeners()) {
            ((IColorTableModifiedListener) listener).colorTableModified(parm);
        }
    }

    public void fireLockTableChanged(Parm parm, LockTable lockTable) {
        for (Object listener : this.lockTableChangedListeners.getListeners()) {
            ((ILockTableChangedListener) listener).lockTableChanged(parm,
                    lockTable);
        }
    }

    /**
     * Add grid changed listener
     * 
     * @param listener
     */
    public void addGridChangedListener(IGridDataChangedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.gridChangedListeners.add(listener);
    }

    /**
     * Remove grid changed listener
     * 
     * @param listener
     */
    public void removeGridChangedListener(IGridDataChangedListener listener) {
        this.gridChangedListeners.remove(listener);
    }

    /**
     * Add parm inventory changed listener
     * 
     * @param listener
     */
    public void addParmInventoryChangedListener(
            IParmInventoryChangedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.parmInventoryChangedListeners.add(listener);
    }

    /**
     * Remove parm inventory changed listener
     * 
     * @param listener
     */
    public void removeParmInventoryChangedListener(
            IParmInventoryChangedListener listener) {
        this.parmInventoryChangedListeners.remove(listener);
    }

    /**
     * Add parm id changed listener
     * 
     * @param listener
     */
    public void addParmIDChangedListener(IParmIDChangedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.parmIDChangedListeners.add(listener);
    }

    /**
     * Remove parm id changed listener
     * 
     * @param listener
     */
    public void removeParmIDChangedListener(IParmIDChangedListener listener) {
        this.parmIDChangedListeners.remove(listener);
    }

    /**
     * Add selection time range changed listener
     * 
     * @param listener
     */
    public void addSelectionTimeRangeChangedListener(
            ISelectionTimeRangeChangedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.selectionTimeRangeChangedListeners.add(listener);
    }

    /**
     * Remove selection time range changed listener
     * 
     * @param listener
     */
    public void removeSelectionTimeRangeChangedListener(
            ISelectionTimeRangeChangedListener listener) {
        this.selectionTimeRangeChangedListeners.remove(listener);
    }

    /**
     * Add parameter selection changed listener
     * 
     * @param listener
     */
    public void addParameterSelectionChangedListener(
            IParameterSelectionChangedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.parameterSelectionChangedListeners.add(listener);
    }

    /**
     * Remove parameter selection changed listener
     * 
     * @param listener
     */
    public void removeParameterSelectionChangedListener(
            IParameterSelectionChangedListener listener) {
        this.parameterSelectionChangedListeners.remove(listener);
    }

    /**
     * Add combine mode changed listener
     * 
     * @param listener
     */
    public void addCombineModeChangedListener(
            ICombineModeChangedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.combineModeChangedListeners.add(listener);
    }

    /**
     * Remove combine mode changed listener
     * 
     * @param listener
     */
    public void removeCombineModeChangedListener(
            ICombineModeChangedListener listener) {
        this.combineModeChangedListeners.remove(listener);
    }

    /**
     * Add vector mode changed listener
     * 
     * @param listener
     */
    public void addVectorModeChangedListener(IVectorModeChangedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.vectorModeChangedListeners.add(listener);
    }

    /**
     * Remove vector mode changed listener
     * 
     * @param listener
     */
    public void removeVectorModeChangedListener(
            IVectorModeChangedListener listener) {
        this.vectorModeChangedListeners.remove(listener);
    }

    /**
     * Add pickup value changed listener
     * 
     * @param listener
     */
    public void addPickupValueChangedListener(
            IPickupValueChangedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.pickupValueChangedListeners.add(listener);
    }

    /**
     * Remove pickup value changed listener
     * 
     * @param listener
     */
    public void removePickupValueChangedListener(
            IPickupValueChangedListener listener) {
        this.pickupValueChangedListeners.remove(listener);
    }

    /**
     * Add color table modified listener
     * 
     * @param listener
     */
    public void addColorTableModifiedListener(
            IColorTableModifiedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.colorTableModifiedListeners.add(listener);
    }

    /**
     * Remove color table modified listener
     * 
     * @param listener
     */
    public void removeColorTableModifiedListener(
            IColorTableModifiedListener listener) {
        this.colorTableModifiedListeners.remove(listener);
    }

    /**
     * Add lock table changed listener
     * 
     * @param listener
     */
    public void addLockTableChangedListener(ILockTableChangedListener listener) {
        Validate.notNull(listener, "Attempting to add null listener");
        this.lockTableChangedListeners.add(listener);
    }

    /**
     * Remove lock table changed listener
     * 
     * @param listener
     */
    public void removeLockTableChangedListener(
            ILockTableChangedListener listener) {
        this.lockTableChangedListeners.remove(listener);
    }

}
