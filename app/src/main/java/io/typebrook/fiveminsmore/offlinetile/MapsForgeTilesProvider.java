package io.typebrook.fiveminsmore.offlinetile;

import android.app.Application;
import android.graphics.Bitmap;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import org.osmdroid.tileprovider.MapTile;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by pham on 2017/5/3.
 */

public class MapsForgeTilesProvider implements TileProvider {
    private static final String TAG = "MapsForgeTilesProvider";

    private MapsforgeTileSource source;

    public MapsForgeTilesProvider(Application app, File file) {
        source = MapsforgeTileSource.createFromFiles(app, file);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {

        MapTile tile = new MapTile(zoom, x, y);
        Bitmap bitmap = source.renderTile(tile);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();

        return new Tile(512, 512, bitmapdata);
    }
}
