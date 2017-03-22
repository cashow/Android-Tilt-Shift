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


/**
 * 这个文件是径向移轴的效果图
 * 外界需要把中心点、移轴半径传进来，由这个自定义view生成最终的移轴效果
 */
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

	/**
	 * 这个方法是将移轴的数据转成移轴效果图
	 * @param blur_type：PREVIEW_IMAGE表示要加载高模糊度的图片，FINAL_IMAGE表示要加载低模糊度的图片
	 * @param tiltX：移轴的中心点x坐标
	 * @param tiltY：移轴的中心点y坐标
	 * @param tiltRadius：移轴区域的半径
	 * @param previewBitmap：高模糊度的图片
	 * @param finalBitmap：低模糊度的图片
	 */
	public void setData(int blur_type, float tiltX, float tiltY, float tiltRadius, Bitmap previewBitmap, Bitmap finalBitmap) {
		float tiltGradientRadius = tiltRadius / 2.0f;

		if (blur_type == Constants.PREVIEW_IMAGE) {
            // 绘制高模糊度的图片
			drawBitmap(previewBitmap);
		} else {
            // 绘制低模糊度的图片
			drawBitmap(finalBitmap);
		}

        // 现在需要绘制一个带有渐变透明度的shader
        // 总共有4个区域
        // 0到1的区域是完全不透明
        // 1到2的区域是从不透明渐变到透明
        // 2到3的区域是完全透明
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

        // 将屏幕上的坐标转成bitmap的坐标
		float shaderTiltX = (float) (tiltX / bitmapResizedRatio);
		float shaderTiltY = (float) (tiltY / bitmapResizedRatio);
        // 将屏幕上的移轴半径改成bitmap的移轴半径
		float shaderTiltRadius = (float) (tiltRadius / bitmapResizedRatio);

		RadialGradient shader = new RadialGradient(shaderTiltX, shaderTiltY, shaderTiltRadius, colors, positions, TileMode.CLAMP);
		paint.setShader(shader);
        // Mode.DST_IN是绘制2层图片的交集，在这里是将模糊图片与带有渐变透明度的shader取交集进行绘制。
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
