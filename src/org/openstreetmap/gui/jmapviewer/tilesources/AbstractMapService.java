// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

/**
 * Class generalizing all tile based tile sources
 *
 * @author Wiktor Niesiobędzki
 *
 */
public abstract class AbstractMapService extends AbstractMercatorTileSource {

    protected String baseUrl;
    private final Map<String, Set<String>> noTileHeaders;
    private final Map<String, Set<String>> noTileChecksums;
    private final Map<String, String> metadataHeaders;
    protected boolean modTileFeatures;
    protected int maxZoom;
    protected int minZoom;

    /**
     * Creates an instance based on TileSource information
     *
     * @param info description of the Tile Source
     */
    public AbstractMapService(MapServiceInfo info) {
        super(info.getName(), info.getUrl());
        this.baseUrl = info.getUrl();
        if (baseUrl != null && baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length()-1);
        }
        this.noTileHeaders = info.getNoTileHeaders();
        this.noTileChecksums = info.getNoTileChecksums();
        this.metadataHeaders = info.getMetadataHeaders();
        this.modTileFeatures = info.isModTileFeatures();
        this.tileSize = info.getTileSize();
        minZoom = info.getMinZoom();
        maxZoom = info.getMaxZoom();
    }

    
    @Override
    public int getMinZoom() {
        return (minZoom == 0) ? super.getMinZoom() : minZoom;
    }

    @Override
    public int getMaxZoom() {
        return (maxZoom == 0) ? super.getMaxZoom() : maxZoom;
    }
    

    /**
     * @return image extension, used for URL creation
     */
    public String getExtension() {
        return "png";
    }

    /**
     * @param zoom level of the tile
     * @param tilex tile number in x axis
     * @param tiley tile number in y axis
     * @return String containg path part of URL of the tile
     * @throws IOException when subclass cannot return the tile URL
     */
    public String getTilePath(int zoom, int tilex, int tiley) throws IOException {
        return "/" + zoom + "/" + tilex + "/" + tiley + "." + getExtension();
    }

    /**
     * @return Base part of the URL of the tile source
     */
    public String getBaseUrl() {
        return this.baseUrl;
    }

    public String getTileUrl(int zoom, int tilex, int tiley) throws IOException {
        return this.getBaseUrl() + getTilePath(zoom, tilex, tiley);
    }

    @Override
    public boolean isNoTileAtZoom(Map<String, List<String>> headers, int statusCode, byte[] content) {
        if (noTileHeaders != null && headers != null) {
            for (Entry<String, Set<String>> searchEntry: noTileHeaders.entrySet()) {
                List<String> headerVals = headers.get(searchEntry.getKey());
                if (headerVals != null) {
                    for (String headerValue: headerVals) {
                        for (String val: searchEntry.getValue()) {
                            if (headerValue.matches(val)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        if (noTileChecksums != null && content != null) {
            for (Entry<String, Set<String>> searchEntry: noTileChecksums.entrySet()) {
                MessageDigest md = null;
                try {
                    md = MessageDigest.getInstance(searchEntry.getKey());
                } catch (NoSuchAlgorithmException e) {
                    break;
                }
                byte[] byteDigest = md.digest(content);
                final int len = byteDigest.length;

                char[] hexChars = new char[len * 2];
                for (int i = 0, j = 0; i < len; i++) {
                    final int v = byteDigest[i];
                    int vn = (v & 0xf0) >> 4;
                    hexChars[j++] = (char) (vn + (vn >= 10 ? 'a'-10 : '0'));
                    vn = (v & 0xf);
                    hexChars[j++] = (char) (vn + (vn >= 10 ? 'a'-10 : '0'));
                }
                for (String val: searchEntry.getValue()) {
                    if (new String(hexChars).equalsIgnoreCase(val)) {
                        return true;
                    }
                }
            }
        }
        return super.isNoTileAtZoom(headers, statusCode, content);
    }

    @Override
    public Map<String, String> getMetadata(Map<String, List<String>> headers) {
        Map<String, String> ret = new HashMap<>();
        if (metadataHeaders != null && headers != null) {
            for (Entry<String, String> searchEntry: metadataHeaders.entrySet()) {
                List<String> headerVals = headers.get(searchEntry.getKey());
                if (headerVals != null) {
                    for (String headerValue: headerVals) {
                        ret.put(searchEntry.getValue(), headerValue);
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public String getTileId(int zoom, int tilex, int tiley) {
        return this.baseUrl + "/" + zoom + "/" + tilex + "/" + tiley;
    }

    @Override
    public boolean isModTileFeatures() {
        return modTileFeatures;
    }
 
    
    @Override
    public TileLoader getTileLoader(TileLoaderListener listener) {
        return new OsmTileLoader(this, listener);
    }

}
