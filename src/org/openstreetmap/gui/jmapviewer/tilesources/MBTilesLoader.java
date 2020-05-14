package org.openstreetmap.gui.jmapviewer.tilesources;

import java.io.InputStream;

import org.imintel.mbtiles4j.MBTilesReader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

/**
 * A tile loader that can load tiles from a local MBTiles source
 * 
 * @author Joel Kozikowski
 */
public class MBTilesLoader extends AbstractTileLoader {

    /**
     * A TileJob that will load a single tile from the MBTiles reader
     * @author Joel Kozikowski
     */
    private final class MBTilesJob implements TileJob {

        private final org.openstreetmap.gui.jmapviewer.Tile jmvTile;
        private InputStream input;

        private MBTilesJob(org.openstreetmap.gui.jmapviewer.Tile jmvTile) {
            this.jmvTile = jmvTile;
        }

        @Override
        public void run() {
            
            if (mbt == null) {
                // We failed to open the MBTiles file.
                return;
            }
            
            synchronized (jmvTile) {
                
                if ((jmvTile.isLoaded() && !jmvTile.hasError()) || jmvTile.isLoading())
                    return;
                
                jmvTile.initLoading();
            }
            
            boolean success = false;
            try {
               int z = jmvTile.getZoom();
               int x = jmvTile.getXtile();
               int y = jmvTile.getYtile();
               if (flipYCoordinate) {
                  y = (1 << z) - y - 1;
               }
               org.imintel.mbtiles4j.Tile mbTile = mbt.getTile(z, x, y);
               if (mbTile != null) {
                   jmvTile.loadImage(mbTile.getData());
                   success = true;
               }
               else {
                   throw new Exception("Tile source does not contain tile for " + jmvTile.getZoom() + "/" + jmvTile.getXtile() + "/" + jmvTile.getYtile());
               }
            } catch (Exception e) {
                jmvTile.setError(e.getMessage());
                if (input == null) {
                    try {
                        System.err.println("Failed loading " + jmvTile.getKey() + ": "
                                +e.getClass() + ": " + e.getMessage());
                        e.printStackTrace();
                    } catch (Exception ioe) {
                        ioe.printStackTrace();
                    }
                }
            } finally {
                jmvTile.finishLoading();
                listener.tileLoadingFinished(jmvTile, success);
            }
        }

        @Override
        public void startTileLoad() {
            startTileLoad(false);
        }

        @Override
        public void startTileLoad(boolean force) {
            jobDispatcher.execute(this);
        }
    }
    
    
    private MBTilesReader mbt;
    private boolean flipYCoordinate;
    
    /**
     * @param mbtReader An MBTilesReader that contains the data being loaded
     * @param listener Any loader/listener object that wants to be notified whenever a tile is loaded by the tile job.
     * @param flipYCoordinate If TRUE, the Y coordinate requested by the controller (always in XYZ format) will be
     *   "flipped" into TMS format before it is loaded.
     */
    public MBTilesLoader(MBTilesReader mbtReader, TileLoaderListener listener, boolean flipYCoordinate) {
        super(listener);
        this.mbt = mbtReader;
        this.flipYCoordinate = flipYCoordinate;
    }

    
    @Override
    public TileJob createTileLoaderJob(org.openstreetmap.gui.jmapviewer.Tile jmvTile) {
        return new MBTilesJob(jmvTile);
    }


    @Override
    public int getMaxLoadRetries() {
        return 0;
    }


}
