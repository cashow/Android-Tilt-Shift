package com.cashow.tiltshift.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.cashow.tiltshift.util.BitmapUtils;
import com.cashow.tiltshift.util.Constants;


public class RoundBlurView extends AppCompatImageView {

	private int bitmapResizedWidth;
	private double bitmapResizedRatio;
	private Bitmap shiftBitmap;
	private Canvas shiftCanvas;
	private Paint paint;

	public RoundBlurView(Activity mActivity, int bitmapResizedWidth, double bitmapResizedRatio) {
		this(mActivity, null);

        this.bitmapResizedWidth = bitmapResizedWidth;
        this.bitmapResizedRatio = bitmapResizedRatio;

		shiftBitmap = Bitmap.createBitmap(bitmapResizedWidth, bitmapResizedWidth, Config.ARGB_8888);
		shiftCanvas = new Canvas(shiftBitmap);
		paint = new Paint();
	}

	public RoundBlurView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setData(int blur_type, float tiltX, float tiltY, float tiltRadius, Bitmap previewBitmap, Bitmap finalBitmap) {
		float tiltGradientRadius = tiltRadius / 2.0f;

		if (blur_type == Constants.PREVIEW_IMAGE) {
			drawBitmap(previewBitmap);
		} else {
			drawBitmap(finalBitmap);
		}

		int colors[] = new int[4];
		colors[0] = 0x00ffffff;
		colors[1] = 0x00ffffff;
		colors[2] = 0xffffffff;
		colors[3] = 0xffffffff;

		float tiltRadius1;
		if (blur_type == Constants.PREVIEW_IMAGE) {
			tiltRadius1 = tiltRadius - 3;
		} else {
			tiltRadius1 = tiltRadius - tiltGradientRadius;
		}
		tiltRadius1 = Math.min(tiltRadius1, bitmapResizedWidth);
		tiltRadius1 = Math.max(tiltRadius1, 0.0f);
		float tiltRadius2 = tiltRadius;
		tiltRadius2 = Math.min(tiltRadius2, bitmapResizedWidth);
		tiltRadius2 = Math.max(tiltRadius2, 0.0f);

		float positions[] = new float[4];
		positions[0] = 0.0f;
		positions[1] = tiltRadius1 / tiltRadius;
		positions[2] = tiltRadius2 / tiltRadius;
		positions[3] = 1.0f;

		float shaderTiltX = (float) (tiltX / bitmapResizedRatio);
		float shaderTiltY = (float) (tiltY / bitmapResizedRatio);
		float shaderTiltRadius = (float) (tiltRadius / bitmapResizedRatio);

		RadialGradient shader = new RadialGradient(shaderTiltX, shaderTiltY, shaderTiltRadius, colors, positions, TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		shiftCanvas.drawRect(0, 0, bitmapResizedWidth, bitmapResizedWidth, paint);

		setImageBitmap(shiftBitmap);
    }

	private void drawBitmap(Bitmap bitmap) {
		int bitmapWidth;
		int bitmapHeight;
		if (bitmap.getWidth() > bitmap.getHeight()) {
			bitmapWidth = bitmapResizedWidth;
			bitmapHeight = bitmapResizedWidth * bitmap.getHeight() / bitmap.getWidth();
		} else {
			bitmapWidth = bitmapResizedWidth * bitmap.getWidth() / bitmap.getHeight();
			bitmapHeight = bitmapResizedWidth;
		}

		shiftCanvas.drawBitmap(BitmapUtils.getResizedBitmap(bitmap, bitmapWidth, bitmapHeight),
				(bitmapResizedWidth - bitmapWidth) / 2, (bitmapResizedWidth - bitmapHeight) / 2, null);
	}

	public Bitmap getRoundShiftBitmap(){
		return shiftBitmap;
	}
}
