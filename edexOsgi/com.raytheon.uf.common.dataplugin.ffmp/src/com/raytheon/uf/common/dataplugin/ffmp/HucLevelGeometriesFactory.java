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

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.SaveableOutputStream;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * Manage a cache of geometries and envelopes for different areas/resolutions.
 * The first time FFMP is loaded the geometries will be simplified and stored to
 * localization for faster retrieval. All geometries and envelopes are held in
 * memory by a soft reference or until they are explicitly cleared.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date          Ticket#  Engineer  Description
 * ------------- -------- --------- --------------------------------------------
 * Dec 09, 2010           rjpeter   Initial creation
 * Apr 25, 2013  1954     bsteffen  Decompress ffmp geometries to save time
 *                                  loading them.
 * Apr 25, 2013  1954     bsteffen  Undo last commit to avoid invalid geoms.
 * Jul 03, 2013  2152     rjpeter   Use streams for serialization
 * Nov 12, 2015  4834     njensen   Changed LocalizationOpFailedException to
 *                                  LocalizationException
 * Feb 15, 2016  5244     nabowle   Replace deprecated LocalizationFile methods.
 * Aug 09, 2016  5819     mapeters  Geom/envelope files moved from SITE to
 *                                  CONFIGURED
 * Aug 15, 2016  5819     mapeters  Geom/envelope files moved back to SITE since
 *                                  code that writes them is called by CAVE
 * Sep 13, 2017  20297    lshi      FFMP AlertViz errors when changing layers and 
 *                                  opening basin trend graphs
 * 
 * </pre>
 * 
 * @author rjpeter
 */
public class HucLevelGeometriesFactory {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(HucLevelGeometriesFactory.class);

    private static final double SIMPLIFICATION_LEVEL = 0.0005;

    private static final double BUFFER_LEVEL = SIMPLIFICATION_LEVEL / 4;

    private static HucLevelGeometriesFactory instance = new HucLevelGeometriesFactory();

    private static final String hucGeomBasePath = "ffmp"
            + IPathManager.SEPARATOR + "aggrGeom";

    private final IPathManager pathManager;

    public static HucLevelGeometriesFactory getInstance() {
        return instance;
    }

    private final ConcurrentMap<String, SoftReference<Map<Long, Geometry>>> geomCache = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, SoftReference<Map<Long, Envelope>>> envCache = new ConcurrentHashMap<>();

    private HucLevelGeometriesFactory() {
        pathManager = PathManagerFactory.getPathManager();
    }

    public synchronized Geometry getGeometry(FFMPTemplates template,
            String dataKey, String domain, String huc, Long pfaf)
            throws Exception {
        return getGeometries(template, dataKey, domain, huc).get(pfaf);
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<Long, Geometry> getGeometries(
            FFMPTemplates template, String dataKey, String cwa, String huc)
            throws Exception {
        String key = dataKey + cwa + huc;
        SoftReference<Map<Long, Geometry>> ref = geomCache.get(key);
        Map<Long, Geometry> map = null;
        if (ref != null) {
            map = ref.get();
        }

        // load from disk
        if (map == null) {
            // generate map/check all entries in list vs map
            Set<Long> pfafs = template.getMap(dataKey, cwa, huc).keySet();
            Collection<Long> pfafsToGenerate = null;

            LocalizationContext lc = pathManager.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
            LocalizationFile f = pathManager.getLocalizationFile(lc,
                    getGeomPath(dataKey, cwa, huc));

            if (f.exists()) {
                boolean deleteFile = false;
                long length = f.getFile().length();

                // read from disk in 8k chunks
                int bufferSize = 8 * 1024;
                if (bufferSize > length) {
                    bufferSize = (int) length;
                }

                try (InputStream is = f.openInputStream();
                        GZIPInputStream gis = new GZIPInputStream(is,
                                bufferSize)) {

                    map = SerializationUtil.transformFromThrift(Map.class, gis);
                    int sizeGuess = Math.max(
                            Math.abs(pfafs.size() - map.size()), 10);
                    pfafsToGenerate = new ArrayList<>(sizeGuess);
                    for (Long pfafToCheck : pfafs) {
                        if (!map.containsKey(pfafToCheck)) {
                            pfafsToGenerate.add(pfafToCheck);
                        }
                    }
                } catch (Exception e) {
                    String msg = "Failed to read geometry map from file: "
                            + f.getPath() + ". Deleting file and regenerating.";
                    statusHandler.handle(Priority.WARN, msg, e);

                    deleteFile = true;
                    pfafsToGenerate = pfafs;
                } finally {
                    if (deleteFile) {
                        try {
                            f.delete();
                        } catch (LocalizationException e) {
                            String msg = "Error deleting file: " + f.getPath();
                            statusHandler.handle(Priority.WARN, msg, e);
                        }
                    }
                }
            } else {
                pfafsToGenerate = pfafs;
            }

            if (pfafsToGenerate.size() > 0) {
                Map<Long, Geometry> tmp = null;
                if (FFMPRecord.ALL.equals(huc)) {
                    tmp = generateSimplifiedGeometry(template, dataKey, cwa,
                            pfafs);
                } else {
                    tmp = generateUnifiedGeometry(template, dataKey, cwa, huc,
                            pfafs);
                }

                if (map == null) {
                    map = tmp;
                } else {
                    map.putAll(tmp);
                }

                persistGeometryMap(dataKey, cwa, huc, map);
            }

            geomCache.put(key, new SoftReference<>(map));
        }

        return map;
    }

    protected synchronized Map<Long, Geometry> generateSimplifiedGeometry(
            FFMPTemplates template, String dataKey, String cwa,
            Collection<Long> pfafs) {
        Map<Long, Geometry> rawGeometries = template.getRawGeometries(dataKey,
                cwa);
        Map<Long, Geometry> simplifiedGeometries = new HashMap<>(
                (int) (pfafs.size() * 1.3) + 1);
        GeometryFactory gf = new GeometryFactory();

        for (Long pfaf : pfafs) {
            Geometry tmpGeom = rawGeometries.get(pfaf);
            if (tmpGeom != null) {
                tmpGeom = TopologyPreservingSimplifier.simplify(tmpGeom,
                        SIMPLIFICATION_LEVEL);
                // add slight buffer to fill internal sections
                tmpGeom = gf.createGeometryCollection(
                        new Geometry[] { tmpGeom }).buffer(BUFFER_LEVEL);
                tmpGeom = TopologyPreservingSimplifier.simplify(tmpGeom,
                        SIMPLIFICATION_LEVEL / 2);
                simplifiedGeometries.put(pfaf, tmpGeom);
            }
        }

        return simplifiedGeometries;
    }

    protected synchronized Map<Long, Geometry> generateUnifiedGeometry(
            FFMPTemplates template, String dataKey, String cwa, String huc,
            Collection<Long> aggrPfafs) throws Exception {
        Map<Long, Geometry> rval = null;
        rval = new HashMap<>((int) (aggrPfafs.size() * 1.3) + 1);
        GeometryFactory gf = new GeometryFactory();
        String childHuc = getChildHuc(template, huc);
        Map<Long, Collection<Long>> childHucMapping = getChildHucMapping(
                template, dataKey, cwa, huc, childHuc);
        Map<Long, Geometry> childHucGeometries = getGeometries(template,
                dataKey, cwa, childHuc);

        // organize child pfafs by parent aggr
        for (Long aggrPfaf : aggrPfafs) {
            Collection<Long> childPfafs = childHucMapping.get(aggrPfaf);

            if ((childPfafs != null) && (childPfafs.size() > 0)) {
                Geometry[] hucGeometries = new Geometry[childPfafs.size()];
                Iterator<Long> iter = childPfafs.iterator();
                int i = 0;
                while (iter.hasNext()) {
                    Long pfaf = iter.next();
                    hucGeometries[i++] = childHucGeometries.get(pfaf);
                }

                if (hucGeometries.length > 0){
                    Geometry tmpGeom = gf.createGeometryCollection(hucGeometries)
                            .buffer(0);
                    try {
                        if (tmpGeom instanceof Polygon) {
                            tmpGeom = deholer(gf, (Polygon) tmpGeom);
                        } else if (tmpGeom instanceof MultiPolygon) {
                            MultiPolygon mp = (MultiPolygon) tmpGeom;
                            int numGeoms = mp.getNumGeometries();
                            hucGeometries = new Geometry[numGeoms];
                            for (i = 0; i < numGeoms; i++) {
                                hucGeometries[i] = deholer(gf,
                                        (Polygon) mp.getGeometryN(i));
                            }
                            tmpGeom = gf.createGeometryCollection(hucGeometries)
                                    .buffer(0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rval.put(aggrPfaf, tmpGeom);
                }
            }
        }

        return rval;
    }

    /**
     * Attempts to remove interior holes on a polygon. Will take up to 3 passes
     * over the polygon expanding any interior rings and merging rings back in.
     * 
     * @param gf
     * @param p
     * @return
     */
    protected Geometry deholer(GeometryFactory gf, Polygon p) {
        int interiorRings = p.getNumInteriorRing();
        int iterations = 0;
        while ((interiorRings > 0) && (iterations < 3)) {
            Geometry[] hucGeometries = new Geometry[interiorRings + 1];
            hucGeometries[0] = p;
            for (int i = 0; i < interiorRings; i++) {
                hucGeometries[i + 1] = p.getInteriorRingN(i).buffer(
                        BUFFER_LEVEL);
            }
            p = (Polygon) gf.createGeometryCollection(hucGeometries).buffer(0);
            iterations++;
            interiorRings = p.getNumInteriorRing();
        }

        return p;
    }

    /**
     * Gets the huc that is one aggregation smaller.
     * 
     * @param tempate
     * @param huc
     * @return
     */
    protected String getChildHuc(FFMPTemplates tempate, String huc) {
        String rval = FFMPRecord.ALL;
        if (huc.startsWith("HUC")) {
            int totalHuc = tempate.getTotalHucLevels();

            if (!huc.equals("HUC" + (totalHuc - 1))) {
                int myHucNum = Integer.parseInt(huc.substring(3));
                rval = "HUC" + (myHucNum + 1);
            }
        }
        return rval;
    }

    /**
     * Returns a map of pfafs to a collection of child pfafs for the child huc.
     * 
     * @param template
     * @param cwa
     * @param huc
     * @param childHuc
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Map<Long, Collection<Long>> getChildHucMapping(
            FFMPTemplates template, String dataKey, String cwa, String huc,
            String childHuc) {

        if (FFMPRecord.ALL.equals(childHuc)) {
            return (Map<Long, Collection<Long>>) template.getMap(dataKey, cwa,
                    huc);
        } else if (childHuc.startsWith("HUC")) {
            int myHucNum = 0;
            if (huc.startsWith("HUC")) {
                myHucNum = Integer.parseInt(huc.substring(3));
            }
            int childHucNum = Integer.parseInt(childHuc.substring(3));
            long divisor = (long) Math.pow(10, childHucNum - myHucNum);

            Set<Long> childPfafs = template.getMap(dataKey, cwa, childHuc)
                    .keySet();
            Map<Long, Collection<Long>> childHucMapping = new HashMap<>(
                    (int) ((childPfafs.size() / 10) * 1.3) + 1);
            for (Long childPfaf : childPfafs) {
                Long pfaf = new Long(childPfaf / divisor);
                Collection<Long> mappedChildPfafs = childHucMapping.get(pfaf);
                if (mappedChildPfafs == null) {
                    mappedChildPfafs = new ArrayList<>();
                    childHucMapping.put(pfaf, mappedChildPfafs);
                }
                mappedChildPfafs.add(childPfaf);
            }

            return childHucMapping;
        }

        throw new IllegalArgumentException("Huc " + childHuc
                + " is an invalid child huc for " + huc);
    }

    protected synchronized void persistGeometryMap(String dataKey, String cwa,
            String huc, Map<Long, Geometry> map) throws Exception {
        /*
         * TODO: we should be saving the geometry files to CONFIGURED, but we
         * can't currently since this code is called from CAVE
         */
        LocalizationContext lc = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
        ILocalizationFile lf = pathManager.getLocalizationFile(lc,
                getGeomPath(dataKey, cwa, huc));

        try (SaveableOutputStream mapSos = lf.openOutputStream();
                GZIPOutputStream mapGos = new GZIPOutputStream(mapSos)) {
            mapGos.write(SerializationUtil.transformToThrift(map));
            mapGos.finish();
            mapGos.flush();
            mapSos.save();
        }
    }

    protected synchronized String getGeomPath(String dataKey, String cwa,
            String huc) {
        return LocalizationUtil.join(hucGeomBasePath, dataKey, cwa, huc,
                "geometries.bin");
    }

    protected synchronized String getEnvelopePath(String dataKey, String cwa,
            String huc) {
        return LocalizationUtil.join(hucGeomBasePath, dataKey, cwa, huc,
                "envelopes.bin");
    }

    public synchronized Envelope getEnvelope(FFMPTemplates template,
            String dataKey, String cwa, String huc, Long pfafId) {
        return getEnvelopes(template, dataKey, cwa, huc).get(pfafId);
    }

    @SuppressWarnings("unchecked")
    public synchronized Map<Long, Envelope> getEnvelopes(
            FFMPTemplates template, String dataKey, String cwa, String huc) {
        String key = dataKey + cwa + huc;
        SoftReference<Map<Long, Envelope>> ref = envCache.get(key);
        Map<Long, Envelope> map = null;
        if (ref != null) {
            map = ref.get();
        }

        if (map == null) {
            // generate map/check all entries in list vs map
            Set<Long> pfafs = template.getMap(dataKey, cwa, huc).keySet();
            Collection<Long> pfafsToGenerate = null;

            LocalizationContext lc = pathManager.getContext(
                    LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
            LocalizationFile f = pathManager.getLocalizationFile(lc,
                    getEnvelopePath(dataKey, cwa, huc));

            if (f.exists()) {
                boolean deleteFile = false;
                long length = f.getFile().length();

                // read from disk in 8k chunks
                int bufferSize = 8 * 1024;
                if (bufferSize > length) {
                    bufferSize = (int) length;
                }
                try (InputStream is = f.openInputStream();
                        GZIPInputStream gis = new GZIPInputStream(is,
                                bufferSize)) {

                    map = SerializationUtil.transformFromThrift(Map.class, gis);
                    int sizeGuess = Math.max(
                            Math.abs(pfafs.size() - map.size()), 10);
                    pfafsToGenerate = new ArrayList<>(sizeGuess);
                    for (Long pfafToCheck : pfafs) {
                        if (!map.containsKey(pfafToCheck)) {
                            pfafsToGenerate.add(pfafToCheck);
                        }
                    }
                } catch (Exception e) {
                    String msg = "Failed to read envelope map from file: "
                            + f.getPath() + ". Deleting file and regenerating.";
                    statusHandler.handle(Priority.WARN, msg, e);

                    deleteFile = true;
                    pfafsToGenerate = pfafs;
                } finally {
                    if (deleteFile) {
                        try {
                            f.delete();
                        } catch (LocalizationException e) {
                            String msg = "Error deleting file: " + f.getPath();
                            statusHandler.handle(Priority.WARN, msg, e);
                        }
                    }
                }
            } else {
                pfafsToGenerate = pfafs;
            }

            if (pfafsToGenerate.size() > 0) {
                if (map == null) {
                    map = new HashMap<>(
                            (int) (pfafsToGenerate.size() * 1.3) + 1);
                }
                try {
                    Map<Long, Geometry> geoms = getGeometries(template,
                            dataKey, cwa, huc);
                    for (Long pfaf : pfafsToGenerate) {
                        Envelope env = geoms.get(pfaf).getEnvelopeInternal();
                        map.put(pfaf, env);
                    }

                    persistEnvelopeMap(dataKey, cwa, huc, map);
                } catch (Exception e) {
                    System.err.println("Can't locate geometries:  No file: "
                            + dataKey + " CWA: " + cwa);
                }
            }

            envCache.put(key, new SoftReference<>(map));
        }

        return map;
    }

    protected synchronized void persistEnvelopeMap(String dataKey, String cwa,
            String huc, Map<Long, Envelope> map) throws Exception {
        /*
         * TODO: we should be saving the envelope files to CONFIGURED, but we
         * can't currently since this code is called from CAVE
         */
        LocalizationContext lc = pathManager.getContext(
                LocalizationType.COMMON_STATIC, LocalizationLevel.SITE);
        ILocalizationFile lf = pathManager.getLocalizationFile(lc,
                getEnvelopePath(dataKey, cwa, huc));

        try (SaveableOutputStream sos = lf.openOutputStream();
                GZIPOutputStream gos = new GZIPOutputStream(sos)) {
            SerializationUtil.transformToThriftUsingStream(map, gos);
            gos.finish();
            gos.flush();
            sos.save();
        }
    }

    public synchronized void clear() {
        geomCache.clear();
        envCache.clear();
    }

    public synchronized void clearGeomCache(String hucLevel) {
        Iterator<String> iter = geomCache.keySet().iterator();
        while (iter.hasNext()) {
            String hucKey = iter.next();
            if (hucKey.startsWith(hucLevel)) {
                iter.remove();
            }
        }
    }
}
