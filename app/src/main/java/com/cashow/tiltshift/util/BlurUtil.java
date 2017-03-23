package com.cashow.tiltshift.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.cashow.tiltshift.R;
import com.cashow.tiltshift.view.LineView;
import com.cashow.tiltshift.view.LinearBlurView;
import com.cashow.tiltshift.view.RoundBlurView;
import com.cashow.tiltshift.view.RoundView;

/**
 * 这个文件用来控制线性移轴和径向移轴的切换，并负责将触摸事件从activity传到对应的 LinearBlurUtil 或者 RoundBlurUtil
 */
public class BlurUtil {
    private Activity mActivity;
    private Context mContext;

    // 移轴效果的ImageView和辅助线的ImageView都放在layout_blur控件里
    private RelativeLayout layout_blur;

    // 径向移轴的辅助线
    private RoundView roundview;
    // 径向移轴的效果图
    private RoundBlurView roundBlurview;

    // 线性移轴的辅助线
    private LineView lineview;
    // 线性移轴的效果图
    private LinearBlurView linearBlurview;

    // 控制线性移轴逻辑的util
    private LinearBlurUtil linearBlurUtil;
    // 控制径向移轴逻辑的util
    private RoundBlurUtil roundBlurUtil;

    // 在设置移轴效果时显示的高模糊度图片
    private Bitmap previewBitmap;
    // 移轴效果设置好后显示的低模糊度图片
    private Bitmap finalBitmap;

    // 屏幕宽度
    private int screenWidth;
    // 图片宽度
    private int bitmapWidth;
    // 屏幕宽度 与 图片宽度 的比率
    private double bitmapResizedRatio;

    // 现在的移轴状态：BLUR_NONE、BLUR_LINEAR 或者 BLUR_ROUND
    private int blurType;

    // 移轴的3个状态
    // BLUR_NONE: 隐藏移轴效果
    // BLUR_LINEAR: 线性移轴
    // BLUR_ROUND：径向移轴
    public static final int BLUR_NONE = 0;
    public static final int BLUR_LINEAR = 1;
    public static final int BLUR_ROUND = 2;


    public BlurUtil(Activity mActivity, Bitmap finalBitmap, Bitmap previewBitmap) {
        this.mActivity = mActivity;
        this.finalBitmap = finalBitmap;
        this.previewBitmap = previewBitmap;
        init();
    }

    private void init() {
        mContext = mActivity.getApplicationContext();

        layout_blur = (RelativeLayout) mActivity.findViewById(R.id.layout_blur);

        // 初始化变量
        initData();
        // 初始化layout_blur
        initLayoutBlur();
        // 初始化linearBlurUtil和roundBlurUtil
        initBlurUtil();
    }

    private void initData() {
        roundBlurUtil = new RoundBlurUtil(mActivity);
        linearBlurUtil = new LinearBlurUtil(mActivity);
        blurType = BLUR_NONE;
        screenWidth = Utils.getScreenWidth(mContext);
        bitmapWidth = (int) Constants.MAX_WIDTH;
        bitmapResizedRatio = Utils.getScreenWidth(mContext) / (bitmapWidth + 0.0);
    }

    public void initLayoutBlur() {
        // 添加线性移轴效果图
        addLinearBlurView();
        // 添加线性移轴的辅助线
        addLineView();
        // 添加径向移轴效果图
        addRoundBlurView();
        // 添加径向移轴的辅助线
        addRoundView();
    }

    // 添加线性移轴效果图
    private void addLinearBlurView() {
        linearBlurview = new LinearBlurView(mActivity, bitmapWidth, bitmapResizedRatio);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, screenWidth);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        linearBlurview.setLayoutParams(params);
        linearBlurview.setData(Constants.FINAL_IMAGE, screenWidth / 2, screenWidth / 2, 0, screenWidth * 0.1875f,
                previewBitmap, finalBitmap);
        linearBlurview.setVisibility(View.GONE);
        layout_blur.addView(linearBlurview);
    }

    // 添加线性移轴的辅助线
    private void addLineView() {
        lineview = new LineView(mActivity, bitmapWidth, bitmapResizedRatio);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, screenWidth);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lineview.setLayoutParams(params);
        lineview.drawLine(screenWidth / 2, screenWidth / 2, 0, screenWidth * 0.1875f);
        lineview.setVisibility(View.GONE);
        layout_blur.addView(lineview);
    }

    // 添加径向移轴效果图
    private void addRoundBlurView() {
        roundBlurview = new RoundBlurView(mActivity, bitmapWidth, bitmapResizedRatio);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, screenWidth);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        roundBlurview.setLayoutParams(params);
        roundBlurview.setData(Constants.FINAL_IMAGE, screenWidth / 2, screenWidth / 2, screenWidth * 0.1875f,
                previewBitmap, finalBitmap);
        roundBlurview.setVisibility(View.GONE);
        layout_blur.addView(roundBlurview);
    }

    // 添加径向移轴的辅助线
    private void addRoundView() {
        roundview = new RoundView(mActivity, bitmapWidth, bitmapResizedRatio);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, screenWidth);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        roundview.setLayoutParams(params);
        roundview.drawRound(screenWidth / 2, screenWidth / 2, screenWidth * 0.1875f);
        roundview.setVisibility(View.GONE);
        layout_blur.addView(roundview);
    }

    // 处理移轴状态的切换
    public void setBlurType(int blurType) {
        this.blurType = blurType;
        switch (blurType) {
            case BLUR_NONE:
                // 切换成"关闭"状态时，隐藏移轴效果
                if (roundBlurview != null && linearBlurview != null) {
                    roundBlurview.setVisibility(View.GONE);
                    linearBlurview.setVisibility(View.GONE);
                }
                break;
            case BLUR_LINEAR:
                // 切换成"线性移轴"状态时，隐藏径向移轴并加载显示移轴效果的动画
                if(roundBlurview != null && linearBlurUtil != null) {
                    roundBlurview.setVisibility(View.GONE);
                    linearBlurUtil.showLinearView();
                }
                break;
            case BLUR_ROUND:
                // 切换成"径向移轴"状态时，隐藏线性移轴并加载显示移轴效果的动画
                if(linearBlurview != null && roundBlurUtil != null) {
                    linearBlurview.setVisibility(View.GONE);
                    roundBlurUtil.showRoundView();
                }
                break;
        }
    }

    // 处理触摸事件
    public boolean handleTouchEvent(MotionEvent event) {
        if (blurType == BLUR_LINEAR) {
            // 线性移轴状态下处理触摸事件，提交给linearBlurUtil
            return linearBlurUtil.handleBlurLinearEvent(event);
        } else if (blurType == BLUR_ROUND) {
            // 径向移轴状态下处理触摸事件，提交给roundBlurUtil
            return roundBlurUtil.handleBlurRoundEvent(event);
        }
        return true;
    }

    // 初始化linearBlurUtil和roundBlurUtil
    private void initBlurUtil() {
        linearBlurUtil.init(linearBlurview, lineview, finalBitmap, previewBitmap);
        roundBlurUtil.init(roundBlurview, roundview, finalBitmap, previewBitmap);
    }

    public Bitmap getLinearShiftBitmap(){
        return linearBlurview.getLinearShiftBitmap();
    }

    public Bitmap getRoundShiftBitmap(){
        return roundBlurview.getRoundShiftBitmap();
    }
}
