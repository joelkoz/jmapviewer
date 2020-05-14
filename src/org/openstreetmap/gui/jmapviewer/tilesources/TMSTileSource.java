// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

/**
 * TMS tile source. This class remains in the hierarchy for backward compatibility
 * reasons. Most of what existed here was pushed down to AbstractMercatorTileSource or
 * AbstractOnlineTIMSTileSource. This class will be removed in future versions.
 * @deprecated
 */
public class TMSTileSource extends AbstractOnlineTMSTileSource {

    /**
     * Constructs a new {@code TMSTileSource}.
     * @param info tile source information
     */
    public TMSTileSource(TileSourceInfo info) {
        super(info);
    }

}
