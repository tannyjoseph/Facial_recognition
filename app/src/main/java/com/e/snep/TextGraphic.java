package com.e.snep;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


import com.e.snep.Camera.GraphicOverlay;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class TextGraphic extends GraphicOverlay.Graphic {

    TextGraphic(GraphicOverlay overlay) {
        super(overlay);
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateText(int c) {
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        DateFormat df = new SimpleDateFormat("HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        Bitmap cc = drawTextBitmap(date, Color.WHITE,100,150,false,500,500);
        canvas.drawBitmap(cc, 0, 10, new Paint());
    }

    public static Bitmap drawTextBitmap(String string, int color, int alpha, int size, boolean underline, int width , int height) {
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        //canvas.drawBitmap();
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(string, 100, 150, paint);
        return result;
    }
}
