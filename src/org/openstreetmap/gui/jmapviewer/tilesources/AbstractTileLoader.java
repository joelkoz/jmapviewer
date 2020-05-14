// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;

/**
 * A {@link TileLoader} implementation that loads tiles from OSM.
 *
 * @author Jan Peter Stotz
 * @author Joel Kozikowski
 */
public abstract class AbstractTileLoader implements TileLoader {

    protected static final ThreadPoolExecutor jobDispatcher = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public boolean hasOutstandingTasks() {
        return jobDispatcher.getTaskCount() > jobDispatcher.getCompletedTaskCount();
    }

    @Override
    public void cancelOutstandingTasks() {
        jobDispatcher.getQueue().clear();
    }

    /**
     * Sets the maximum number of concurrent connections the tile loader will do
     * @param num number of concurrent connections
     */
    public static void setConcurrentConnections(int num) {
        jobDispatcher.setMaximumPoolSize(num);
    }
}
