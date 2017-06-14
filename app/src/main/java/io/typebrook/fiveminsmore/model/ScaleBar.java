package io.typebrook.fiveminsmore.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.location.Location;
import android.support.v7.widget.AppCompatImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

/**
 * Created by pham on 2017/6/14.
 * Copied from https://stackoverflow.com/questions/17852773/how-to-show-the-scale-bar-and-zom-controls-on-the-map
 */

public class ScaleBar extends AppCompatImageView {
    float mXOffset = 10;
    float mYOffset = 10;
    float mLineWidth = 7;
    int mTextSize = 60;

    boolean mIsLatitudeBar = true;
    boolean mIsLongitudeBar = true;

    private GoogleMap mMap;

    float mXdpi;
    float mYdpi;

    public ScaleBar(Context context, GoogleMap map) {
        super(context);

        mMap = map;

        mXdpi = context.getResources().getDisplayMetrics().xdpi;
        mYdpi = context.getResources().getDisplayMetrics().ydpi;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();

        drawScaleBarPicture(canvas);

        canvas.restore();
    }

    private void drawScaleBarPicture(Canvas canvas) {
        // We want the scale bar to be as long as the closest round-number miles/kilometers
        // to 1-inch at the latitude at the current center of the screen.

        final Paint barPaint = new Paint();
        barPaint.setColor(Color.BLACK);
        barPaint.setAntiAlias(true);
        barPaint.setStrokeWidth(mLineWidth);

        final Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setShadowLayer(5, 0, 0, Color.WHITE);
        textPaint.setShader(new Shader());
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(mTextSize);

        drawXMetric(canvas, textPaint, barPaint);
    }

    private void drawXMetric(Canvas canvas, Paint textPaint, Paint barPaint) {
        Projection projection = mMap.getProjection();

        if (projection != null) {
            LatLng p1 = projection.fromScreenLocation(new Point((int) ((getWidth() / 2) - (mXdpi / 2)), getHeight() / 2));
            LatLng p2 = projection.fromScreenLocation(new Point((int) ((getWidth() / 2) + (mXdpi / 2)), getHeight() / 2));

            Location locationP1 = new Location("ScaleBar location p1");
            Location locationP2 = new Location("ScaleBar location p2");

            locationP1.setLatitude(p1.latitude);
            locationP2.setLatitude(p2.latitude);
            locationP1.setLongitude(p1.longitude);
            locationP2.setLongitude(p2.longitude);

            float xMetersPerInch = locationP1.distanceTo(locationP2);

            if (mIsLatitudeBar) {
                String xMsg = scaleBarLengthText(xMetersPerInch);
                Rect xTextRect = new Rect();
                textPaint.getTextBounds(xMsg, 0, xMsg.length(), xTextRect);

                int textSpacing = (int) (xTextRect.height() / 5.0);

                canvas.drawRect(mXOffset, mYOffset, mXOffset + mXdpi, mYOffset + mLineWidth, barPaint);
                canvas.drawRect(mXOffset + mXdpi, mYOffset, mXOffset + mXdpi + mLineWidth, mYOffset +
                        xTextRect.height() + mLineWidth + textSpacing, barPaint);

                if (!mIsLongitudeBar) {
                    canvas.drawRect(mXOffset, mYOffset, mXOffset + mLineWidth, mYOffset +
                            xTextRect.height() + mLineWidth + textSpacing, barPaint);
                }
                canvas.drawText(xMsg, (mXOffset + mXdpi / 2 - xTextRect.width() / 2),
                        (mYOffset + xTextRect.height() + mLineWidth + textSpacing), textPaint);
            }
        }
    }

    private void drawYMetric(Canvas canvas, Paint textPaint, Paint barPaint) {
        Projection projection = mMap.getProjection();

        if (projection != null) {
            Location locationP1 = new Location("ScaleBar location p1");
            Location locationP2 = new Location("ScaleBar location p2");

            LatLng p1 = projection.fromScreenLocation(new Point(getWidth() / 2,
                    (int) ((getHeight() / 2) - (mYdpi / 2))));
            LatLng p2 = projection.fromScreenLocation(new Point(getWidth() / 2,
                    (int) ((getHeight() / 2) + (mYdpi / 2))));

            locationP1.setLatitude(p1.latitude);
            locationP2.setLatitude(p2.latitude);
            locationP1.setLongitude(p1.longitude);
            locationP2.setLongitude(p2.longitude);

            float yMetersPerInch = locationP1.distanceTo(locationP2);

            if (mIsLongitudeBar) {
                String yMsg = scaleBarLengthText(yMetersPerInch);
                Rect yTextRect = new Rect();
                textPaint.getTextBounds(yMsg, 0, yMsg.length(), yTextRect);

                int textSpacing = (int) (yTextRect.height() / 5.0);

                canvas.drawRect(mXOffset, mYOffset, mXOffset + mLineWidth, mYOffset + mYdpi, barPaint);
                canvas.drawRect(mXOffset, mYOffset + mYdpi, mXOffset + yTextRect.height() +
                        mLineWidth + textSpacing, mYOffset + mYdpi + mLineWidth, barPaint);

                if (!mIsLatitudeBar) {
                    canvas.drawRect(mXOffset, mYOffset, mXOffset + yTextRect.height() +
                            mLineWidth + textSpacing, mYOffset + mLineWidth, barPaint);
                }

                float x = mXOffset + yTextRect.height() + mLineWidth + textSpacing;
                float y = mYOffset + mYdpi / 2 + yTextRect.width() / 2;

                canvas.rotate(-90, x, y);
                canvas.drawText(yMsg, x, y + textSpacing, textPaint);
            }
        }
    }

    private String scaleBarLengthText(float meters) {
        if (meters >= 1000) {
            String kmValue = String.format(Locale.getDefault(), "%.2f", meters / 1000);
            return kmValue + "km";
        } else {
            String mValue = String.format(Locale.getDefault(), "%.0f", meters);
            return mValue + "m";
        }
    }
}