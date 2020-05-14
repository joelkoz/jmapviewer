// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;


/**
 * A TileController manages the loading of individual tiles from a tile
 * source, coordinating between the tile loader and tile cache.
 */
public class TileController {

    private TileLoader tileLoader;
    private TileCache tileCache;
    private TileSource tileSource;
    TileLoaderListener listener;
    
    
    /**
     * @param tileSource The initial source to load tiles from
     * @param listener The listener to be notified when tiles have been loaded.
     */
    public TileController(TileSource tileSource, TileCache tileCache, TileLoaderListener listener) {
        this.listener = listener;
        this.tileCache = tileCache;
        this.setTileSource(tileSource);
    }

    /**
     * retrieves a tile from the cache. If the tile is not present in the cache
     * a load job is added to the working queue of {@link TileLoader}.
     *
     * @param tilex the X position of the tile
     * @param tiley the Y position of the tile
     * @param zoom the zoom level of the tile
     * @return specified tile from the cache or <code>null</code> if the tile
     *         was not found in the cache.
     */
    public Tile getTile(int tilex, int tiley, int zoom) {
        int max = 1 << zoom;
        if (tilex < 0 || tilex >= max || tiley < 0 || tiley >= max)
            return null;
        Tile tile = tileCache.getTile(tileSource, tilex, tiley, zoom);
        if (tile == null) {
            tile = new Tile(tileSource, tilex, tiley, zoom);
            tileCache.addTile(tile);
            tile.loadPlaceholderFromCache(tileCache);
        }
        if (tile.hasError()) {
            tile.loadPlaceholderFromCache(tileCache);
        }
        if (!tile.isLoaded()) {
            tileLoader.createTileLoaderJob(tile).startTileLoad();
        }
        return tile;
    }

    public TileCache getTileCache() {
        return tileCache;
    }

    public void setTileCache(TileCache tileCache) {
        this.tileCache = tileCache;
    }
    
    public TileSource getTileSource() {
        return tileSource;
    }

    public void setTileSource(TileSource tileSource) {
        this.tileSource = tileSource;
        this.tileLoader = tileSource.getTileLoader(this.listener);
    }

    
    /**
     * Removes all jobs from the queue that are currently not being processed by
     * the tile loader (if any).
     */
    public void cancelOutstandingJobs() {
        tileLoader.cancelOutstandingTasks();
    }
}
