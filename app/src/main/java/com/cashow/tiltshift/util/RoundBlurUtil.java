package com.cashow.tiltshift.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cashow.tiltshift.R;
import com.cashow.tiltshift.data.Point;
import com.cashow.tiltshift.view.RoundBlurView;
import com.cashow.tiltshift.view.RoundView;

/**
 * 这个util是用来记录和控制径向移轴相关的数据及业务逻辑，例如移轴的半径之类的
 * 用来显示移轴效果的自定义view放在roundBlurview里，用来显示移轴辅助线的自定义view放在roundview里
 * 外界并不会直接对roundBlurview和roundview进行操作，所有操作都需要通过调用RoundBlurUtil的接口实现
 * 这个util相当于MVP模式中的model层
 */
public class RoundBlurUtil {
    // 用来显示移轴效果的自定义view
	private RoundBlurView roundBlurview;
    // 用来显示移轴辅助线的自定义view
	private RoundView roundview;

    // 屏幕宽度
	private int screenWidth;
    // 上一次触摸时双指之间的距离
	private double lastFingerDis;

    // 移轴效果的中心坐标
    private Point tilt = new Point();
    // 上一次触摸时的坐标
    private Point pre = new Point();
    // 触摸点现在的坐标
    private Point current = new Point();

    // 移轴效果的半径
	private float tiltRadius;

    // isMoved和lastClickTime是用来判断这次触摸事件是不是单纯的点击事件
	private boolean isMoved;
	private long lastClickTime;

    // 进行移轴操作时预览用的高模糊度bitmap
	private Bitmap previewBitmap;
    // 移轴效果操作完毕后显示的低模糊度bitmap
	private Bitmap finalBitmap;

    // 移轴效果渐隐渐出的动画效果
	private Animation animation_alpha_in;
	private Animation animation_alpha_init_in;
	private Animation animation_alpha_out;

	private Context context;

    // 目前的动画类型。有3种动画类型：
    // ANIM_INIT：从其他移轴效果切换到线性移轴效果时要显示的动画，主要是显示移轴预览效果以及辅助线
    // ANIM_INIT_FINISHED：在ANIM_INIT动画结束后会立即调用ANIM_INIT_FINISHED动画，主要是隐藏移轴预览效果以及辅助线
    // ANIM_TOUCH：用户开始拖动和缩放移轴区域时要显示的动画，在动画开始前要显示预览效果及辅助线，动画结束后隐藏预览效果及辅助线
    private int animationType;

	private static final int ANIM_INIT = 1;
	private static final int ANIM_TOUCH = 2;
	private static final int ANIM_AFTER_INIT = 3;

	public RoundBlurUtil(Activity mActivity) {
		this.context = mActivity.getApplicationContext();

        // 获取屏幕宽度
		screenWidth = Utils.getScreenWidth(context);

        // 移轴效果初始的位置在屏幕最中间
        tilt.setPoint(screenWidth / 2, screenWidth / 2);
        // 将初始的移轴效果半径设置成屏幕宽度的 3/16
		tiltRadius = screenWidth * 0.1875f;
	}

	public void init(RoundBlurView roundBlurview, final RoundView roundview, Bitmap finalBitmap, Bitmap previewBitmap) {
		this.roundBlurview = roundBlurview;
		this.roundview = roundview;
		this.finalBitmap = finalBitmap;
		this.previewBitmap = previewBitmap;

        initAnimation();
	}

	private void initAnimation() {
        animation_alpha_in = AnimationUtils.loadAnimation(context, R.anim.alpha_in);
        animation_alpha_out = AnimationUtils.loadAnimation(context, R.anim.photo_alpha_out);
        animation_alpha_init_in = AnimationUtils.loadAnimation(context, R.anim.photo_alpha_in);

        setAnimationListener(animation_alpha_in);
        setAnimationListener(animation_alpha_out);
        setAnimationListener(animation_alpha_init_in);
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
			setRoundBlurView(Constants.PREVIEW_IMAGE);
			roundview.setVisibility(View.VISIBLE);
			break;
		case ANIM_TOUCH:
			roundview.setVisibility(View.VISIBLE);
			break;
		case ANIM_AFTER_INIT:
			setRoundBlurView(Constants.PREVIEW_IMAGE);
			break;
		default:
			break;
		}
	}

	private void animationEnd() {
		switch (animationType) {
		case ANIM_INIT:
			startAnimation(roundview, animation_alpha_out, ANIM_AFTER_INIT);
			break;
		case ANIM_TOUCH:
			setRoundBlurView(Constants.PREVIEW_IMAGE);
			roundview.setVisibility(View.GONE);
			break;
		case ANIM_AFTER_INIT:
			setRoundBlurView(Constants.FINAL_IMAGE);
			roundview.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	public void showRoundView() {
		roundBlurview.setVisibility(View.VISIBLE);
		startAnimation(roundview, animation_alpha_init_in, ANIM_INIT);
	}

	private void startAnimation(View view, Animation animation, int anim_type) {
		animationType = anim_type;
		view.startAnimation(animation);
	}

	public boolean handleBlurRoundEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_POINTER_DOWN:
			if (event.getPointerCount() == 2) {
                // 如果有2个以上的触摸点，计算2个触摸点之间初始的距离
				lastFingerDis = Utils.distanceBetweenFingers(event);
			}
			break;
		case MotionEvent.ACTION_DOWN:
            // 在出现新的触摸点时记录下触摸点的坐标
			pre.x = event.getRawX();
			pre.y = event.getRawY();

			if (event.getPointerCount() == 1) {
                // 如果只有一个触摸点，将isMoved置为false并记录点击的时间
                // 这2个变量是用来判断这次触摸事件是不是单纯的点击事件
				isMoved = false;
				lastClickTime = System.currentTimeMillis();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (pre.x == -1 && pre.y == -1) {
                // 如果没有记录上一次触摸点的位置，将这次的位置赋值给上一次触摸点pre
				pre.x = event.getRawX();
				pre.y = event.getRawY();
				return true;
			}
			if (event.getPointerCount() == 1) {
				current.x = event.getRawX();
				current.y = event.getRawY();

				if (Math.abs(current.x - pre.x) > 1e-8 && Math.abs(current.y - pre.y) > 1e-8 || System.currentTimeMillis() - lastClickTime > 300) {
                    // 如果触摸点移动过了，或者触摸时间超过300ms，那么这一次的触摸事件就不认定成点击事件
                    // 触摸点移动时移轴效果的中心点也要跟着移动
					if (!isMoved) {
						isMoved = true;
						startAnimation(roundview, animation_alpha_in, ANIM_TOUCH);
					}
					tilt.x += current.x - pre.x;
					tilt.y += current.y - pre.y;

					pre.x = current.x;
					pre.y = current.y;

					setRoundBlurView(Constants.PREVIEW_IMAGE);
				}
			} else if (event.getPointerCount() == 2) {
                // 有2个触摸点时要缩放移轴的显示区域
                // 触摸点距离变大时，需要加大移轴效果的半径
				isMoved = true;
				double fingerDis = Utils.distanceBetweenFingers(event);
				double rate = fingerDis / lastFingerDis;
				tiltRadius *= rate;
				tiltRadius = Math.max(tiltRadius, Constants.BLUR_MIN_WIDTH);

				setRoundBlurView(Constants.PREVIEW_IMAGE);

				lastFingerDis = fingerDis;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			pre.x = -1;
			pre.y = -1;
			break;
		case MotionEvent.ACTION_UP:
			if (event.getPointerCount() == 1) {

				if (!isMoved) {
                    // 如果这次触摸事件结束后触摸点没有进行移动，那么这次触摸事件可以认定是点击事件
                    // 这个时候应该将移轴效果的中心点设置成现在的触摸点坐标
					tilt.x = event.getRawX();
					tilt.y = event.getRawY();

					startAnimation(roundview, animation_alpha_in, ANIM_INIT);
				} else {
                    // 如果这次触摸事件结束后触摸点进行了移动，那么显示移轴效果淡出的动画
                    startAnimation(roundview, animation_alpha_out, ANIM_TOUCH);
					setRoundBlurView(Constants.FINAL_IMAGE);
				}
			}
			break;
		default:
			break;
		}
		return true;
	}

	private void setRoundBlurView(int blur_type) {
        // 移轴的位置和区域大小有变化时，都需要调用这个函数提示更新移轴图片和辅助线
		roundBlurview.setData(blur_type, tilt.x, tilt.y, tiltRadius, previewBitmap, finalBitmap);
		roundBlurview.invalidate();

		roundview.drawRound(tilt.x, tilt.y, tiltRadius);
		roundview.invalidate();
	}
}
