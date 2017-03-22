package com.cashow.tiltshift.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.cashow.tiltshift.util.Utils;

public class LineView extends AppCompatImageView {
	/**
	 * 进行线性移轴时的辅助线
	 */

    // 辅助线的bitmap
	private Bitmap lineBitmap;
    // 辅助线的画布
	private Canvas lineCanvas;

    // 用来清空画布的画笔
	private Paint clearPaint;
    // 辅助线的画笔
	private Paint linePaint;

    // 高斯模糊的bitmap的宽度
	private int bitmapResizedWidth;
    // 高斯模糊的bitmap的缩放大小
	private double bitmapResizedRatio;

	public LineView(Activity mActivity, int bitmapResizedWidth, double bitmapResizedRatio) {
		this(mActivity, null);

        this.bitmapResizedWidth = bitmapResizedWidth;
        this.bitmapResizedRatio = bitmapResizedRatio;

        // 创建一个大小是 bitmapResizedWidth * bitmapResizedWidth 的bitmap
		lineBitmap = Bitmap.createBitmap(bitmapResizedWidth, bitmapResizedWidth, Config.ARGB_8888);
        // 指定bitmap的画布
		lineCanvas = new Canvas(lineBitmap);

        // 初始化paint
        initPaint();
	}

    private void initPaint() {
        // 初始化清空画布的画笔
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

        // 初始化辅助线的画笔
        linePaint = new Paint();
        // 设置画笔的颜色
        linePaint.setARGB(178, 255, 255, 255);
        // 设置画笔的宽度
        linePaint.setStrokeWidth(3);
        // 抗锯齿
        linePaint.setAntiAlias(true);
    }

	public LineView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void drawLine(float centerX, float centerY, float tiltRotate, float tiltHeight) {
        /**
         * 绘制辅助线。辅助线位置或宽度有变化时需要调用进行重新绘制。
         */
        // 清空画布
		lineCanvas.drawPaint(clearPaint);

        // 新的中心点坐标
		float newCenterX = (float) (centerX / bitmapResizedRatio);
		float newCenterY = (float) (centerY / bitmapResizedRatio);
        // 新的移轴区域宽度
		float newTiltHeight = (float) (tiltHeight / bitmapResizedRatio);

        // 辅助线的上边界的位置，计算方法是y轴坐标减去移轴区域宽度的一半
		float tiltTopHeightUp = Utils.clamp(newCenterY - newTiltHeight / 2, 0.0f, bitmapResizedWidth);
        // 辅助线的下边界的位置，计算方法是y轴坐标加上移轴区域宽度的一半
		float tiltTopHeightDown = Utils.clamp(newCenterY + newTiltHeight / 2, 0.0f, bitmapResizedWidth);

		lineCanvas.save();
		lineCanvas.rotate(tiltRotate, newCenterX, newCenterY);
		lineCanvas.drawLine(-bitmapResizedWidth, tiltTopHeightUp, 2 * bitmapResizedWidth, tiltTopHeightUp, linePaint);
		lineCanvas.drawLine(-bitmapResizedWidth, tiltTopHeightDown, 2 * bitmapResizedWidth, tiltTopHeightDown, linePaint);
		lineCanvas.restore();

		setImageBitmap(lineBitmap);
    }
}
