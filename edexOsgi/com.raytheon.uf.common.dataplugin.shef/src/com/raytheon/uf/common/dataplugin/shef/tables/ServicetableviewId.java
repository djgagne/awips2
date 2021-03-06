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
package com.raytheon.uf.common.dataplugin.shef.tables;
// default package
// Generated Oct 17, 2008 2:22:17 PM by Hibernate Tools 3.2.2.GA

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * ServicetableviewId generated by hbm2java
 * 
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 17, 2008                        Initial generation by hbm2java
 * Aug 19, 2011      10672     jkorman Move refactor to new project
 * Oct 07, 2013       2361     njensen Removed XML annotations
 * 
 * </pre>
 * 
 * @author jkorman
 * @version 1.1
 */
@Embeddable
@com.raytheon.uf.common.serialization.annotations.DynamicSerialize
public class ServicetableviewId extends com.raytheon.uf.common.dataplugin.persist.PersistableDataObject implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String lid;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String name;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String stream;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String state;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String county;

    @com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement
    private String hsa;

    public ServicetableviewId() {
    }

    public ServicetableviewId(String lid, String name, String stream,
            String state, String county, String hsa) {
        this.lid = lid;
        this.name = name;
        this.stream = stream;
        this.state = state;
        this.county = county;
        this.hsa = hsa;
    }

    @Column(name = "lid", length = 8)
    public String getLid() {
        return this.lid;
    }

    public void setLid(String lid) {
        this.lid = lid;
    }

    @Column(name = "name", length = 50)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "stream", length = 32)
    public String getStream() {
        return this.stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    @Column(name = "state", length = 2)
    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "county", length = 20)
    public String getCounty() {
        return this.county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    @Column(name = "hsa", length = 3)
    public String getHsa() {
        return this.hsa;
    }

    public void setHsa(String hsa) {
        this.hsa = hsa;
    }

    public boolean equals(Object other) {
        if ((this == other))
            return true;
        if ((other == null))
            return false;
        if (!(other instanceof ServicetableviewId))
            return false;
        ServicetableviewId castOther = (ServicetableviewId) other;

        return ((this.getLid() == castOther.getLid()) || (this.getLid() != null
                && castOther.getLid() != null && this.getLid().equals(
                castOther.getLid())))
                && ((this.getName() == castOther.getName()) || (this.getName() != null
                        && castOther.getName() != null && this.getName()
                        .equals(castOther.getName())))
                && ((this.getStream() == castOther.getStream()) || (this
                        .getStream() != null
                        && castOther.getStream() != null && this.getStream()
                        .equals(castOther.getStream())))
                && ((this.getState() == castOther.getState()) || (this
                        .getState() != null
                        && castOther.getState() != null && this.getState()
                        .equals(castOther.getState())))
                && ((this.getCounty() == castOther.getCounty()) || (this
                        .getCounty() != null
                        && castOther.getCounty() != null && this.getCounty()
                        .equals(castOther.getCounty())))
                && ((this.getHsa() == castOther.getHsa()) || (this.getHsa() != null
                        && castOther.getHsa() != null && this.getHsa().equals(
                        castOther.getHsa())));
    }

    public int hashCode() {
        int result = 17;

        result = 37 * result
                + (getLid() == null ? 0 : this.getLid().hashCode());
        result = 37 * result
                + (getName() == null ? 0 : this.getName().hashCode());
        result = 37 * result
                + (getStream() == null ? 0 : this.getStream().hashCode());
        result = 37 * result
                + (getState() == null ? 0 : this.getState().hashCode());
        result = 37 * result
                + (getCounty() == null ? 0 : this.getCounty().hashCode());
        result = 37 * result
                + (getHsa() == null ? 0 : this.getHsa().hashCode());
        return result;
    }

}
