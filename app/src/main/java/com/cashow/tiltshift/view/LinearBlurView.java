package com.cashow.tiltshift.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.cashow.tiltshift.util.BitmapUtils;
import com.cashow.tiltshift.util.Constants;
import com.cashow.tiltshift.util.Utils;


/**
 * 这个文件是线性移轴的效果图
 * 外界需要把中心点、旋转角度、移轴半径传进来，由这个自定义view生成最终的移轴效果
 */
public class LinearBlurView extends AppCompatImageView {
    // 移轴效果的bitmap
	private Bitmap shiftBitmap;
    // 移轴效果的画布
	private Canvas shiftCanvas;
    // 移轴效果的画笔
	private Paint paint;

    // 图片的大小
	private int bitmapResizedWidth;
    // 图片缩放的比例
	private double bitmapResizedRatio;

	public LinearBlurView(Activity mActivity, int bitmapResizedWidth, double bitmapResizedRatio) {
		this(mActivity, null);
		this.bitmapResizedWidth = bitmapResizedWidth;
        this.bitmapResizedRatio = bitmapResizedRatio;

        // 创建一个 bitmapResizedWidth * bitmapResizedWidth 的bitmap
		shiftBitmap = Bitmap.createBitmap(bitmapResizedWidth, bitmapResizedWidth, Config.ARGB_8888);
        // 指定shiftBitmap的画布
		shiftCanvas = new Canvas(shiftBitmap);
		paint = new Paint();
	}

	public LinearBlurView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    /**
     * 这个方法是将移轴的数据转成移轴效果图
     * @param blur_type：PREVIEW_IMAGE表示要加载高模糊度的图片，FINAL_IMAGE表示要加载低模糊度的图片
     * @param centerX：移轴的中心点x坐标
     * @param centerY：移轴的中心点y坐标
     * @param tiltRotate：移轴的旋转角度
     * @param tiltHeight：移轴区域的半径
     * @param previewBitmap：高模糊度的图片
     * @param finalBitmap：低模糊度的图片
     */
	public void setData(int blur_type, float centerX, float centerY, float tiltRotate, float tiltHeight,
			Bitmap previewBitmap, Bitmap finalBitmap) {

		float newCenterX = (float) (centerX / bitmapResizedRatio);
		float newCenterY = (float) (centerY / bitmapResizedRatio);
		float newTiltHeight = (float) (tiltHeight / bitmapResizedRatio);

		if (blur_type == Constants.PREVIEW_IMAGE) {
			drawBitmap(previewBitmap);
		} else {
			drawBitmap(finalBitmap);
		}

		int colors[] = new int[6];
		colors[0] = 0xffffffff;
		colors[1] = 0xffffffff;
		colors[2] = 0x00ffffff;
		colors[3] = 0x00ffffff;
		colors[4] = 0xffffffff;
		colors[5] = 0xffffffff;

		double tiltNewRotate = 360.0 - tiltRotate;
		while (tiltNewRotate >= 180.0) {
			tiltNewRotate -= 180.0;
		}

		float tiltTopHeight1 = Utils.clamp(newCenterY - newTiltHeight / 2, 0.0f, bitmapResizedWidth);

		float tiltTopHeight2;
		float tiltTopHeight3;
		if (blur_type == Constants.PREVIEW_IMAGE) {
			tiltTopHeight2 = Utils.clamp(newCenterY - newTiltHeight / 2 + 3, 0.0f, bitmapResizedWidth);
			tiltTopHeight3 = Utils.clamp(newCenterY + newTiltHeight / 2 - 3, 0.0f, bitmapResizedWidth);
		} else {
			tiltTopHeight2 = Utils.clamp(newCenterY - newTiltHeight / 4, 0.0f, bitmapResizedWidth);
			tiltTopHeight3 = Utils.clamp(newCenterY + newTiltHeight / 4, 0.0f, bitmapResizedWidth);
		}
		float tiltTopHeight4 = Utils.clamp(newCenterY + newTiltHeight / 2, 0.0f, bitmapResizedWidth);

		float positions[] = new float[6];
		positions[0] = 0.0f;
		positions[1] = tiltTopHeight1 / bitmapResizedWidth;
		positions[2] = tiltTopHeight2 / bitmapResizedWidth;
		positions[3] = tiltTopHeight3 / bitmapResizedWidth;
		positions[4] = tiltTopHeight4 / bitmapResizedWidth;
		positions[5] = 1.0f;

		LinearGradient shader = new LinearGradient(0, 0, 0, bitmapResizedWidth, colors, positions, TileMode.CLAMP);

		paint.setShader(shader);
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		shiftCanvas.save();
		shiftCanvas.rotate(tiltRotate, newCenterX, newCenterY);
		shiftCanvas.drawRect(-bitmapResizedWidth, -bitmapResizedWidth, 2 * bitmapResizedWidth, 2 * bitmapResizedWidth,
				paint);
		shiftCanvas.restore();

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

	public Bitmap getLinearShiftBitmap(){
		return shiftBitmap;
	}
}
