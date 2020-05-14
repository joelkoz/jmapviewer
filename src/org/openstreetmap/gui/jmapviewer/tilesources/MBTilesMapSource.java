package org.openstreetmap.gui.jmapviewer.tilesources;

import java.io.File;

import org.imintel.mbtiles4j.MBTilesReadException;
import org.imintel.mbtiles4j.MBTilesReader;
import org.imintel.mbtiles4j.model.MetadataEntry;
import org.imintel.mbtiles4j.model.MetadataEntry.TileMimeType;
import org.imintel.mbtiles4j.model.MetadataEntry.TileSetType;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

public class MBTilesMapSource extends AbstractMercatorTileSource {

    private MBTilesReader mbtReader;
    private MBTilesLoader loader;
    private int minZoom;
    private int maxZoom;
    private MetadataEntry meta;
    private boolean flipYCoordinate;

    
    
    /**
     * @param name A tile layer name as displayed to the user
     * @param id A unique id for this tile source. Unlike the name it has to be unique and 
     *           has to consist only of characters valid for filenames
     * @param fileName The SQLite file that contains the MBTiles set
     */
    public MBTilesMapSource(String name, String id, String fileName) {
        this(name, id, fileName, true);
    }    
    
    
    /**
     * @param name A tile layer name as displayed to the user
     * @param id A unique id for this tile source. Unlike the name it has to be unique and 
     *           has to consist only of characters valid for filenames
     * @param fileName The SQLite file that contains the MBTiles set
     * @param flipYCoordinate If TRUE, the Y coordinate of tiles requested by the tile controller (always in XYZ format) will be
     *   "flipped" into TMS format before it is loaded.  This is normally TRUE for files that follow the MBTiles
     *   specification correctly, but there may be cases of files where you need this "flip" not to happen.
     */
    public MBTilesMapSource(String name, String id, String fileName, boolean flipYCoordinate) {
        super(name, id);
        try {
            this.mbtReader = new MBTilesReader(new File(fileName));
            this.minZoom = mbtReader.getMinZoom();
            this.maxZoom = mbtReader.getMaxZoom();
            meta = mbtReader.getMetadata();
            this.flipYCoordinate = flipYCoordinate;
         }
         catch (MBTilesReadException ex) {
             System.err.println("Could not read MBTiles file. " + ex.getMessage());
             ex.printStackTrace();
             mbtReader = null;
         }
    }
    
    
    @Override
    public TileLoader getTileLoader(TileLoaderListener listener) {
        if (this.loader == null) {
            this.loader = new MBTilesLoader(this.mbtReader, listener, flipYCoordinate);
        }
        return loader;
    }


    /**
     * Get the minimum zoom level that exists in the MBtiles file.
     */
    @Override
    public int getMinZoom() {
        return minZoom;
    }


    /**
     * Get the maximum zoom level that exists in the MBtiles file.
     */
    @Override
    public int getMaxZoom() {
        return maxZoom;
    }
    
    private String tsType = null;
    
    /**
     * Returns the type of this tileset ("baselayer" or "overlay")
     */
    public String getTilesetType() {

        if (tsType == null) {
            if (meta != null) {
                TileSetType tst = meta.getTilesetType();
                if (tst != null) {
                    tsType = tst.toString();
                    return tsType;
                }
            }
            tsType = "???";
        }
        
        return tsType;
    }

    
    private String tmType = null;
    
    /**
     * Returns the Mime Type of the tiles that are stored in this tileset ("JPG" or "PNG")
     */
    public String getTileMimeType() {

        if (tmType == null) {
            if (meta != null) {
                TileMimeType tmt = meta.getTileMimeType();
                if (tmt != null) {
                    tmType = tmt.toString();
                    return tmType;
                }
            }
            tmType = "???";
        }
        
        return tmType;
    }
    
    
    /**
     * @return image extension, of the tile types stored in the MBtiles source
     */
    public String getExtension() {
        return this.getTileMimeType();
    }

}
