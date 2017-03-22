package com.cashow.tiltshift.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cashow.tiltshift.R;
import com.cashow.tiltshift.data.Point;
import com.cashow.tiltshift.view.LineView;
import com.cashow.tiltshift.view.LinearBlurView;


/**
 * 这个util是用来记录和控制线性移轴相关的数据及业务逻辑，例如移轴的半径、宽度之类的
 * 用来显示移轴效果的自定义view放在linearBlurview里，用来显示移轴辅助线的自定义view放在lineview里
 * 外界并不会直接对linearBlurview和lineview进行操作，所有操作都需要通过调用LinearBlurUtil的接口实现
 * 这个util相当于MVP模式中的model层
 */
public class LinearBlurUtil {
    // 双指缩放时要处理的坐标：
    // 第一个触摸点上一次触摸时的坐标
    private Point pre0 = new Point();
    // 第一个触摸点现在的坐标
    private Point current0 = new Point();
    // 第二个触摸点上一次触摸时的坐标
    private Point pre1 = new Point();
    // 第二个触摸点现在的坐标
    private Point current1 = new Point();
    // 上一次触摸时双指之间的距离
    private double lastFingerDis;

    // 拖动移轴区域时要处理的坐标：
    // 上一次触摸时的坐标
    private Point pre = new Point();
    // 触摸点现在的坐标
    private Point current = new Point();

    // 移轴效果的中心坐标
    private Point tilt = new Point();

    // 移轴效果的半径
    private float tiltHeight;
    // 移轴效果的旋转角度
    private float tiltRotate;

    // 屏幕宽度
    private int screenWidth;

    private boolean isMoved;
    private long lastClickTime;

    // 进行移轴操作时预览用的高模糊度bitmap
    private Bitmap previewBitmap;
    // 移轴效果操作完毕后显示的低模糊度bitmap
    private Bitmap finalBitmap;

    private Animation animation_alpha_in;
    private Animation animation_alpha_init_in;
    private Animation animation_alpha_out;

    // 用来显示移轴效果的自定义view
    private LinearBlurView linearBlurview;
    // 用来显示移轴辅助线的自定义view
    private LineView lineview;

    private Activity mActivity;
    private Context context;

    // 目前的动画类型，有3种动画类型：
    // ANIM_INIT：从其他移轴效果切换到线性移轴效果时要显示的动画，主要是显示移轴预览效果以及辅助线
    // ANIM_INIT_FINISHED：在ANIM_INIT动画结束后会立即调用ANIM_INIT_FINISHED动画，主要是隐藏移轴预览效果以及辅助线
    // ANIM_TOUCH：用户开始拖动和缩放移轴区域时要显示的动画，在动画开始前要显示预览效果及辅助线，动画结束后隐藏预览效果及辅助线
    private int animationType;

    private static final int ANIM_INIT = 1;
    private static final int ANIM_TOUCH = 2;
    private static final int ANIM_INIT_FINISHED = 3;

    public LinearBlurUtil(Activity mActivity) {
        this.mActivity = mActivity;
        this.context = mActivity.getApplicationContext();

        // 获取屏幕宽度
        screenWidth = Utils.getScreenWidth(context);

        // 移轴效果初始的位置在屏幕最中间
        tilt.setPoint(screenWidth / 2, screenWidth / 2);
        Log.d("wy", "x, y = " + screenWidth/2 + " " + screenWidth / 2);
        // 初始的移轴效果旋转角度是 0
        tiltRotate = 0.0f;
        // 将初始的移轴效果半径设置成屏幕宽度的 3/8
        tiltHeight = screenWidth * 0.375f;
    }

    // 初始化LinearBlurUtil
    public void init(LinearBlurView linearBlurview, final LineView lineview, Bitmap finalBitmap, Bitmap previewBitmap) {
        this.linearBlurview = linearBlurview;
        this.lineview = lineview;
        this.finalBitmap = finalBitmap;
        this.previewBitmap = previewBitmap;

        initAnimation();
    }

    // 初始化移轴效果切换时的动画
    private void initAnimation() {
        animation_alpha_in = AnimationUtils.loadAnimation(context, R.anim.alpha_in);
        animation_alpha_out = AnimationUtils.loadAnimation(context, R.anim.photo_alpha_out);
        animation_alpha_init_in = AnimationUtils.loadAnimation(context, R.anim.photo_alpha_in);

        setAnimationListener(animation_alpha_init_in);
        setAnimationListener(animation_alpha_in);
        setAnimationListener(animation_alpha_out);
    }

    // 设置动画的属性和回调
    private void setAnimationListener(Animation animation) {
        animation.setFillAfter(true);
        animation.setFillEnabled(true);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                animationStart();
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                animationEnd();
            }
        });
    }

    private void animationStart() {
        switch (animationType) {
            case ANIM_INIT:
                setLinearBlurView(Constants.PREVIEW_IMAGE);
                lineview.setVisibility(View.VISIBLE);
                break;
            case ANIM_TOUCH:
                lineview.setVisibility(View.VISIBLE);
                break;
            case ANIM_INIT_FINISHED:
                setLinearBlurView(Constants.PREVIEW_IMAGE);
                break;
            default:
                break;
        }
    }

    private void animationEnd() {
        switch (animationType) {
            case ANIM_INIT:
                startAnimation(lineview, animation_alpha_out, ANIM_INIT_FINISHED);
                break;
            case ANIM_TOUCH:
                setLinearBlurView(Constants.FINAL_IMAGE);
                lineview.setVisibility(View.GONE);
                break;
            case ANIM_INIT_FINISHED:
                setLinearBlurView(Constants.FINAL_IMAGE);
                lineview.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    // 显示线性移轴效果
    public void showLinearView() {
        linearBlurview.setVisibility(View.VISIBLE);
        startAnimation(lineview, animation_alpha_init_in, ANIM_INIT);
    }

    // 开启动画
    private void startAnimation(View view, Animation animation, int anim_type) {
        animationType = anim_type;
        view.startAnimation(animation);
    }

    // 处理触摸事件
    public boolean handleBlurLinearEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                pre0.x = event.getX(0);
                pre0.y = event.getY(0);

                if (event.getPointerCount() >= 2) {
                    pre1.x = event.getX(1);
                    pre1.y = event.getY(1);

                    lastFingerDis = Utils.distanceBetweenFingers(event);
                }
                break;
            case MotionEvent.ACTION_DOWN:
                pre.x = event.getRawX();
                pre.y = event.getRawY();

                pre0.x = event.getX(0);
                pre0.y = event.getY(0);

                if (event.getPointerCount() >= 2) {
                    pre1.x = event.getX(1);
                    pre1.y = event.getY(1);
                }

                if (event.getPointerCount() == 1) {
                    isMoved = false;
                    lastClickTime = System.currentTimeMillis();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pre.x == -1 && pre.y == -1) {
                    pre.x = event.getRawX();
                    pre.y = event.getRawY();
                    return true;
                }
                if (event.getPointerCount() == 1) {
                    current.x = event.getRawX();
                    current.y = event.getRawY();

                    if ((Math.abs(current.x - pre.x) > 1e-8 && Math.abs(current.y - pre.y) > 1e-8)
                            || System.currentTimeMillis() - lastClickTime > 300) {
                        if (!isMoved) {
                            isMoved = true;
                            startAnimation(lineview, animation_alpha_in, ANIM_TOUCH);
                        }
                        current.x = event.getRawX();
                        current.y = event.getRawY();

                        tilt.x += current.x - pre.x;
                        tilt.y += current.y - pre.y;

                        pre.x = current.x;
                        pre.y = current.y;

                        setLinearBlurView(Constants.PREVIEW_IMAGE);
                    }
                } else if (event.getPointerCount() == 2) {
                    isMoved = true;
                    current0.x = event.getX(0);
                    current0.y = event.getY(0);

                    current1.x = event.getX(1);
                    current1.y = event.getY(1);

                    double pi = Math.acos(-1.0);
                    float x1 = pre1.x - pre0.x;
                    float y1 = pre1.y - pre0.y;

                    float x2 = current1.x - current0.x;
                    float y2 = current1.y - current0.y;

                    double cos = (x1 * x2 + y1 * y2) / Math.sqrt(x1 * x1 + y1 * y1) / Math.sqrt(x2 * x2 + y2 * y2);
                    cos = Math.min(cos, 1.0);
                    cos = Math.max(cos, 0.0);

                    float diffRotate = (float) Math.acos(cos);
                    diffRotate = (float) (diffRotate * 180.0f / pi);

                    float cross = x1 * y2 - x2 * y1;
                    if (cross < 0) {
                        diffRotate = -diffRotate;
                    }
                    tiltRotate += diffRotate;
                    while (tiltRotate > 360.0f) {
                        tiltRotate -= 360.0f;
                    }
                    while (tiltRotate < 0.0f) {
                        tiltRotate += 360.0f;
                    }

                    double fingerDis = Utils.distanceBetweenFingers(event);
                    double rate = fingerDis / lastFingerDis;
                    tiltHeight *= rate;

                    tiltHeight = Math.max(tiltHeight, Constants.BLUR_MIN_WIDTH);

                    setLinearBlurView(Constants.PREVIEW_IMAGE);

                    lastFingerDis = fingerDis;
                    pre0.x = current0.x;
                    pre0.y = current0.y;

                    pre1.x = current1.x;
                    pre1.y = current1.y;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                pre.x = -1;
                pre.y = -1;
                break;
            case MotionEvent.ACTION_UP:
                if (event.getPointerCount() == 1) {
                    if (!isMoved) {
                        // 如果这次触摸事件结束后触摸点没有进行移动，那么将移轴效果的中心点设置成现在的触摸点坐标
                        tilt.x = event.getRawX();
                        tilt.y = event.getRawY();

                        startAnimation(lineview, animation_alpha_in, ANIM_INIT);
                    } else {
                        // 如果这次触摸事件结束后触摸点进行了移动，那么显示移轴效果结束的动画
                        startAnimation(lineview, animation_alpha_out, ANIM_TOUCH);
                        setLinearBlurView(Constants.FINAL_IMAGE);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void setLinearBlurView(int blur_type) {
        linearBlurview.setData(blur_type, tilt.x, tilt.y, tiltRotate, tiltHeight, previewBitmap, finalBitmap);
        linearBlurview.invalidate();

        lineview.drawLine(tilt.x, tilt.y, tiltRotate, tiltHeight);
        lineview.invalidate();
    }
}
