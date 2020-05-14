package org.openstreetmap.gui.jmapviewer.tilesources;

import java.awt.Point;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.OsmMercator;
import org.openstreetmap.gui.jmapviewer.Projected;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileRange;
import org.openstreetmap.gui.jmapviewer.TileXY;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.IProjected;

public abstract class AbstractMercatorTileSource extends AbstractTileSource {

    protected OsmMercator osmMercator;
    protected int tileSize;

    
    public AbstractMercatorTileSource(String name, String id) {
        this(name, id, 0);
    }    
    
    
    public AbstractMercatorTileSource(String name, String id, int tileSize) {
        super(name, id);
        this.tileSize = tileSize;
        this.osmMercator = new OsmMercator(this.getTileSize());
    }

    @Override
    public int getMaxZoom() {
        return 21;
    }

    
    @Override
    public int getMinZoom() {
        return 0;
    }


    /**
     * @return default tile size to use, when not set in Imagery Preferences
     */
    @Override
    public int getDefaultTileSize() {
        return OsmMercator.DEFAUL_TILE_SIZE;
    }

    
    /*
     * Unless an explicit tile size was passed in, use the default size...
     */
    @Override
    public int getTileSize() {
        if (tileSize <= 0) {
            return getDefaultTileSize();
        }
        return tileSize;
    }
  
    @Override
    public int getTileXMax(int zoom) {
        return getTileMax(zoom);
    }

    @Override
    public int getTileXMin(int zoom) {
        return 0;
    }

    @Override
    public int getTileYMax(int zoom) {
        return getTileMax(zoom);
    }

    @Override
    public int getTileYMin(int zoom) {
        return 0;
    }

    
    protected static int getTileMax(int zoom) {
        return (int) Math.pow(2.0, zoom) - 1;
    }

        
    @Override
    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        return osmMercator.getDistance(lat1, lon1, lat2, lon2);
    }

    
    @Override
    public Point latLonToXY(double lat, double lon, int zoom) {
        return new Point(
                (int) Math.round(osmMercator.lonToX(lon, zoom)),
                (int) Math.round(osmMercator.latToY(lat, zoom))
                );
    }

    
    @Override
    public ICoordinate xyToLatLon(int x, int y, int zoom) {
        return new Coordinate(
                osmMercator.yToLat(y, zoom),
                osmMercator.xToLon(x, zoom)
                );
    }

    @Override
    public TileXY latLonToTileXY(double lat, double lon, int zoom) {
        return new TileXY(
                osmMercator.lonToX(lon, zoom) / getTileSize(),
                osmMercator.latToY(lat, zoom) / getTileSize()
                );
    }

    @Override
    public ICoordinate tileXYToLatLon(int x, int y, int zoom) {
        return new Coordinate(
                osmMercator.yToLat(y * getTileSize(), zoom),
                osmMercator.xToLon(x * getTileSize(), zoom)
                );
    }

    @Override
    public IProjected tileXYtoProjected(int x, int y, int zoom) {
        double mercatorWidth = 2 * Math.PI * OsmMercator.EARTH_RADIUS;
        double f = mercatorWidth * getTileSize() / osmMercator.getMaxPixels(zoom);
        return new Projected(f * x - mercatorWidth / 2, -(f * y - mercatorWidth / 2));
    }

    @Override
    public TileXY projectedToTileXY(IProjected p, int zoom) {
        double mercatorWidth = 2 * Math.PI * OsmMercator.EARTH_RADIUS;
        double f = mercatorWidth * getTileSize() / osmMercator.getMaxPixels(zoom);
        return new TileXY((p.getEast() + mercatorWidth / 2) / f, (-p.getNorth() + mercatorWidth / 2) / f);
    }
    
    
    @Override
    public boolean isInside(Tile inner, Tile outer) {
        int dz = inner.getZoom() - outer.getZoom();
        if (dz < 0) return false;
        return outer.getXtile() == inner.getXtile() >> dz &&
                outer.getYtile() == inner.getYtile() >> dz;
    }

    
    @Override
    public TileRange getCoveringTileRange(Tile tile, int newZoom) {
        if (newZoom <= tile.getZoom()) {
            int dz = tile.getZoom() - newZoom;
            TileXY xy = new TileXY(tile.getXtile() >> dz, tile.getYtile() >> dz);
            return new TileRange(xy, xy, newZoom);
        } else {
            int dz = newZoom - tile.getZoom();
            TileXY t1 = new TileXY(tile.getXtile() << dz, tile.getYtile() << dz);
            TileXY t2 = new TileXY(t1.getX() + (1 << dz) - 1, t1.getY() + (1 << dz) - 1);
            return new TileRange(t1, t2, newZoom);
        }
    }
    
    
    @Override
    public String getServerCRS() {
        return "EPSG:3857";
    }
    
}
