package gpsplus.rtkgps.routing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

public class StatusOverlay extends Overlay {
    @Override
    public void draw(Canvas pCanvas, MapView pMapView, boolean pShadow) {
        if (!isEnabled()) return;
        if (pShadow) {
            //draw a shadow if needed, otherwise return
            return;
        }
        Paint lp3;
        lp3 = new Paint();
        lp3.setColor(Color.RED);
        lp3.setAntiAlias(true);
        lp3.setStyle(Paint.Style.STROKE);
        lp3.setStrokeWidth(1);
        lp3.setTextAlign(Paint.Align.LEFT);
        lp3.setTextSize(12);
        // Calculate the half-world size
        final Rect viewportRect = new Rect();
        final Projection projection = pMapView.getProjection();

        // Save the Mercator coordinates of what is on the screen
        viewportRect.set(projection.getScreenRect());

        // Draw a line from one corner to the other
        pCanvas.drawLine(viewportRect.left, viewportRect.top,
                viewportRect.right, viewportRect.bottom, lp3);
    }
}
