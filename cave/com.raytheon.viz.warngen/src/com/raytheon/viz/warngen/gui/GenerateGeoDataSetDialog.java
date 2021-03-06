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
package com.raytheon.viz.warngen.gui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

import com.raytheon.uf.common.dataplugin.warning.gis.GeospatialFactory;
import com.raytheon.uf.common.dataplugin.warning.gis.GeospatialMetadata;
import com.raytheon.uf.common.geospatial.SpatialException;
import com.raytheon.uf.viz.core.VizApp;
import com.raytheon.viz.ui.dialogs.CaveSWTDialog;

/**
 * A blocking modal dialog that generates a geometry spatial data set. This
 * dialog will only display in the rare scenario where the Geospatial data is
 * not ready. It conveys to the user that the data is still being created and
 * will be ready soon.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 20, 2014 3353       rferrel     Initial creation
 * Feb 26, 2015 3353       rjpeter     Make modal optional.
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */

public class GenerateGeoDataSetDialog extends CaveSWTDialog {
    private final String site;

    private final GeospatialMetadata gmd;

    private ProgressBar progressBar;

    private final String messageFormat = "Please wait. Generating Geometry Spatial Data Set for %s.\nThis may take several minutes.";

    /**
     * @param parentShell
     * @param site
     * @param gmd
     * @param modal
     */
    protected GenerateGeoDataSetDialog(Shell parentShell, String site,
            GeospatialMetadata gmd, boolean modal) {
        /*
         * This needs to be a blocking dialog. The return value is used to
         * placed an entry in WarngenLayer's siteMap. Several layers of calls in
         * both WarngenLayer and WarngenDialog assume the siteMap contains the
         * values generated by this dialog.
         * 
         * If made non-blocking these layers of calls would need a non-blocking
         * way of being informed when the siteMap is updated. Also synchronize
         * on siteMap become more complicated.
         */
        super(parentShell, (modal ? SWT.PRIMARY_MODAL : 0) | SWT.BORDER,
                CAVE.NONE);
        this.site = site;
        this.gmd = gmd;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.viz.ui.dialogs.CaveSWTDialogBase#initializeComponents(org
     * .eclipse.swt.widgets.Shell)
     */
    @Override
    protected void initializeComponents(Shell shell) {
        shell.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
        Composite mainComposite = new Composite(shell, SWT.NONE);
        GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = 2;
        gl.marginHeight = 1;
        gl.marginWidth = 1;
        mainComposite.setLayout(gl);
        Label label = new Label(mainComposite, SWT.NONE);
        label.setText(String.format(messageFormat, site));
        label.setBackground(shell.getBackground());
        progressBar = new ProgressBar(mainComposite, SWT.DEFAULT);
        GridData gd = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
        progressBar.setLayoutData(gd);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.dialogs.CaveSWTDialog#preOpened()
     */
    @Override
    protected void preOpened() {
        super.preOpened();
        progressBar.setSelection(progressBar.getMinimum());
        progressBar.setSelection(SWT.NORMAL);
        setShellCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
        Job job = new Job("Generate GeoSpatial data set") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    setReturnValue(GeospatialFactory.generateGeospatialDataSet(
                            site, gmd));
                } catch (SpatialException e) {
                    setReturnValue(e);
                } finally {
                    VizApp.runAsync(new Runnable() {

                        @Override
                        public void run() {
                            setShellCursor(null);
                            close();
                        }
                    });
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }

    private void setShellCursor(Cursor cursor) {
        if (!shell.isDisposed()) {
            shell.setCursor(cursor);
            Composite comp = shell.getParent();
            while (comp != null) {
                Shell parentShell = comp.getShell();
                if (parentShell.isDisposed()) {
                    break;
                }
                parentShell.setCursor(cursor);
                comp = parentShell.getParent();
            }
        }
    }
}
