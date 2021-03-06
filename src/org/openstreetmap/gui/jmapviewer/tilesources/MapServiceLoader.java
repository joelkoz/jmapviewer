// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileJob;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;

/**
 * A {@link TileLoader} implementation that loads tiles online sources 
 * such as OSM.
 *
 * @author Jan Peter Stotz
 * @author Joel Kozikowski
 */
public class MapServiceLoader extends AbstractTileLoader {
    
    private final class MapServiceJob implements TileJob {
        private final Tile tile;
        private InputStream input;
        private boolean force;

        private MapServiceJob(Tile tile) {
            this.tile = tile;
        }

        @Override
        public void run() {
            synchronized (tile) {
                
                if ((tile.isLoaded() && !tile.hasError()) || tile.isLoading())
                    return;
                
                tile.initLoading();
            }
            boolean success = false;
            try {
                URLConnection conn = loadTileFromOsm(tile);
                if (force) {
                    conn.setUseCaches(false);
                }
                loadTileMetadata(tile, conn);
                if ("no-tile".equals(tile.getValue("tile-info"))) {
                    tile.setError("No tile at this zoom level");
                } else {
                    input = conn.getInputStream();
                    try {
                        tile.loadImage(input);
                    } finally {
                        input.close();
                        input = null;
                    }
                }
                success = true;
            } catch (IOException e) {
                tile.setError(e.getMessage());
                if (input == null) {
                    try {
                        System.err.println("Failed loading " + getTileUrl(tile) +": "
                                +e.getClass() + ": " + e.getMessage());
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            } finally {
                tile.finishLoading();
                listener.tileLoadingFinished(tile, success);
            }
        }

        @Override
        public void startTileLoad() {
            startTileLoad(false);
        }

        @Override
        public void startTileLoad(boolean force) {
            this.force = force;
            jobDispatcher.execute(this);
        }
    }

    /**
     * Holds the HTTP headers. Insert e.g. User-Agent here when default should not be used.
     */
    public Map<String, String> headers = new HashMap<>();

    public int timeoutConnect;
    public int timeoutRead;

    protected AbstractMapService mapService;

    public MapServiceLoader(AbstractMapService mapService, TileLoaderListener listener) {
        this(mapService, listener, null);
    }

    public MapServiceLoader(AbstractMapService mapService, TileLoaderListener listener, Map<String, String> headers) {
        super(listener);
        this.headers.put("Accept", "text/html, image/png, image/jpeg, image/gif, */*");
        this.headers.put("user-agent", "JMapViewer/2.0 (" + System.getProperty("java.version") + ")");
        if (headers != null) {
            this.headers.putAll(headers);
        }
        this.mapService = mapService;
    }

    @Override
    public TileJob createTileLoaderJob(final Tile tile) {
        return new MapServiceJob(tile);
    }

    
    protected String getTileUrl(Tile tile) throws IOException {
        return mapService.getTileUrl(tile.getZoom(), tile.getXtile(), tile.getYtile());
    }
    
    protected URLConnection loadTileFromOsm(Tile tile) throws IOException {
        URL url;
        url = new URL(getTileUrl(tile));
        URLConnection urlConn = url.openConnection();
        if (urlConn instanceof HttpURLConnection) {
            prepareHttpUrlConnection((HttpURLConnection) urlConn);
        }
        return urlConn;
    }

    protected void loadTileMetadata(Tile tile, URLConnection urlConn) {
        String str = urlConn.getHeaderField("X-VE-TILEMETA-CaptureDatesRange");
        if (str != null) {
            tile.putValue("capture-date", str);
        }
        str = urlConn.getHeaderField("X-VE-Tile-Info");
        if (str != null) {
            tile.putValue("tile-info", str);
        }

        Long lng = urlConn.getExpiration();
        if (lng.equals(0L)) {
            try {
                str = urlConn.getHeaderField("Cache-Control");
                if (str != null) {
                    for (String token: str.split(",")) {
                        if (token.startsWith("max-age=")) {
                            lng = Long.parseLong(token.substring(8)) * 1000 +
                                    System.currentTimeMillis();
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // ignore malformed Cache-Control headers
                if (JMapViewer.debug) {
                    System.err.println(e.getMessage());
                }
            }
        }
        if (!lng.equals(0L)) {
            tile.putValue("expires", lng.toString());
        }
    }

    protected void prepareHttpUrlConnection(HttpURLConnection urlConn) {
        for (Entry<String, String> e : headers.entrySet()) {
            urlConn.setRequestProperty(e.getKey(), e.getValue());
        }
        if (timeoutConnect != 0)
            urlConn.setConnectTimeout(timeoutConnect);
        if (timeoutRead != 0)
            urlConn.setReadTimeout(timeoutRead);
    }

    
    @Override
    public int getMaxLoadRetries() {
        return 2;
    }

}
