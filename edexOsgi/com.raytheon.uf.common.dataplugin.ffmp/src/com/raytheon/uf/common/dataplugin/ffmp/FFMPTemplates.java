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
package com.raytheon.uf.common.dataplugin.ffmp;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.raytheon.uf.common.datastorage.StorageException;
import com.raytheon.uf.common.gridcoverage.GridCoverage;
import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.monitor.config.FFMPRunConfigurationManager;
import com.raytheon.uf.common.monitor.config.FFMPSourceConfigurationManager;
import com.raytheon.uf.common.monitor.config.FFMPTemplateConfigurationManager;
import com.raytheon.uf.common.monitor.scan.ScanUtils;
import com.raytheon.uf.common.monitor.xml.DomainXML;
import com.raytheon.uf.common.monitor.xml.FFMPRunXML;
import com.raytheon.uf.common.monitor.xml.ProductRunXML;
import com.raytheon.uf.common.monitor.xml.SourceXML;
import com.raytheon.uf.common.serialization.SerializationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.common.xmrg.hrap.HRAPCoordinates;
import com.raytheon.uf.common.xmrg.hrap.HRAPSubGrid;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.io.WKBReader;

/**
 * FFMPHucTemplate maker/factory
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * 07/29/09      2152       D. Hladky   Initial release
 * 07/09/10      3914       D. Hladky   Localization work
 * 12/13/10      7484       D. Hladky   Service Backup
 * 02/01/13      1569       D.Hladky    Constants
 * 03/01/13      13228      G. Zhang    Add VGB county and related code
 * 02/20/13      1635       D. Hladky   Constants
 * 03/18/13      1817       D. Hladky   Fixed issue with BOX where only 1 HUC was showing up.
 * 04/15/13      1902       M. Duff     Generic List
 * 06/10/13      2085       njensen     Use countyMap for efficiency
 * 07/01/13      2155       dhladky     Fixed duplicate pfafs that were in domainList arrays from overlapping domains.
 * 07/15/13      2184       dhladky     Remove all HUC's for storage except ALL
 * Nov 18, 2014  3831       dhladky     StatusHandler logging. Proper list sizing. Geometry chunk sizing.
 * Aug 08, 2015  4722       dhladky     Improved Grid support.
 * Nov 12, 2015  4834       njensen     Changed LocalizationOpFailedException to LocalizationException
 * Feb 15, 2016  5244       nabowle     Replace deprecated LocalizationFile methods.
 * Apr 07, 2016  5491       tjensen     Fix NullPointerException from getRawGeometries
 * Aug 09, 2016  5819       mapeters    Template files moved from SITE to CONFIGURED
 * Apr 12, 2017  DR 11250   lshi        VGBs inclusion for localization was incorrect
 * May 18, 2017  6266       nabowle     Removed mode from FFMPUtils.getRadarCenter(). Code cleanup.
 *
 * </pre>
 *
 * @author dhladky
 */

public class FFMPTemplates {

    /** Template file location dir **/
    private String TEMPLATE_FILE_LOC = null;

    /** County Warning Area **/
    private DomainXML primaryCWA = null;

    /** Domains to load **/
    private ArrayList<DomainXML> domains = new ArrayList<>();

    /** HASH of the aggregates, lists of pfafs **/
    private HashMap<String, HashMap<String, HashMap<String, LinkedHashMap<Long, ?>>>> domainMap = null;

    private HashMap<String, HashMap<String, HashMap<Long, ArrayList<FFMPVirtualGageBasinMetaData>>>> virtualGageBasinsInParentPfaf = null;

    private HashMap<String, HashMap<String, HashMap<String, ArrayList<FFMPVirtualGageBasinMetaData>>>> vgbsInCounty = null;

    private HashMap<String, HashMap<String, LinkedHashMap<String, FFMPVirtualGageBasinMetaData>>> virtualDomainMap = null;

    private final Map<String, SoftReference<Map<Long, Geometry>>> cwaRawGeometries = new ConcurrentHashMap<>();

    /** Singleton instance of this class */
    private static FFMPTemplates template = null;

    /** Mode of operation **/
    private MODE mode = null;

    /** Start depth for HUC lookups **/
    private int hucDepthStart = 4;

    /** virtual basins **/
    private boolean virtual = false;

    /** Default max extent buffer **/
    private double maxExtent = 0.0f;

    /** number of HUC levels to use **/
    private int totalHucLevels = 0;

    /** Template configuration **/
    private FFMPTemplateConfigurationManager ftcm = null;

    /** Runner configuration **/
    private FFMPRunConfigurationManager frcm = null;

    /** The FFMP **/
    private FFMPRunXML runner = null;

    /** geometry factory **/
    public static final GeometryFactory factory = new GeometryFactory();

    /** ALL huc key **/
    public static final long ALL_HUC_KEY = 0L;

    public boolean done = false;

    private final Map<Long, FFMPCounty> countyMap = new HashMap<>();

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(FFMPTemplates.class);

    private final IPathManager pathManager;

    /**
     * Single constructor
     *
     * @return
     */
    public static synchronized FFMPTemplates getInstance(DomainXML domain,
            String siteKey, MODE mode) {

        if (template == null) {
            template = new FFMPTemplates(domain, mode);
            template.loadTemplate(siteKey, domain.getCwa(), domain.isPrimary());
        }
        return template;
    }

    /**
     * Multiple constructor loads multiple
     *
     * @return
     */
    public static synchronized FFMPTemplates getInstance(DomainXML primaryCWA,
            MODE mode) {

        if (template == null) {
            template = new FFMPTemplates(primaryCWA, mode);
            template.addDomain(primaryCWA);
        }
        return template;
    }

    /**
     * EDEX constructor
     *
     * @param primaryCWA
     * @param mode
     */
    public FFMPTemplates(DomainXML primaryCWA, MODE mode) {
        this.pathManager = PathManagerFactory.getPathManager();
        this.mode = mode;
        this.primaryCWA = primaryCWA;
        init();
    }

    public enum MODE {

        CAVE("CAVE"), EDEX("EDEX");

        private final String mode;

        private MODE(String mode) {
            this.mode = mode;
        }

        public String getMode() {
            return mode;
        }
    }

    /**
     * localization and such
     */
    private void init() {

        TEMPLATE_FILE_LOC = "ffmp" + IPathManager.SEPARATOR + "templates"
                + IPathManager.SEPARATOR;

        domainMap = new HashMap<>();
        virtualDomainMap = new HashMap<>();
        virtualGageBasinsInParentPfaf = new HashMap<>();
        ftcm = FFMPTemplateConfigurationManager.getInstance();
        frcm = FFMPRunConfigurationManager.getInstance();
        vgbsInCounty = new HashMap<>();

        try {
            ftcm.readConfigXml();

            if (ftcm.isRegenerate() && (mode == MODE.EDEX)) {
                dumpTemplates();
            }

            setHucDepthStart(ftcm.getHucDepth());
            setTotalHucLevels(ftcm.getNumberOfHuc());
            setMaxExtent(ftcm.getExtents());
            setVirtual(ftcm.getVirtual());
            domains.add(primaryCWA);

        } catch (Exception e) {
            if (mode == MODE.EDEX) {
                statusHandler.handle(Priority.PROBLEM,
                        "No configuration file found, default settings applied");

                // we use 4 because it is the 90% solution as a start point for
                // the analysis. Added check to make sure at least 2 HUC layers
                // are created.
                int preliminarystart = 4;
                // first crack
                List<Integer> hucParams = FFMPUtils.getHucParameters(
                        preliminarystart, primaryCWA.getCwa());
                int startDepth = hucParams.get(0);
                int numlevels = hucParams.get(1);
                int i = 1;
                // recursively call until we have two layers
                while (numlevels < 2) {
                    int checkDepth = preliminarystart - i;
                    hucParams = FFMPUtils.getHucParameters(checkDepth,
                            primaryCWA.getCwa());
                    startDepth = hucParams.get(0);
                    numlevels = hucParams.get(1);
                    i++;

                    // safety value in case it just won't work with this shape
                    if (checkDepth == 0) {
                        // bail, won't work
                        statusHandler.handle(Priority.ERROR,
                                "Cannot create a good template. There are not enough unique HUC's to create more than 1 layer.");
                        return;
                    }
                }

                setHucDepthStart(startDepth);
                setTotalHucLevels(numlevels);
                setExtents(20000.0);
                setVirtual(true);

                ArrayList<String> vgbs = new ArrayList<>();
                domains.add(primaryCWA);

                // create a new entry
                ftcm.setExcludedVGBs(vgbs);
                ftcm.setExtents(getMaxExtent());
                ftcm.setHucDepth(getHucDepthStart());
                ftcm.setNumberOfHuc(getTotalHucLevels());
                ftcm.setVirtual(true);
                ftcm.setRegenerate(false);
                ftcm.saveConfigXml();

            } else {
                statusHandler.handle(Priority.ERROR,
                        "No configuration files can be created, can not create templates");
            }
        }
    }

    public void setTotalHucLevels(int totalHucLevels) {
        this.totalHucLevels = totalHucLevels;
    }

    public int getTotalHucLevels() {
        return totalHucLevels;
    }

    /**
     * Gets the FFMPBasinData template file/object
     *
     * @param hucName
     * @param cwa
     * @return
     * @throws StorageException
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<Long, ?> readTemplateFile(String dataKey, String huc,
            String cwa) {
        LinkedHashMap<Long, ?> map = null;
        long[] list = readDomainList(huc, cwa, dataKey);

        if (huc.equals(FFMPRecord.ALL)) {
            map = new LinkedHashMap<>(list.length, 1.0f);
            Map<Long, FFMPBasinMetaData> protoMap = (Map<Long, FFMPBasinMetaData>) readDomainMap(
                    dataKey, huc, cwa);
            // add them to the master hash
            for (long l : list) {
                ((Map<Long, FFMPBasinMetaData>) map).put(l, protoMap.get(l));
            }
        } else {
            map = fromPrimitive(
                    (Map<Long, long[]>) readDomainMap(dataKey, huc, cwa), list);
        }

        return map;
    }

    /**
     * Gets the FFMPBasinData template file/object
     *
     * @param hucName
     * @param cwa
     * @return
     */
    private LinkedHashMap<String, FFMPVirtualGageBasinMetaData> readVGBFile(
            String name, String cwa, String dataKey) {

        HashMap<String, FFMPVirtualGageBasinMetaData> protoMap = readVGBDomainMap(
                dataKey, cwa);
        String[] list = readVGBDomainList(dataKey, cwa);
        LinkedHashMap<String, FFMPVirtualGageBasinMetaData> map = new LinkedHashMap<>(
                list.length, 1.0f);

        // construct ordered map
        for (String lid : list) {     
            map.put(lid, protoMap.get(lid));
        }

        return map;
    }

    /**
     * Writes out the byte array from thrift
     *
     * @param map
     * @param cwa
     * @param dataKey
     * @throws IOException
     */
    private void writeVGBFile(Map<String, FFMPVirtualGageBasinMetaData> map,
            String cwa, String dataKey) {

        String[] list = new String[map.keySet().size()];

        int i = 0;
        for (String lid : map.keySet()) {
            list[i] = lid;
            i++;
        }

        try {
            LocalizationContext lc = pathManager.getContext(
                    LocalizationType.COMMON_STATIC,
                    LocalizationLevel.CONFIGURED);
            ILocalizationFile lfmap = pathManager.getLocalizationFile(lc,
                    getAbsoluteFileName(dataKey, "VIRTUAL", cwa, "map"));
            ILocalizationFile lflist = pathManager.getLocalizationFile(lc,
                    getAbsoluteFileName(dataKey, "VIRTUAL", cwa, "list"));

            try (SaveableOutputStream listSos = lflist.openOutputStream();
                    GZIPOutputStream listGos = new GZIPOutputStream(listSos);
                    SaveableOutputStream mapSos = lfmap.openOutputStream();
                    GZIPOutputStream mapGos = new GZIPOutputStream(mapSos)) {

                listGos.write(SerializationUtil.transformToThrift(list));
                listGos.finish();
                listGos.flush();
                listSos.save();

                mapGos.write(SerializationUtil.transformToThrift(map));
                mapGos.finish();
                mapGos.flush();
                mapSos.save();
            }

            list = null;

        } catch (SerializationException se) {
            statusHandler.error("Serialization Exception: Write VGB: cwa: "
                    + cwa + " dataKey: " + dataKey, se);
        } catch (FileNotFoundException fnfe) {
            statusHandler.error("File Not found Exception: Write VGB: cwa: "
                    + cwa + " dataKey: " + dataKey, fnfe);
        } catch (IOException ioe) {
            statusHandler.error("IO Exception: Write VGB: cwa: " + cwa
                    + " dataKey: " + dataKey, ioe);
        } catch (LocalizationException e) {
            statusHandler.error("Localization Exception: Write VGB: cwa: " + cwa
                    + " dataKey: " + dataKey, e);
        }
    }

    /**
     * Writes out the byte array from thrift
     *
     * @param huc
     * @param cwa
     * @param map
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private void writeTemplateFile(String dataKey, String huc, String cwa,
            LinkedHashMap<Long, ?> map) {

        int x = 0;
        long[] list = new long[map.keySet().size()];
        for (long l : map.keySet()) {
            list[x] = l;
            x++;
        }

        try {
            LocalizationContext lc = pathManager.getContext(
                    LocalizationType.COMMON_STATIC,
                    LocalizationLevel.CONFIGURED);

            ILocalizationFile lflist = pathManager.getLocalizationFile(lc,
                    getAbsoluteFileName(dataKey, huc, cwa, "list"));
            ILocalizationFile lfmap = pathManager.getLocalizationFile(lc,
                    getAbsoluteFileName(dataKey, huc, cwa, "map"));

            try (SaveableOutputStream listSos = lflist.openOutputStream();
                    GZIPOutputStream listGos = new GZIPOutputStream(listSos);
                    SaveableOutputStream mapSos = lfmap.openOutputStream();
                    GZIPOutputStream mapGos = new GZIPOutputStream(mapSos)) {

                listGos.write(SerializationUtil.transformToThrift(list));
                listGos.finish();
                listGos.flush();

                if (huc.equals(FFMPRecord.ALL)) {
                    mapGos.write(SerializationUtil.transformToThrift(map));
                } else {
                    mapGos.write(
                            SerializationUtil.transformToThrift(toPrimitive(
                                    (LinkedHashMap<Long, ArrayList<Long>>) map)));
                }
                mapGos.finish();
                mapGos.flush();

                listSos.save();
                mapSos.save();
            }

            list = null;

        } catch (SerializationException se) {
            statusHandler.error("Serialization Exception: Write Template: cwa: "
                    + cwa + " dataKey:" + dataKey + " huc: " + huc, se);
        } catch (FileNotFoundException fnfe) {
            statusHandler.error(
                    "File Not found Exception: Write Template: cwa: " + cwa
                            + " dataKey:" + dataKey + " huc: " + huc,
                    fnfe);
        } catch (IOException ioe) {
            statusHandler.error("IO Exception: Write Template: cwa: " + cwa
                    + " dataKey:" + dataKey + " huc: " + huc, ioe);
        } catch (LocalizationException e) {
            statusHandler.error("Localization Exception: Write Template: cwa: "
                    + cwa + " dataKey:" + dataKey + " huc: " + huc, e);
        }
    }

    /**
     * Gets the completed filename
     *
     * @return
     */

    private String getAbsoluteFileName(String dataKey, String huc, String cwa,
            String appendage) {
        String filename = null;
        if (appendage != null) {
            filename = getTemplateFileLocation() + huc + "-" + cwa + "-"
                    + dataKey + "-" + appendage + ".bin";
        } else {
            filename = getTemplateFileLocation() + huc + "-" + cwa + "-"
                    + dataKey + ".bin";
        }

        return filename;
    }

    /**
     * Gets the template file directory
     *
     * @return
     */
    public String getTemplateFileLocation() {
        return TEMPLATE_FILE_LOC;
    }

    /**
     * Double for max Extent of the radar
     *
     * @return
     */
    public double getMaxExtent() {
        return maxExtent;
    }

    /**
     * This maxExtent
     *
     * @param maxExtent
     */
    public void setMaxExtent(Double maxExtent) {
        this.maxExtent = maxExtent;
    }

    /**
     * Gets a basin
     *
     * @param pfaf
     * @return
     */
    public FFMPBasinMetaData getBasin(String dataKey, Long pfaf) {
        FFMPBasinMetaData fmbd = null;
        for (DomainXML domain : domains) {
            Map<Long, ?> map = getMap(dataKey, domain.getCwa(), FFMPRecord.ALL);
            fmbd = (FFMPBasinMetaData) map.get(pfaf);
            if (fmbd != null) {
                break;
            }
        }
        return fmbd;
    }

    /**
     * Gets a basin in terms of total loaded domains and sites
     *
     * @param pfaf
     * @return
     */
    public FFMPBasinMetaData getBasin(Long pfaf) {
        FFMPBasinMetaData fmbd = null;

        if (runner == null) {
            runner = frcm.getRunner(getPrimaryDomain().getCwa());
        }
        for (ProductRunXML product : runner.getProducts()) {
            if (isSiteLoaded(product.getProductKey())) {
                for (DomainXML domain : domains) {
                    Map<Long, ?> map = getMap(product.getProductKey(),
                            domain.getCwa(), FFMPRecord.ALL);
                    fmbd = (FFMPBasinMetaData) map.get(pfaf);
                    if (fmbd != null) {
                        return fmbd;
                    }
                }
            }
        }
        return fmbd;
    }

    /**
     * Load the templates
     */
    public synchronized void loadTemplate(String dataKey, String cwa,
            boolean isPrimary) {
        // synchronized this, don't want half loaded templates
        done = false;

        long time = System.currentTimeMillis();

        List<String> totalHucs = ftcm.getHucLevels();
        for (String huc : totalHucs) {
            statusHandler.handle(Priority.INFO,
                    "FFMPTemplate: Starting site template process " + dataKey
                            + ":" + cwa + ":" + huc);
            // special handling for VGB's
            if (huc.equals(FFMPRecord.VIRTUAL)) {
                getVirtualGageBasins(dataKey, cwa);
            } else {
                getMap(dataKey, cwa, huc);
            }

            statusHandler.handle(Priority.INFO,
                    "FFMPTemplate: Finishing template process " + dataKey + ": "
                            + cwa + ":" + huc);
        }

        statusHandler.handle(Priority.INFO,
                "FFMPTemplate: Template Load for " + dataKey + ": " + cwa
                        + ": took " + (System.currentTimeMillis() - time)
                        + " ms");
    }

    /**
     * Finds the aggregated pfaf of a given pfaf
     *
     * @param pfaf
     * @return
     */
    public Long findAggregatedPfaf(Long pfaf, String dataKey, String huc) {
        Long rpfaf = null;
        try {
            rpfaf = getAggregatedPfaf(pfaf, dataKey, huc);
        } catch (NullPointerException npe) {
            // FIXME
            statusHandler.error("NPE for pfaf=" + pfaf + ", dataKey=" + dataKey
                    + ", huc=" + huc, npe);
        }
        return rpfaf;
    }

    /**
     * Finds the aggregated pfaf of a given VGB
     *
     * @param lid
     * @param dataKey
     * @param huc
     * @return
     */
    public Long findAggregatedVGB(String lid, String dataKey, String huc) {
        Long rpfaf = null;
        try {
            FFMPVirtualGageBasinMetaData metadata = getVirtualGageBasinMetaData(
                    dataKey, lid);
            if (metadata != null) {
                rpfaf = findAggregatedPfaf(metadata.getParentPfaf(), dataKey,
                        huc);
            }
        } catch (NullPointerException npe) {
            // FIXME - NPE shouldn't be flow control.
            // do nothing, can be null on occasion
        }
        return rpfaf;
    }

    public ArrayList<Long> getAggregatePfafs(Object pfaf, String dataKey,
            String huc, List<DomainXML> domainList) {
        ArrayList<Long> list = new ArrayList<>();
        for (DomainXML domain : domainList) {
            List<Long> pfafList = getAggregatePfafsByDomain(pfaf, dataKey,
                    domain.getCwa(), huc);
            // Sometimes the domains have overlaps in basins.
            // You can't blindly add the domain list to the main list.
            // You have to check if it already exists in the list.
            if (pfafList != null) {
                for (Long lpfaf : pfafList) {
                    if (!list.contains(lpfaf)) {
                        list.add(lpfaf);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Gets the aggregate mappings for all domains
     *
     * @param pfaf
     * @return
     */
    public ArrayList<Long> getAggregatePfafs(Object pfaf, String dataKey,
            String huc) {
        ArrayList<Long> list = new ArrayList<>();
        for (DomainXML domain : domains) {
            List<Long> domainList = getAggregatePfafsByDomain(pfaf, dataKey,
                    domain.getCwa(), huc);
            // Sometimes the domains have overlaps in basins.
            // You can't blindly add the domain list to the main list.
            // You have to check if it already exists in the list.
            if (domainList != null) {
                for (Long lpfaf : domainList) {
                    if (!list.contains(lpfaf)) {
                        list.add(lpfaf);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Gets the aggregate mappings for all domains, used by the FFFG dialog If
     * you want ALL and I mean ALL of the basins in a given coverage
     *
     * @param pfaf
     * @return
     */

    public ArrayList<Long> getAllAggregatePfafs(Object pfaf, String huc) {
        ArrayList<Long> list = new ArrayList<>();
        Set<Long> domainSet = new HashSet<>();
        if (runner == null) {
            runner = frcm.getRunner(getPrimaryDomain().getCwa());
        }
        for (ProductRunXML product : runner.getProducts()) {
            if (isSiteLoaded(product.getProductKey())) {
                for (DomainXML domain : domains) {
                    List<Long> domainList = getAggregatePfafsByDomain(pfaf,
                            product.getProductKey(), domain.getCwa(), huc);
                    // Sometimes the domains have overlaps in basins.
                    // You can't blindly add the domain list to the main list.
                    // You have to check if it already exists in the list.
                    if (domainList != null) {
                        for (Long lpfaf : domainList) {
                            if (!list.contains(lpfaf)) {
                                list.add(lpfaf);
                            }
                        }
                    }
                }
            }
        }

        Iterator<Long> iter = domainSet.iterator();
        while (iter.hasNext()) {
            list.add(iter.next());
        }

        return list;
    }

    /**
     * Gets the aggregate mappings by domain
     *
     * @param pfaf
     * @param domain
     * @param huc
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Long> getAggregatePfafsByDomain(Object pfaf, String dataKey,
            String domain, String huc) {

        List<Long> list = new ArrayList<>();

        Map<Long, ArrayList<Long>> map = (LinkedHashMap<Long, ArrayList<Long>>) getMap(
                dataKey, domain, huc);
        if (map == null) {
            return list;
        }
        try {
            // Virtual Gage Basin centered
            if (pfaf instanceof String) {
                FFMPVirtualGageBasinMetaData metadata = getVirtualGageBasinMetaData(
                        dataKey, (String) pfaf);
                if (metadata != null) {
                    Long pfafl = findAggregatedPfaf(metadata.getParentPfaf(),
                            dataKey, huc);
                    list = map.get(pfafl);
                }
            } else {
                Object object = map.get(pfaf);
                if (object instanceof FFMPBasinMetaData) {
                    list.add(((FFMPBasinMetaData) object).getPfaf());
                } else {
                    list = (ArrayList<Long>) object;
                }
            }
        } catch (NullPointerException npe) {
            // FIXME - NPE shouldn't be flow control.
            // can be null
        }

        return list;
    }

    /**
     * Don't ever call this for anything other than FFTI or FFFG calls
     *
     * @param pfaf
     * @param huc
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<FFMPBasinMetaData> getAggregationBasins(Long pfaf,
            String dataKey, String huc) {
        List<FFMPBasinMetaData> basins = new ArrayList<>();
        for (DomainXML domain : domains) {
            List<Long> basinList = (List<Long>) getMap(dataKey, domain.getCwa(),
                    huc).get(pfaf);

            if (basinList != null) {
                Map<Long, ?> basinMap = getMap(dataKey, domain.getCwa(),
                        FFMPRecord.ALL);
                for (Long key : basinList) {
                    basins.add((FFMPBasinMetaData) basinMap.get(key));
                }
            }
        }

        return basins;
    }

    /**
     * Get the basin by Lat/Lon
     *
     * @param coor
     * @return
     */
    @SuppressWarnings("unchecked")
    public FFMPBasinMetaData findBasinByLatLon(String dataKey,
            Coordinate coor) {
        Point point = factory.createPoint(coor);
        HucLevelGeometriesFactory geomFactory = HucLevelGeometriesFactory
                .getInstance();
        try {
            String huc = "HUC0";
            for (DomainXML domain : getDomains()) {
                String cwa = domain.getCwa();
                Map<Long, Envelope> envMap = geomFactory.getEnvelopes(this,
                        dataKey, cwa, huc);
                Map<Long, List<Long>> aggrMap = (Map<Long, List<Long>>) getMap(
                        dataKey, cwa, huc);
                Map<Long, Geometry> geomMap = geomFactory.getGeometries(this,
                        dataKey, cwa, FFMPRecord.ALL);
                for (Entry<Long, List<Long>> entry : aggrMap.entrySet()) {
                    Envelope env = envMap.get(entry.getKey());
                    if ((env != null) && env.contains(coor)) {
                        // in the general envelope, check individual basins
                        for (Long pfaf : entry.getValue()) {
                            if (geomMap.get(pfaf).contains(point)) {
                                return (FFMPBasinMetaData) getMap(dataKey, cwa,
                                        FFMPRecord.ALL).get(pfaf);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            statusHandler.error("Find Basin by lon lat failed: dataKey: "
                    + dataKey + " coor:" + coor.toString(), e);
        }

        return null;
    }

    /**
     * Gets the pfaf by basinId
     *
     * @param basinId
     * @return
     */
    public Long findPfafByBasinId(String dataKey, int basinId) {
        // TODO: make reverse lookup...
        FFMPBasinMetaData basin = null;
        for (DomainXML domain : domains) {
            Map<Long, ?> map = getMap(dataKey, domain.getCwa(), FFMPRecord.ALL);
            for (Object mapVal : map.values()) {
                basin = ((FFMPBasinMetaData) mapVal);
                if (basin.getBasinId() == basinId) {
                    return basin.getPfaf();
                }
            }
        }
        return null;
    }

    /**
     * Finds the center of an aggregation of basins (Roughly)
     *
     * @param pfaf
     * @param dataKey
     * @param huc
     * @return
     */
    public Coordinate findAggregationCenter(Long pfaf, String dataKey,
            String huc) {
        HucLevelGeometriesFactory geomFactory = HucLevelGeometriesFactory
                .getInstance();
        Envelope env = null;

        for (DomainXML domain : getDomains()) {
            String cwa = domain.getCwa();
            Envelope tmp = null;

            try {
                tmp = geomFactory.getEnvelope(this, dataKey, cwa, huc, pfaf);
            } catch (Exception e) {
                String msg = "Error getting envelope: dataKey=" + dataKey
                        + ", cwa=" + cwa + ", huc=" + huc + ", pfaf=" + pfaf;
                statusHandler.handle(Priority.PROBLEM, msg, e);
            }

            if (tmp != null) {
                if (env == null) {
                    env = tmp;
                } else {
                    env.expandToInclude(tmp);
                }
                break;
            }
        }

        if (env != null) {
            return env.centre();
        }

        return null;
    }

    /**
     * Check to see if file is there
     *
     * @param hucName
     * @return
     */
    private boolean templateGood(String dataKey, String huc, String cwa) {
        boolean good = false;
        try {
            LocalizationContext lc = pathManager.getContext(
                    LocalizationType.COMMON_STATIC,
                    LocalizationLevel.CONFIGURED);
            ILocalizationFile listf = pathManager.getLocalizationFile(lc,
                    getAbsoluteFileName(dataKey, huc, cwa, "list"));
            ILocalizationFile mapf = pathManager.getLocalizationFile(lc,
                    getAbsoluteFileName(dataKey, huc, cwa, "map"));

            good = listf.exists() && mapf.exists();
        } catch (Exception e) {
            statusHandler
                    .handle(Priority.PROBLEM,
                            "Error determining if list and map files exist for dataKey="
                                    + dataKey + ", huc=" + huc + ", cwa=" + cwa,
                            e);
        }
        return good;
    }

    /**
     * Gets the starting index for searching
     *
     * @return
     */
    public int getHucDepthStart() {
        return hucDepthStart;
    }

    /**
     * Sets the starting index for searching
     *
     * @param hucDepthStart
     */
    public void setHucDepthStart(int hucDepthStart) {
        this.hucDepthStart = hucDepthStart;
    }

    /**
     * Get a listing of the counties in the FFMP monitored area
     *
     * @return
     */
    public FFMPCounties getCounties(String dataKey) {
        ArrayList<FFMPCounty> countyList = new ArrayList<>();
        FFMPCounty county;

        try {

            for (DomainXML domain : domains) {
                for (Long key : getMap(dataKey, domain.getCwa(),
                        FFMPRecord.COUNTY).keySet()) {
                    if (countyMap.get(key) == null) {
                        county = FFMPUtils.getCounty(key, MODE.CAVE.getMode());
                        if (county != null) {
                            if ((county.getGid() != null)
                                    && (county.getCountyFips() != null)
                                    && (county.getCountyName() != null)
                                    && (county.getDisplayFips() != null)
                                    && (county.getState() != null)) {
                                countyMap.put(key, county);
                            }
                        }
                    } else {
                        county = countyMap.get(key);
                    }
                    if ((county != null) && !countyList.contains(county)) {
                        countyList.add(county);
                    }
                }
            }

        } catch (Exception e) {
            statusHandler.error("Failed to lookup County: dataKey: " + dataKey,
                    e);
        }

        FFMPCounties counties = new FFMPCounties(countyList);

        return counties;
    }

    /**
     * Finds the parent aggregated pfaf of a given pfaf.
     *
     * @param key
     * @param dataKey
     * @param huc
     * @return
     */
    @SuppressWarnings("unchecked")
    public Long getAggregatedPfaf(Long key, String dataKey, String huc) {
        if (huc.equals(FFMPRecord.ALL)) {
            return key;
        } else if (huc.equals(FFMPRecord.COUNTY)) {
            // TODO: use envelope contains to limit search area?
            for (DomainXML domain : domains) {
                Map<Long, ?> map = getMap(dataKey, domain.getCwa(), huc);
                for (Entry<Long, ?> entry : map.entrySet()) {
                    if (((List<Long>) entry.getValue()).contains(key)) {
                        return entry.getKey();
                    }
                }
            }
        } else {
            int hucNum = Integer.parseInt(huc.substring(3));
            int endIndex = getHucDepthStart() + hucNum;
            return Long.parseLong(key.toString().substring(0, endIndex));
        }
        return null;
    }

    /**
     * Find the extents of this domain
     *
     * @param cwa
     * @return
     */
    public Geometry getCWAExtents(String cwa) {

        Geometry cwaGeometry = FFMPUtils.getCwaGeometry(cwa, mode.getMode());

        return cwaGeometry;
    }

    /**
     * Find the extents of the collective sites
     *
     * @param dataKey
     * @return
     */
    public String getSiteExtents(String dataKey) throws Exception {

        String siteExtents = null;
        // figure out which product to apply this to
        if (runner == null) {
            runner = frcm.getRunner(getPrimaryDomain().getCwa());
        }

        ProductRunXML product = runner.getProduct(dataKey);
        FFMPSourceConfigurationManager fscm = FFMPSourceConfigurationManager
                .getInstance();
        SourceXML primeSource = fscm.getSource(product.getProductName());

        if (primeSource.getDataType().equals(
                FFMPSourceConfigurationManager.DATA_TYPE.XMRG.getDataType())) {

            Rectangle rect = null;

            rect = HRAPCoordinates.getHRAPCoordinates();
            rect.setBounds(rect.x * primeSource.getHrapGridFactor(),
                    rect.y * primeSource.getHrapGridFactor(),
                    rect.width * primeSource.getHrapGridFactor(),
                    rect.height * primeSource.getHrapGridFactor());

            HRAPSubGrid hrapgrid = new HRAPSubGrid(rect,
                    primeSource.getHrapGridFactor());
            Geometry geo = hrapgrid.getGeometry();
            siteExtents = FFMPUtils.getGeometryText(geo);

        } else if (primeSource.getDataType().equals(
                FFMPSourceConfigurationManager.DATA_TYPE.RADAR.getDataType())) {

            Coordinate siteCoor = FFMPUtils.getRadarCenter(dataKey);
            Polygon poly = FFMPUtils.getRadarPolygon(siteCoor,
                    120.0 * ScanUtils.NMI_TO_KM * 1000.0);
            siteExtents = FFMPUtils.getRadarPolygonText(poly);

        } else if (primeSource.getDataType().equals(
                FFMPSourceConfigurationManager.DATA_TYPE.GRID.getDataType())) {
            // extract the Grid Coverage for use in site extents creation
            GridCoverage coverage = FFMPUtils
                    .getGridCoverageRecord(primeSource.getDataPath());
            siteExtents = FFMPUtils.getGeometryText(coverage.getGeometry());
        } else if (primeSource.getDataType().equals(
                FFMPSourceConfigurationManager.DATA_TYPE.PDO.getDataType())) {
            statusHandler.handle(Priority.PROBLEM,
                    "PDO Not yet implemented:  " + dataKey);

        } else {
            statusHandler.handle(Priority.PROBLEM,
                    "No method of extracting siteExtents for this type:  "
                            + dataKey);
        }

        return siteExtents;
    }

    /**
     * Sets the extents
     *
     * @param maxExtent
     */
    public void setExtents(double maxExtent) {
        this.maxExtent = maxExtent;
    }

    private void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    /**
     * gets the virtuals or not
     *
     * @return
     */
    public boolean getVirtual() {
        return virtual;
    }

    /**
     * Get the maps from storage or create them
     *
     * @param huc
     * @param cwa
     * @return
     */
    @SuppressWarnings("unchecked")
    private LinkedHashMap<Long, ?> readMap(String dataKey, String cwa,
            String huc) {

        LinkedHashMap<Long, ?> map = null;

        if (!templateGood(dataKey, huc, cwa)) {
            if (mode == MODE.EDEX) {
                List<Long> list = null;
                Map<Long, Set<Long>> aggrPfafToAllChildPfafsMap = null;

                if (huc.equals(FFMPRecord.ALL)) {
                    try {
                        map = loadBasins(dataKey, cwa,
                                FFMPUtils.getBasins(cwa, getMaxExtent(),
                                        getSiteExtents(dataKey),
                                        mode.getMode()));
                    } catch (Exception e) {
                        statusHandler.handle(Priority.PROBLEM,
                                "Unable to create FFMP Template for this dataKey: "
                                        + dataKey,
                                e);
                    }
                } else if (huc.equals(FFMPRecord.COUNTY)) {
                    list = getCountyFips(cwa, dataKey);
                } else {
                    int myHucNum = Integer.parseInt(huc.substring(3));
                    TreeSet<Long> aggrPfafs = new TreeSet<>();
                    aggrPfafToAllChildPfafsMap = new HashMap<>();
                    if (myHucNum + 1 == getTotalHucLevels()) {
                        Set<Long> allPfafs = getMap(dataKey, cwa,
                                FFMPRecord.ALL).keySet();
                        for (Long pfaf : allPfafs) {
                            int endIndex = getHucDepthStart() + myHucNum;
                            Long aggrPfaf = Long.parseLong(
                                    pfaf.toString().substring(0, endIndex));
                            aggrPfafs.add(aggrPfaf);
                            Set<Long> allChildPfafs = aggrPfafToAllChildPfafsMap
                                    .get(aggrPfaf);
                            if (allChildPfafs == null) {
                                allChildPfafs = new TreeSet<>();
                                aggrPfafToAllChildPfafsMap.put(aggrPfaf,
                                        allChildPfafs);
                            }
                            allChildPfafs.add(pfaf);
                        }
                    } else {
                        String childHuc = "HUC" + (myHucNum + 1);
                        Map<Long, List<Long>> childHucMap = (Map<Long, List<Long>>) getMap(
                                dataKey, cwa, childHuc);
                        for (Entry<Long, List<Long>> entry : childHucMap
                                .entrySet()) {
                            Long aggrPfaf = new Long(
                                    entry.getKey().longValue() / 10);
                            aggrPfafs.add(aggrPfaf);
                            Set<Long> childAggrPfafs = aggrPfafToAllChildPfafsMap
                                    .get(aggrPfaf);
                            if (childAggrPfafs == null) {
                                childAggrPfafs = new TreeSet<>();
                                aggrPfafToAllChildPfafsMap.put(aggrPfaf,
                                        childAggrPfafs);
                            }
                            childAggrPfafs.addAll(entry.getValue());
                        }
                    }
                    list = new ArrayList<>(aggrPfafs);
                }

                if (!huc.equals(FFMPRecord.ALL)) {
                    map = new LinkedHashMap<>();
                    Map<Long, Geometry> rawGeometries = null;
                    LinkedHashMap<Long, FFMPBasinMetaData> allMap = (LinkedHashMap<Long, FFMPBasinMetaData>) getMap(
                            dataKey, cwa, FFMPRecord.ALL);

                    for (Long key : list) {
                        List<Long> innerList = null;

                        if (huc.equals(FFMPRecord.COUNTY)) {
                            innerList = new ArrayList<>();
                            List<?> countyInfo = FFMPUtils.getCountyInfo(key,
                                    mode.getMode());

                            PreparedGeometry countyGeometry = PreparedGeometryFactory
                                    .prepare((Geometry) countyInfo.get(0));
                            String countyName = (String) countyInfo.get(1);
                            String state = (String) countyInfo.get(2);
                            statusHandler.handle(Priority.INFO,
                                    "Processing --- County: " + countyName
                                            + " State: " + state);
                            boolean primary = false;
                            if (cwa.equals(getPrimaryDomain().getCwa())) {
                                primary = true;
                            }

                            if (rawGeometries == null
                                    || rawGeometries.isEmpty()) {
                                rawGeometries = getRawGeometries(dataKey, cwa);
                            }

                            for (Entry<Long, FFMPBasinMetaData> entry : allMap
                                    .entrySet()) {

                                if (countyGeometry.contains(rawGeometries
                                        .get(entry.getKey()).getCentroid())) {
                                    FFMPBasinMetaData basin = entry.getValue();
                                    basin.setCounty(countyName);
                                    basin.setState(state);
                                    basin.setPrimaryCwa(primary);
                                    innerList.add(entry.getKey());
                                }
                            }

                        } else {
                            innerList = new ArrayList<>(
                                    aggrPfafToAllChildPfafsMap.get(key));
                        }

                        statusHandler.handle(Priority.DEBUG, "HUC: " + huc
                                + " INNERLIST SIZE:  " + innerList.size());

                        if ((innerList != null) && (!innerList.isEmpty())) {

                            ((Map<Long, List<Long>>) map).put(key, innerList);
                        }
                    }

                    // trigger the write of the "ALL" now that the counties are
                    // set.
                    if (huc.equals(FFMPRecord.COUNTY)) {

                        if (allMap != null) {
                            writeTemplateFile(dataKey, FFMPRecord.ALL, cwa,
                                    allMap);
                        }
                    }
                }

                aggrPfafToAllChildPfafsMap = null;
                list = null;

                // clean up
                System.runFinalization();
                System.gc();

                if (!huc.equals(FFMPRecord.ALL) && map != null) {
                    writeTemplateFile(dataKey, huc, cwa, map);
                }
            }
        } else {
            map = readTemplateFile(dataKey, huc, cwa);
        }
        return map;
    }

    /**
     * load up the maps
     *
     * @param huc
     * @return
     */
    public synchronized LinkedHashMap<Long, ?> getMap(String dataKey,
            String cwa, String huc) {

        LinkedHashMap<Long, ?> map = null;
        HashMap<String, LinkedHashMap<Long, ?>> hucMap = null;

        HashMap<String, HashMap<String, LinkedHashMap<Long, ?>>> cwaMap = domainMap
                .get(dataKey);
        if (cwaMap == null) {
            cwaMap = new HashMap<>();
            domainMap.put(dataKey, cwaMap);
        }

        hucMap = cwaMap.get(cwa);
        if (hucMap == null) {
            hucMap = new HashMap<>();
            cwaMap.put(cwa, hucMap);
        }

        map = hucMap.get(huc);
        if (map == null) {
            map = readMap(dataKey, cwa, huc);
            hucMap.put(huc, map);
        }

        return map;
    }

    /**
     * Find the list of pfafs for this HUC level
     *
     * @param siteKey
     * @param huc
     * @param domains
     * @return
     */
    public synchronized List<Long> getHucKeyList(String siteKey, String huc,
            List<DomainXML> domains) {

        Set<Long> keys = new HashSet<>();

        for (DomainXML domain : domains) {
            Map<Long, ?> map = getMap(siteKey, domain.getCwa(), huc);
            keys.addAll(map.keySet());
        }

        return new ArrayList<>(keys);
    }

    /**
     * Gets the template config manager
     *
     * @return
     */
    public FFMPTemplateConfigurationManager getTemplateMgr() {
        return ftcm;
    }

    /**
     * Read the file or generate VGB's for primary domain
     *
     * @param cwa
     * @return
     */
    private LinkedHashMap<String, FFMPVirtualGageBasinMetaData> readVirtualGageBasins(
            String dataKey, String cwa) {

        LinkedHashMap<String, FFMPVirtualGageBasinMetaData> virtuals = null;

        if (!templateGood(dataKey, "VIRTUAL", cwa)) {
            if (mode == MODE.EDEX) {
                double extent = 0.0;
                if (cwa.equals(getPrimaryDomain().getCwa())) {
                    extent = getMaxExtent();
                }

                virtuals = FFMPUtils.getVirtualGageBasins(extent, cwa,   //<=====
                        mode.getMode());
                ArrayList<String> removes = new ArrayList<>();
                Map<Long, Geometry> rawGeometries = getRawGeometries(dataKey,
                        cwa);
                // assign pfafs

                for (FFMPVirtualGageBasinMetaData vb : virtuals.values()) {
                    Point vgbPoint = factory.createPoint(vb.getCoordinate());
                    /*
                     * Expensive..., use envelopes first to get rough idea and
                     * skip unnecessary checks
                     */
                    Map<Long, ?> map = getMap(dataKey, cwa, FFMPRecord.ALL);
                    for (Entry<Long, ?> entry : map.entrySet()) {
                        Long pfaf = entry.getKey();
                        Geometry geometry = rawGeometries.get(pfaf);
                        if (geometry != null) {
                            if (vgbPoint.within(geometry)) {
                                FFMPBasinMetaData basin = (FFMPBasinMetaData) entry
                                        .getValue();
                                vb.setParentPfaf(pfaf);
                                vb.setCwa(basin.getCwa());
                                break;
                            }
                        }
                    }
                    // if it dosen't have a pfaf, we can't locate it.
                    if (vb.getParentPfaf() == null) {
                        removes.add(vb.getLid());
                        
                    }
                }
                
                // gets rid of excluded ones
                // DR 11250, the filtering code is moved to FFMPRowGenerator.run() to avoid breaking the indexing of vgb and values arrays.
//                if ((ftcm.getExcludedVGBs() != null)
//                        && (ftcm.getExcludedVGBs().getLids().size() > 0)) {
//                    for (String lid : ftcm.getExcludedVGBs().getLids()) {
//                        removes.add(lid);
//                    }
//                }
//                for (String lid : removes) {
//                    virtuals.remove(lid);
//                }

                // write them out
                writeVGBFile(virtuals, cwa, dataKey);
            }
        } else {

            virtuals = readVGBFile("VIRTUAL", cwa, dataKey);

        }

        return virtuals;
    }

    /**
     * Generate the Virtual Gage Basins Meta Data for primary domain
     *
     * @return
     */
    public synchronized LinkedHashMap<String, FFMPVirtualGageBasinMetaData> getVirtualGageBasins(
            String dataKey, String cwa) {

        LinkedHashMap<String, FFMPVirtualGageBasinMetaData> map = null;

        HashMap<String, LinkedHashMap<String, FFMPVirtualGageBasinMetaData>> virtualmap = virtualDomainMap
                .get(dataKey);
        if (virtualmap == null) {
            virtualmap = new HashMap<>();
        }

        virtualDomainMap.put(dataKey, virtualmap);

        map = virtualmap.get(cwa);
        if (map == null) {
            map = readVirtualGageBasins(dataKey, cwa);
            if (map == null) {
                return map;
            }

            virtualmap.put(cwa, map);

            HashMap<String, ArrayList<FFMPVirtualGageBasinMetaData>> vgbMap = new HashMap<>();

            HashMap<Long, ArrayList<FFMPVirtualGageBasinMetaData>> virtualGageBasins = new HashMap<>(
                    (int) (map.size() * 1.3));

            for (FFMPVirtualGageBasinMetaData vgb : map.values()) {
                Long id = vgb.getParentPfaf();
                // see getCountyStateName()
                String stateCommaCnty = vgb.getState() + ", " + vgb.getCounty();

                ArrayList<FFMPVirtualGageBasinMetaData> list = virtualGageBasins
                        .get(id);
                ArrayList<FFMPVirtualGageBasinMetaData> list2 = vgbMap
                        .get(stateCommaCnty.toUpperCase());

                if (list == null) {
                    list = new ArrayList<>();
                    virtualGageBasins.put(id, list);
                }
                list.add(vgb);

                if (list2 == null) {
                    list2 = new ArrayList<>();
                    vgbMap.put(stateCommaCnty.toUpperCase(), list2);
                }
                list2.add(vgb);
            }

            HashMap<String, HashMap<String, ArrayList<FFMPVirtualGageBasinMetaData>>> vMapCounty = vgbsInCounty
                    .get(dataKey);
            HashMap<String, HashMap<Long, ArrayList<FFMPVirtualGageBasinMetaData>>> virtualMapPfaf = virtualGageBasinsInParentPfaf
                    .get(dataKey);
            if (virtualMapPfaf == null) {
                virtualMapPfaf = new HashMap<>();

            }

            if (vMapCounty == null) {
                vMapCounty = new HashMap<>();
            }

            vMapCounty.put(cwa, vgbMap);
            vgbsInCounty.put(dataKey, vMapCounty);

            virtualMapPfaf.put(cwa, virtualGageBasins);
            virtualGageBasinsInParentPfaf.put(dataKey, virtualMapPfaf);
        }
        return map;
    }

    /**
     * Gets the Virtual Gage Basin MetaData
     *
     * @param lid
     * @return
     */
    public FFMPVirtualGageBasinMetaData getVirtualGageBasinMetaData(
            String dataKey, String lid) {
        FFMPVirtualGageBasinMetaData vgbmd = null;
        for (DomainXML domain : domains) {
            Map<String, FFMPVirtualGageBasinMetaData> map = getVirtualGageBasins(
                    dataKey, domain.getCwa());
            if (map != null && map.containsKey(lid)) {
                vgbmd = map.get(lid);
                break;
            }
        }

        return vgbmd;
    }

    /**
     * Gets the Virtual Gage Basin MetaData
     *
     * @param dataKey
     * @param cwa
     * @param parentPfaf
     * @return
     */
    public synchronized List<FFMPVirtualGageBasinMetaData> getVirtualGageBasinMetaData(
            String dataKey, String cwa, Long parentPfaf) {

        if (cwa == null) {
            for (DomainXML domain : domains) {
                List<FFMPVirtualGageBasinMetaData> result = getVirtualGageBasinMetaData(
                        dataKey, domain.getCwa(), parentPfaf);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        Map<String, HashMap<Long, ArrayList<FFMPVirtualGageBasinMetaData>>> virtualMap = virtualGageBasinsInParentPfaf
                .get(dataKey);

        Map<Long, ArrayList<FFMPVirtualGageBasinMetaData>> map = virtualMap
                .get(cwa);

        if (map != null) {
            return map.get(parentPfaf);
        }

        return null;
    }

    /**
     * Finds the Lid string used by the aggregated VGB's
     *
     * @param fbmd
     * @param huc
     * @return
     */
    public String getVGBLidString(FFMPBasinMetaData fbmd, Long aggPfaf,
            String huc) {
        String lid = null;
        if ("COUNTY".equals(huc)) {
            lid = fbmd.getState() + ", " + fbmd.getCounty();
        } else {
            lid = fbmd.getHucName() + "-" + aggPfaf;
        }
        return lid;
    }

    /**
     * Gets you the list of pfafs that have VGB's for a given HUC level DONT
     * EVER CALL THIS WITH "ALL" HUC level use getVirtualGageBasins() for that
     *
     * @param huc
     * @return
     */
    public List<Long> getVGBsInAggregate(Long pfaf, String dataKey,
            String huc) {
        List<Long> vgbPfafs = null;
        for (Long iPfaf : getAggregatePfafs(pfaf, dataKey, huc)) {
            if (checkVirtualGageBasinMetaData(dataKey, iPfaf)) {
                if (vgbPfafs == null) {
                    vgbPfafs = new ArrayList<>();
                }
                vgbPfafs.add(iPfaf);
            }
        }

        return vgbPfafs;
    }

    /**
     * Check for VGB's in aggregate pfaf
     *
     * @param pfaf
     * @param dataKey
     * @param huc
     * @return
     */
    public boolean checkVGBsInAggregate(Long pfaf, String dataKey, String huc) {
        for (Long iPfaf : getAggregatePfafs(pfaf, dataKey, huc)) {
            if (checkVirtualGageBasinMetaData(dataKey, iPfaf)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the Virtual Gage Basin MetaData
     *
     * @param pfaf
     * @return
     */
    public synchronized boolean checkVirtualGageBasinMetaData(String dataKey,
            Long pfaf) {

        Map<String, HashMap<Long, ArrayList<FFMPVirtualGageBasinMetaData>>> virtualMap = virtualGageBasinsInParentPfaf
                .get(dataKey);

        for (DomainXML domain : domains) {

            Map<Long, ArrayList<FFMPVirtualGageBasinMetaData>> map = virtualMap
                    .get(domain.getCwa());
            if (map != null) {
                List<FFMPVirtualGageBasinMetaData> list = map.get(pfaf);
                if (list != null && !list.isEmpty()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the Virtual Gage Basin MetaData
     *
     * @param pfaf
     * @return
     */
    public synchronized List<Long> getVirtualGageBasinLookupIds(String dataKey,
            Long pfaf, String huc, String rowName) {
        if (isCountyRow(huc, rowName)) {
            return getVgbLookupIdsByCounty(dataKey, pfaf, huc, rowName);
        }

        Map<String, HashMap<Long, ArrayList<FFMPVirtualGageBasinMetaData>>> virtualMap = virtualGageBasinsInParentPfaf
                .get(dataKey);
        List<Long> result = new ArrayList<>();

        for (DomainXML domain : domains) {
            Map<Long, ArrayList<FFMPVirtualGageBasinMetaData>> map = virtualMap
                    .get(domain.getCwa());
            if (map != null) {
                List<FFMPVirtualGageBasinMetaData> list = map.get(pfaf);
                if (list != null && !list.isEmpty()) {
                    for (FFMPVirtualGageBasinMetaData md : list) {
                        if (!result.contains(md.getLookupId())) {
                            result.add(md.getLookupId());
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Writes out all basins from DB by CWA
     *
     * @param results
     */
    private LinkedHashMap<Long, FFMPBasinMetaData> loadBasins(String siteKey,
            String cwa, Object[] results) {
        LinkedHashMap<Long, FFMPBasinMetaData> basins = new LinkedHashMap<>();
        WKBReader reader = new WKBReader();
        FFMPBasinMetaData basin = null;
        int upstreamDepth = 0;
        String compositeKey = siteKey + cwa;
        SoftReference<Map<Long, Geometry>> rawGeomRef = cwaRawGeometries
                .get(compositeKey);
        Map<Long, Geometry> pfafGeometries = null;

        if (rawGeomRef != null) {
            pfafGeometries = rawGeomRef.get();
        }

        if (results != null && results.length > 0) {

            if (pfafGeometries == null) {
                pfafGeometries = new HashMap<>(results.length, 1.0f);
                cwaRawGeometries.put(compositeKey,
                        new SoftReference<>(pfafGeometries));
            }

            for (int i = 0; i < results.length; i++) {
                Object[] row = (Object[]) results[i];
                basin = FFMPUtils.getMetaDataBasin(row, mode.getMode());

                if (upstreamDepth == 0) {
                    upstreamDepth = basin.getStreamPfafs().size();
                }

                basin.removeZeros();
                basins.put(basin.getPfaf(), basin);

                if ((row.length >= (upstreamDepth + 9))
                        && (row[upstreamDepth + 9] != null)) {
                    try {
                        pfafGeometries.put(basin.getPfaf(),
                                reader.read((byte[]) row[upstreamDepth + 9])
                                        .buffer(0));

                    } catch (Exception e) {
                        statusHandler
                                .error("Failure to add rawGeometry in loadBasins: "
                                        + siteKey, e);
                    }
                }
            }
        } else {
            statusHandler.handle(Priority.PROBLEM,
                    "Query to basins table returned no basin geometries within given area: "
                            + siteKey);
        }

        System.runFinalization();
        System.gc();

        return basins;
    }

    /**
     * compress to primitive
     *
     * @param list
     * @return
     */
    private Map<Long, long[]> toPrimitive(Map<Long, ArrayList<Long>> map) {
        Map<Long, long[]> primList = new HashMap<>();
        for (Entry<Long, ArrayList<Long>> entry : map.entrySet()) {
            List<Long> val = entry.getValue();
            long[] longs = new long[val.size()];
            for (int i = 0; i < val.size(); i++) {
                longs[i] = val.get(i).longValue();
            }
            primList.put(entry.getKey(), longs);
        }
        return primList;
    }

    /**
     * back to LinkedHash
     *
     * @param longs
     * @return
     */
    private LinkedHashMap<Long, List<Long>> fromPrimitive(
            Map<Long, long[]> longs, long[] list) {
        // reconstructs the LinkedHash in order
        LinkedHashMap<Long, List<Long>> map = new LinkedHashMap<>();
        for (Long l : list) {
            List<Long> longa = new ArrayList<>();
            for (int i = 0; i < longs.get(l).length; i++) {
                longa.add(longs.get(l)[i]);
            }
            map.put(l, longa);
        }
        return map;
    }

    /**
     * Get the basin ID of the county
     *
     * @param dataKey
     * @param nameState
     * @return
     */
    @SuppressWarnings("unchecked")
    public Long getCountyIdByNameState(String dataKey, String nameState) {
        String[] parts = nameState.split(",");
        if (parts.length == 2) {
            for (DomainXML domain : domains) {
                Map<Long, List<Long>> countyMap = (Map<Long, List<Long>>) getMap(
                        dataKey, domain.getCwa(), FFMPRecord.ALL);
                for (Entry<Long, List<Long>> entry : countyMap.entrySet()) {
                    for (Long key : entry.getValue()) {
                        FFMPBasinMetaData basin = getBasin(dataKey, key);
                        if (basin.getState().equals(parts[0].trim())
                                && basin.getCounty().equals(parts[1].trim())) {
                            return entry.getKey();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets the domains to load for this template instance
     *
     * @param domains
     */
    public synchronized void setDomains(ArrayList<DomainXML> domains) {
        this.domains = domains;
    }

    /**
     * Gets the domains in this template
     *
     * @return
     */
    public synchronized ArrayList<DomainXML> getDomains() {
        return domains;
    }

    /**
     * Add domains to the templates, This method is used by EDEX to add domains
     *
     * @param domain
     */
    public synchronized void addDomain(DomainXML domain) {
        if (runner == null) {
            runner = frcm.getRunner(getPrimaryDomain().getCwa());
        }
        // create template for all of the site/domain combos
        // eliminate duplicates

        if (runner != null) {

            ArrayList<String> products = new ArrayList<>();
            for (ProductRunXML product : runner.getProducts()) {
                if (!products.contains(product.getProductKey())) {
                    products.add(product.getProductKey());
                }
            }

            for (String dataKey : products) {
                loadTemplate(dataKey, domain.getCwa(), domain.isPrimary());
            }

            if (!domains.contains(domain)) {
                domains.add(domain);
            }
        } else {
            statusHandler.handle(Priority.PROBLEM, "The Domain: "
                    + domain.getCwa()
                    + " your runner is null (not truly a primary domain), check your FFMPRunConfig.xml");
        }

        done = true;
    }

    /**
     * Add domains to the templates, This method is used by EDEX to add domains
     *
     * @param domain
     */
    public synchronized void addDomain(String dataKey, DomainXML domain) {

        loadTemplate(dataKey, domain.getCwa(), domain.isPrimary());

        if (!domains.contains(domain)) {
            domains.add(domain);
        }
        done = true;
    }

    /**
     * dump a domain on the fly, CAVE side
     *
     * @param domainName
     */
    public synchronized void removeDomain(String dataKey, String domainName) {

        domainMap.get(dataKey).get(domainName);
        virtualDomainMap.get(dataKey).get(domainName);

        for (DomainXML domain : domains) {
            if (domain.getCwa().equals(domainName)) {
                domains.remove(domain);
                break;
            }
        }
    }

    /**
     * dump a dataKey
     *
     * @param dataKey
     */
    public synchronized void removeDataKey(String dataKey) {
        domainMap.remove(dataKey);
        virtualDomainMap.remove(dataKey);
    }

    /**
     * Set the primary domain
     */
    public void setPrimaryDomain(DomainXML primaryCWA) {
        this.primaryCWA = primaryCWA;
    }

    /**
     * Gets the primary
     *
     * @return
     */
    public DomainXML getPrimaryDomain() {
        return primaryCWA;
    }

    /**
     * Gets the list for all of the pfafs
     *
     * @param huc
     * @param cwa
     * @return
     */
    public long[] readDomainList(String huc, String cwa, String dataKey) {

        long[] list = null;

        LocalizationContext lc = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.CONFIGURED);
        ILocalizationFile f = pathManager.getLocalizationFile(lc,
                getAbsoluteFileName(dataKey, huc, cwa, "list"));

        try (InputStream is = f.openInputStream();
                GZIPInputStream gis = new GZIPInputStream(is)) {
            list = SerializationUtil.transformFromThrift(long[].class, gis);
        } catch (SerializationException | IOException
                | LocalizationException e) {
            statusHandler
                    .error("Exception reading domain list: Read Domain. cwa: "
                            + cwa + " dataKey: " + dataKey + " huc: " + huc, e);
        }

        return list;
    }

    /**
     * Reads the actual domain map
     *
     * @param huc
     * @param cwa
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<Long, ?> readDomainMap(String dataKey, String huc, String cwa) {

        Map<Long, ?> map = null;

        LocalizationContext lc = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.CONFIGURED);
        ILocalizationFile f = pathManager.getLocalizationFile(lc,
                getAbsoluteFileName(dataKey, huc, cwa, "map"));

        try (InputStream is = f.openInputStream();
                GZIPInputStream gis = new GZIPInputStream(is)) {
            map = SerializationUtil.transformFromThrift(HashMap.class, gis);
        } catch (SerializationException | IOException
                | LocalizationException e) {
            statusHandler.error("Exception reading domain map. Domain Map: "
                    + dataKey + " cwa:" + cwa + " huc: " + huc, e);

        }

        return map;
    }

    /**
     * Reads the actual VGB domain map
     *
     * @param dataKey
     * @param cwa
     * @return
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, FFMPVirtualGageBasinMetaData> readVGBDomainMap(
            String dataKey, String cwa) {

        HashMap<String, FFMPVirtualGageBasinMetaData> map = null;

        LocalizationContext lc = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.CONFIGURED);
        ILocalizationFile f = pathManager.getLocalizationFile(lc,
                getAbsoluteFileName(dataKey, FFMPRecord.VIRTUAL, cwa, "map"));

        try (InputStream is = f.openInputStream();
                GZIPInputStream gis = new GZIPInputStream(is)) {
            map = SerializationUtil.transformFromThrift(HashMap.class, gis);
        } catch (SerializationException | IOException
                | LocalizationException e) {
            statusHandler
                    .error("Exception reading VHB Domain map. Virtual Basins: "
                            + dataKey + " cwa: " + cwa, e);
        }

        return map;
    }

    /**
     * Reads the actual VGB domain list
     *
     * @param dataKey
     * @param cwa
     * @return
     */
    public String[] readVGBDomainList(String dataKey, String cwa) {

        String[] list = null;

        LocalizationContext lc = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.CONFIGURED);
        ILocalizationFile f = pathManager.getLocalizationFile(lc,
                getAbsoluteFileName(dataKey, FFMPRecord.VIRTUAL, cwa, "list"));

        try (InputStream is = f.openInputStream();
                GZIPInputStream gis = new GZIPInputStream(is)) {
            list = SerializationUtil.transformFromThrift(String[].class, gis);
        } catch (SerializationException | IOException
                | LocalizationException e) {
            statusHandler
                    .error("Exception reading VGB Domain List. Read Virtual Domain: cwa: "
                            + cwa + " dataKey: " + dataKey, e);
        }

        return list;
    }

    public void verifyUnifiedGeometries(String huc, String domain) {
        HucLevelGeometriesFactory hucGeomFactory = HucLevelGeometriesFactory
                .getInstance();

        if (runner == null) {
            runner = frcm.getRunner(getPrimaryDomain().getCwa());
        }

        for (ProductRunXML product : runner.getProducts()) {
            if (isSiteLoaded(product.getProductKey())) {
                long t1 = System.currentTimeMillis();
                statusHandler.handle(Priority.INFO,
                        "FFMPTemplate: Starting geometry unify process "
                                + product.getProductKey() + ": " + domain + ": "
                                + huc);
                try {
                    hucGeomFactory.getGeometries(this, product.getProductKey(),
                            domain, huc);
                    hucGeomFactory.getEnvelopes(this, product.getProductKey(),
                            domain, huc);
                } catch (Exception e) {
                    statusHandler.handle(Priority.ERROR,
                            "Failed persisting unified huc envelopes for huc: "
                                    + product.getProductKey() + ": " + huc
                                    + ": " + domain,
                            e);
                }

                hucGeomFactory.clear();

                long t2 = System.currentTimeMillis();
                statusHandler.handle(Priority.INFO,
                        "FFMPTemplate: Finished geometry unify process "
                                + product.getProductKey() + ": " + domain + ": "
                                + huc + " in [" + (t2 - t1) + "] ms");
            }
        }
    }

    /**
     *
     * @param cwa
     * @return
     */
    public Map<Long, Geometry> getRawGeometries(String siteKey, String cwa) {

        String compositeKey = siteKey + cwa;
        SoftReference<Map<Long, Geometry>> rawGeomRef = cwaRawGeometries
                .get(compositeKey);
        Map<Long, Geometry> pfafGeometries = null;
        if (rawGeomRef != null) {
            pfafGeometries = rawGeomRef.get();
        }
        if (pfafGeometries == null || pfafGeometries.isEmpty()) {
            // TODO: add sync locking per cwa
            long t0 = System.currentTimeMillis();
            pfafGeometries = FFMPUtils.getRawGeometries(
                    getMap(siteKey, cwa, FFMPRecord.ALL).keySet());
            long t1 = System.currentTimeMillis();
            statusHandler.handle(Priority.INFO,
                    "Retrieval of raw geometries for site " + siteKey + " cwa "
                            + cwa + " took " + (t1 - t0) + " ms.");
            cwaRawGeometries.put(compositeKey,
                    new SoftReference<>(pfafGeometries));
        }
        return pfafGeometries;
    }

    /**
     * Look for overlaps
     *
     * @param pfaf
     * @param huc
     * @return
     */
    public boolean checkOverlap(long pfaf, String dataKey, String huc) {

        Map<Long, ?> map = getMap(dataKey, getPrimaryDomain().getCwa(), huc);

        for (long lpfaf : map.keySet()) {
            if (lpfaf == pfaf) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the areas for a list of pfafs
     *
     * @param pfafs
     * @return
     */
    public List<Double> getAreas(List<Long> pfafs) {
        List<Double> areas = new ArrayList<>(pfafs.size());
        for (Long pfaf : pfafs) {
            areas.add(getBasin(pfaf).getArea());
        }
        return areas;
    }

    /**
     * gets all up and down stream basins for a given pfaf
     *
     * @param dataKey
     * @param pfaf
     * @return
     */
    public ArrayList<Long> getUpStreamBasins(String dataKey, Long pfaf) {

        ArrayList<Long> streamPfafs = new ArrayList<>();

        if (pfaf != null) {
            FFMPBasinMetaData fmbd = getBasin(pfaf);

            if (fmbd != null) {
                if (fmbd.getStreamPfafs() != null) {
                    for (Integer basinId : fmbd.getStreamPfafs()) {
                        Long basinPfaf = findPfafByBasinId(dataKey, basinId);
                        streamPfafs.add(basinPfaf);
                    }
                }
            }
        }

        return streamPfafs;
    }

    /**
     * Gets the down stream trace
     *
     * @param dataKey
     * @param pfaf
     * @return
     */
    public Long getDownStreamBasins(String dataKey, Long pfaf) {

        if (pfaf != null) {
            FFMPBasinMetaData basin = getBasin(pfaf);
            if (basin != null) {
                int basinId = getBasin(pfaf).getBasinId();

                for (DomainXML domain : domains) {
                    for (Entry<Long, ?> entry : getMap(dataKey, domain.getCwa(),
                            FFMPRecord.ALL).entrySet()) {
                        FFMPBasinMetaData downBasin = (FFMPBasinMetaData) entry
                                .getValue();

                        if (downBasin.getStreamPfafs() != null) {
                            if (downBasin.getStreamPfafs().contains(basinId)) {
                                return downBasin.getPfaf();
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get the FIPS for the pfaf being looked at
     *
     * @param pfaf
     * @return
     */
    public long getCountyFipsByPfaf(long pfaf) {

        FFMPRunConfigurationManager.getInstance();
        ArrayList<FFMPRunXML> runners = FFMPRunConfigurationManager
                .getInstance().getFFMPRunners();
        for (FFMPRunXML runner : runners) {
            ArrayList<ProductRunXML> products = runner.getProducts();
            for (ProductRunXML product : products) {
                if (this.isSiteLoaded(product.getProductKey())) {
                    for (DomainXML domain : domains) {
                        for (Entry<Long, ?> entry : getMap(
                                product.getProductKey(), domain.getCwa(),
                                "COUNTY").entrySet()) {
                            @SuppressWarnings("unchecked")
                            ArrayList<Long> countyList = (ArrayList<Long>) entry
                                    .getValue();

                            if (countyList.contains(pfaf)) {
                                return entry.getKey();
                            }
                        }
                    }
                }
            }
        }

        return 0l;
    }

    /**
     * Gets a metadata basin contained within the domain listed.
     *
     * @param dataKey
     * @param domains
     * @param pfafs
     * @return
     */
    public FFMPBasinMetaData getBasinInDomains(String dataKey,
            List<DomainXML> domains, List<Long> pfafs) {

        FFMPBasinMetaData mbasin = null;

        for (DomainXML domain : domains) {

            Map<Long, ?> map = getMap(dataKey, domain.getCwa(), FFMPRecord.ALL);

            for (Long key : pfafs) {
                if (map.containsKey(key)) {
                    mbasin = (FFMPBasinMetaData) map.get(key);
                    if (mbasin.getCwa().equals(domain.getCwa())
                            || mbasin.isPrimaryCwa()) {
                        return mbasin;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Check for site load or not
     *
     * @param siteKey
     * @return
     */
    public boolean isSiteLoaded(String siteKey) {
        if (domainMap.containsKey(siteKey)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Work around for bad shape files
     *
     * @param cwa
     * @param dataKey
     * @return
     */
    private List<Long> getCountyFips(String cwa, String dataKey) {

        List<String> resolutions = ScanUtils.getResolutionLevels("county");
        List<Long> list = null;

        for (String resolution : resolutions) {
            double res = Double
                    .parseDouble(resolution.substring(9).replace('_', '.'));

            if (res >= 0.004) {

                try {
                    list = FFMPUtils.getUniqueCountyFips(cwa, getMaxExtent(),
                            getSiteExtents(dataKey), mode.getMode(),
                            resolution);
                } catch (Exception e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Unable to create FFMP Template for this dataKey: "
                                    + dataKey,
                            e);
                }

                if (!list.isEmpty()) {
                    break;
                }
            }
        }

        return list;
    }

    /**
     * Get the county info
     *
     * @param siteKey
     * @param countyPfaf
     * @return
     */
    public String getCountyStateName(String siteKey, Long countyPfaf) {

        String rname = null;

        if (countyMap == null || countyMap.size() == 0) {
            getCounties(siteKey);
        }

        FFMPCounty county = countyMap.get(countyPfaf);
        if (county == null) {
            county = FFMPUtils.getCounty(countyPfaf, MODE.CAVE.getMode());
            countyMap.put(countyPfaf, county);
        }

        if (county != null) {
            StringBuilder name = new StringBuilder();
            name.append(county.getState() + ", ");
            name.append(county.getCountyName());
            rname = name.toString();
        }

        return rname;
    }

    /**
     * Causes a general recreation of template and template-related binary files
     * (Aggregate Geometries, Source Bins, FFTI files)
     */
    public void dumpTemplates() {
        LocalizationContext siteCtx = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
        LocalizationContext configCtx = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.CONFIGURED);
        LocalizationContext[] contexts = new LocalizationContext[] { siteCtx,
                configCtx };
        ILocalizationFile[] lfs = pathManager.listFiles(contexts, "ffmp",
                new String[] { ".bin" }, true, true);

        if (lfs != null) {
            for (ILocalizationFile lf : lfs) {
                try {
                    lf.delete();
                } catch (LocalizationException e) {
                    statusHandler.handle(Priority.PROBLEM,
                            "Error deleting " + lf.getPath(), e);
                }
            }
        }

        // write out the config XML so templates
        // don't keep regening
        ftcm.setRegenerate(false);
        ftcm.saveConfigXml();
        synchronized (this) {
            template = null;
        }
    }

    /**
     * @see #getCountyStateName()
     */
    public static boolean isCountyRow(String huc, String rowName) {

        return "COUNTY".equals(huc) && rowName.contains(",");

    }

    public synchronized List<Long> getVgbLookupIdsByCounty(String dataKey,
            Long pfaf, String huc, String rowName) {

        String stateCommaCnty = rowName;

        Map<String, HashMap<String, ArrayList<FFMPVirtualGageBasinMetaData>>> virtualMap = vgbsInCounty
                .get(dataKey);

        List<Long> result = new ArrayList<>();

        for (DomainXML domain : domains) {
            Map<String, ArrayList<FFMPVirtualGageBasinMetaData>> map = virtualMap
                    .get(domain.getCwa());
            if (map != null) {
                List<FFMPVirtualGageBasinMetaData> list = map
                        .get(stateCommaCnty.trim().toUpperCase());

                if (list != null && !list.isEmpty()) {

                    for (FFMPVirtualGageBasinMetaData md : list) {
                        if (!result.contains(md.getLookupId())) {
                            result.add(md.getLookupId());
                        }
                    }
                }
            }
        }

        return result;
    }

}
