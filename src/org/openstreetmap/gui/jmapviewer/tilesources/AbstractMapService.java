// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

    
   /**
    * Extracts metadata about the tile based on HTTP headers
    *
    * @param headers HTTP headers from Tile Source server
    * @return tile metadata
    */
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
    public boolean isModTileFeatures() {
        return modTileFeatures;
    }
 
    
    @Override
    public TileLoader getTileLoader(TileLoaderListener listener) {
        return new MapServiceLoader(this, listener);
    }

}
