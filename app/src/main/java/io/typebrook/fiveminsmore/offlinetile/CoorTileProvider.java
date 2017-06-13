package io.typebrook.fiveminsmore.offlinetile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;

/**
 * Created by pham on 2017/6/6.
 */

public class CoorTileProvider implements TileProvider {

    private static final int TILE_SIZE_DP = 256;

    private final float mScaleFactor;

    private final Bitmap mBorderTile;

    public CoorTileProvider(Context context) {
            /* Scale factor based on density, with a 0.8 multiplier to increase tile generation
             * speed */
        mScaleFactor = context.getResources().getDisplayMetrics().density * 0.8f;
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStrokeWidth(5f);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        mBorderTile = Bitmap.createBitmap((int) (TILE_SIZE_DP * mScaleFactor),
                (int) (TILE_SIZE_DP * mScaleFactor), android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBorderTile);
        canvas.drawRect(0, 0, TILE_SIZE_DP * mScaleFactor, TILE_SIZE_DP * mScaleFactor,
                borderPaint);
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        Bitmap coorTile = drawTileCoors(x, y, zoom);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        coorTile.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] bitmapData = stream.toByteArray();
        return new Tile((int) (TILE_SIZE_DP * mScaleFactor),
                (int) (TILE_SIZE_DP * mScaleFactor), bitmapData);
    }

    private Bitmap drawTileCoors(int x, int y, int zoom) {
        // Synchronize copying the bitmap to avoid a race condition in some devices.
        Bitmap copy = null;
        synchronized (mBorderTile) {
            copy = mBorderTile.copy(android.graphics.Bitmap.Config.ARGB_8888, true);
        }
        Canvas canvas = new Canvas(copy);
        String tileCoords = "(" + x + ", " + y + ")";
        String zoomLevel = "zoom = " + zoom;
            /* Paint is not thread safe. */
        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(18 * mScaleFactor);
        mTextPaint.setColor(Color.WHITE);

        canvas.drawText(tileCoords, TILE_SIZE_DP * mScaleFactor / 2,
                TILE_SIZE_DP * mScaleFactor / 2, mTextPaint);
        canvas.drawText(zoomLevel, TILE_SIZE_DP * mScaleFactor / 2,
                TILE_SIZE_DP * mScaleFactor * 2 / 3, mTextPaint);
        return copy;
    }
}
