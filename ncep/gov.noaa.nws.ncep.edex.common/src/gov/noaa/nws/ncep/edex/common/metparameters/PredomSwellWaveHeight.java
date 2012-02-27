package gov.noaa.nws.ncep.edex.common.metparameters;


import javax.measure.quantity.Length;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;

/**
 * Maps to the GEMPAK parameter HOSW
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize


public class PredomSwellWaveHeight extends AbstractMetParameter implements Length, ISerializableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4506732984397655917L;

	public PredomSwellWaveHeight() {
		super( UNIT );
	}
}
