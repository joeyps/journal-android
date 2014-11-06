package joey.thosedays;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by joey on 14/11/5.
 */
public class CoverBitmapDrawable extends BitmapDrawable {
    private Matrix mMatrix = new Matrix();
    private int moldHeight;
    boolean simpleMapping = false;

    public CoverBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (bounds.height() > moldHeight) {
            moldHeight = bounds.height();
            Bitmap b = getBitmap();
            RectF src = new RectF(0, 0, b.getWidth(), b.getHeight());
            RectF dst;

            if (simpleMapping) {
                dst = new RectF(bounds);
                mMatrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
            } else {
                // Full Screen Image -> Always scale and center-crop in order to fill the screen
                float dwidth = src.width();
                float dheight = src.height();

                float vwidth = bounds.width();
                float vheight = bounds.height();

                float scale;
                float dx = 0, dy = 0;

                if (dwidth * vheight > vwidth * dheight) {
                    scale = (float) vheight / (float) dheight;
                    dx = (vwidth - dwidth * scale) * 0.5f;
                } else {
                    scale = (float) vwidth / (float) dwidth;
                    dy = (vheight - dheight * scale) * 0.5f;
                }

                mMatrix.setScale(scale, scale);
                mMatrix.postTranslate(dx, dy);

            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(0xaa00ff00);
        canvas.drawBitmap(getBitmap(), mMatrix, null);
    }
}
