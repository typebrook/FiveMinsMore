package io.typebrook.fiveminsmore.offlinetile;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import io.typebrook.fiveminsmore.Constant;

import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.labels.TileBasedLabelStore;
import org.mapsforge.map.layer.renderer.DatabaseRenderer;
import org.mapsforge.map.layer.renderer.RendererJob;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.rule.RenderThemeFuture;
import org.osmdroid.tileprovider.MapTile;

import java.io.File;
import java.io.FileNotFoundException;

public class MapsforgeTileSource {
    private static final String TAG = "MapsforgeTileSource";

    // Reasonable defaults ..
    public static int MIN_ZOOM = 7;
    public static int MAX_ZOOM = 20;
    public static final int TILE_SIZE_PIXELS = 128;
    private final DisplayModel model = new DisplayModel();
    //    private final float textScale = DisplayModel.getDefaultUserScaleFactor();
    private final float textScale = 0.8f;
    private RenderThemeFuture themeFuture = null;
    private DatabaseRenderer renderer;
    private AndroidGraphicFactory factory = AndroidGraphicFactory.INSTANCE;

    private MultiMapDataStore mapDatabase;

    /**
     * The reason this constructor is protected is because all parameters,
     * except file should be determined from the archive file. Therefore a
     * factory method is necessary.
     *
     * @param file
     * @param xmlRenderTheme the themeFuture to render tiles with
     */
    protected MapsforgeTileSource(Application app,
                                  File file,
                                  XmlRenderTheme xmlRenderTheme,
                                  MultiMapDataStore.DataPolicy dataPolicy) {

        AndroidGraphicFactory.createInstance(app);

//        model.setFixedTileSize(256);

        mapDatabase = new MultiMapDataStore(dataPolicy);
        mapDatabase.addMapDataStore(new MapFile(file), false, false);

        InMemoryTileCache tileCache = new InMemoryTileCache(5);
        renderer = new DatabaseRenderer(mapDatabase, factory, tileCache,
                new TileBasedLabelStore(tileCache.getCapacityFirstLevel()), true, true, null);

        Log.d(TAG, "min=" + MIN_ZOOM + " max=" + MAX_ZOOM + " tilesize=" + TILE_SIZE_PIXELS);

        themeFuture = new RenderThemeFuture(factory, xmlRenderTheme, model);
        //super important!! without the following line, all rendering activities will block until the themeFuture is created.
        new Thread(themeFuture).start();

    }

    /**
     * Creates a new MapsforgeTileSource from file.
     * <p></p>
     * Parameters minZoom and maxZoom are obtained from the
     * database. If they cannot be obtained from the DB, the default values as
     * defined by this class are used, which is zoom = 3-20
     *
     * @param file
     * @return the tile source
     */
    public static MapsforgeTileSource createFromFiles(Application app, File file) {
        Log.d(TAG, "createFromFiles");

        ExternalRenderTheme renderTheme;

        try {
            renderTheme = new ExternalRenderTheme(Constant.DEFAULT_THEME_PATH);
            Log.d(TAG, "find themeFuture file");
            if (renderTheme.getRelativePathPrefix() != null)
                Log.d(TAG, renderTheme.getRelativePathPrefix());
        } catch (FileNotFoundException e) {
            Log.d(TAG, "load themeFuture failed");
            renderTheme = null;
        }

        return new MapsforgeTileSource(app, file, renderTheme, MultiMapDataStore.DataPolicy.RETURN_FIRST);
    }

    //The synchronized here is VERY important.  If missing, the mapDatabase read gets corrupted by multiple threads reading the file at once.
    public synchronized Bitmap renderTile(MapTile pTile) {

        Tile tile = new Tile(pTile.getX(), pTile.getY(), (byte) pTile.getZoomLevel(), 512);

        if (mapDatabase == null)
            return null;

        try {
            //Draw the tile
            RendererJob mapGeneratorJob = new RendererJob(tile, mapDatabase, themeFuture, model, textScale, false, false);
            AndroidBitmap bmp = (AndroidBitmap) renderer.executeJob(mapGeneratorJob);

            if (bmp != null)
                return AndroidGraphicFactory.getBitmap(bmp);
            else
                Log.d(TAG, "BitMap == null");

        } catch (Exception ex) {
            Log.d(TAG, "### Mapsforge tile generation failed", ex);
        }
        //Make the bad tile easy to spot
        Bitmap bmp = Bitmap.createBitmap(TILE_SIZE_PIXELS, TILE_SIZE_PIXELS, Bitmap.Config.RGB_565);
        bmp.eraseColor(Color.GRAY);
        return bmp;
    }

    public void dispose() {
        themeFuture.decrementRefCount();
        themeFuture = null;
        renderer = null;
        if (mapDatabase != null)
            mapDatabase.close();
        mapDatabase = null;
    }
}