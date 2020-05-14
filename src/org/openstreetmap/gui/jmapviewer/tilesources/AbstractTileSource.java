// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.awt.Image;
import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileXY;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;

public abstract class AbstractTileSource implements TileSource {

    protected String name;
    protected String id;

    protected String attributionText;
    protected String attributionLinkURL;
    protected Image attributionImage;
    protected String attributionImageURL;
    protected String termsOfUseText;
    protected String termsOfUseURL;

    
    public AbstractTileSource(String name, String id) {
        this.name = name;
        this.id = id;
    }
    
    @Override
    public String getName() {
        return name;
    }

    
    @Override
    public String getId() {
        return id;
    }

    
    @Override
    public String toString() {
        return getName();
    }


    @Override
    public boolean requiresAttribution() {
        return attributionText != null || attributionLinkURL != null || attributionImage != null
                || termsOfUseText != null || termsOfUseURL != null;
    }

    @Override
    public String getAttributionText(int zoom, ICoordinate topLeft, ICoordinate botRight) {
        return attributionText;
    }

    @Override
    public String getAttributionLinkURL() {
        return attributionLinkURL;
    }

    @Override
    public Image getAttributionImage() {
        return attributionImage;
    }

    @Override
    public String getAttributionImageURL() {
        return attributionImageURL;
    }

    @Override
    public String getTermsOfUseText() {
        return termsOfUseText;
    }

    @Override
    public String getTermsOfUseURL() {
        return termsOfUseURL;
    }

    public void setAttributionText(String attributionText) {
        this.attributionText = attributionText;
    }

    public void setAttributionLinkURL(String attributionLinkURL) {
        this.attributionLinkURL = attributionLinkURL;
    }

    public void setAttributionImage(Image attributionImage) {
        this.attributionImage = attributionImage;
    }

    public void setAttributionImageURL(String attributionImageURL) {
        this.attributionImageURL = attributionImageURL;
    }

    public void setTermsOfUseText(String termsOfUseText) {
        this.termsOfUseText = termsOfUseText;
    }

    public void setTermsOfUseURL(String termsOfUseURL) {
        this.termsOfUseURL = termsOfUseURL;
    }

    @Override
    public Point latLonToXY(ICoordinate point, int zoom) {
        return latLonToXY(point.getLat(), point.getLon(), zoom);
    }

    @Override
    public ICoordinate xyToLatLon(Point point, int zoom) {
        return xyToLatLon(point.x, point.y, zoom);
    }

    @Override
    public TileXY latLonToTileXY(ICoordinate point, int zoom) {
        return latLonToTileXY(point.getLat(), point.getLon(), zoom);
    }

    @Override
    public ICoordinate tileXYToLatLon(TileXY xy, int zoom) {
        return tileXYToLatLon(xy.getXIndex(), xy.getYIndex(), zoom);
    }

    @Override
    public ICoordinate tileXYToLatLon(Tile tile) {
        return tileXYToLatLon(tile.getXtile(), tile.getYtile(), tile.getZoom());
    }
    
}
