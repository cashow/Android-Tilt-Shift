package com.cashow.tiltshift.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.cashow.tiltshift.R;
import com.cashow.tiltshift.util.BitmapUtils;
import com.cashow.tiltshift.util.Utils;


public class RoundView extends AppCompatImageView {
	/**
	 * 径向移轴的辅助线，包含有2个bitmap: 中心辅助定位的bitmap和外围的圆形边界
	 */

    // 辅助线中心辅助定位的bitmap
	private Bitmap centerBitmap;
    // 辅助线外围的圆形边界
	private Bitmap roundBitmap;
    // 辅助线的画布
	private Canvas roundCanvas;

    // 用来清空画布的画笔
	private Paint clearPaint;
    // 辅助线的画笔
	private Paint roundPaint;
    
    private Context mContext;
    // 高斯模糊的bitmap的缩放大小
	private double bitmapResizedRatio;

	public RoundView(Activity mActivity, int bitmapResizedWidth, double bitmapResizedRatio) {
		this(mActivity, null);
		this.mContext = mActivity.getApplicationContext();
        this.bitmapResizedRatio = bitmapResizedRatio;
		
		centerBitmap = getCenterBitmap();

		// 创建一个大小是 bitmapResizedWidth * bitmapResizedWidth 的bitmap
		roundBitmap = Bitmap.createBitmap(bitmapResizedWidth, bitmapResizedWidth, Config.ARGB_8888);
        // 指定bitmap的画布
		roundCanvas = new Canvas(roundBitmap);

        // 初始化paint
        initPaint();
	}

    private Bitmap getCenterBitmap() {
        // 获取辅助线中心用来辅助定位的bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.photo_blur_round_center);
        int centerBitmapWidth = (int) (Utils.dp2px(mContext, 25) / bitmapResizedRatio);
        bitmap = BitmapUtils.getResizedBitmap(bitmap, centerBitmapWidth, centerBitmapWidth);
        return bitmap;
    }

    private void initPaint() {
        // 初始化清空画布的画笔
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

        // 初始化辅助线的画笔
        roundPaint = new Paint();
        // 抗锯齿
        roundPaint.setAntiAlias(true);
        // 绘画时只画边界
        roundPaint.setStyle(Paint.Style.STROKE);
        // 设置画笔的颜色
        roundPaint.setARGB(178, 255, 255, 255);
        // 设置画笔的宽度
        roundPaint.setStrokeWidth(3);
    }

	public RoundView(Context mContext, AttributeSet attrs) {
		super(mContext, attrs);
		this.mContext = mContext;
	}

	public void drawRound(float tiltX, float tiltY, float tiltRadius) {
        /**
         * 绘制辅助线。辅助线位置或宽度有变化时需要调用进行重新绘制。
         */
		roundCanvas.drawPaint(clearPaint);

        // 新的中心点坐标
		float newTiltX = (float) (tiltX / bitmapResizedRatio);
		float newTiltY = (float) (tiltY / bitmapResizedRatio);
        // 新的移轴区域半径
		float newTiltRadius = (float) (tiltRadius / bitmapResizedRatio);

        // 绘制辅助线
		roundCanvas.drawCircle(newTiltX, newTiltY, newTiltRadius, roundPaint);
		roundCanvas.drawBitmap(centerBitmap, newTiltX - centerBitmap.getWidth() / 2, newTiltY - centerBitmap.getHeight() / 2, null);
		setImageBitmap(roundBitmap);
    }
}
