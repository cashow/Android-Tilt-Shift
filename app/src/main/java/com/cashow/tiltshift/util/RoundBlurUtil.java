package com.cashow.tiltshift.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.cashow.tiltshift.R;
import com.cashow.tiltshift.view.RoundBlurView;
import com.cashow.tiltshift.view.RoundView;

public class RoundBlurUtil {
	private RoundBlurView roundBlurview;
	private RoundView roundview;

	private int screenWidth;
	private double lastFingerDis;

	private float tiltX;
	private float tiltY;
	private float tiltRadius;

	private float preX;
	private float preY;
	private float currentX;
	private float currentY;

	private boolean isMoved;

	private long lastClickTime;

	private int animationType;

	private Bitmap previewBitmap;
	private Bitmap finalBitmap;

	private Animation animation_alpha_in;
	private Animation animation_alpha_init_in;
	private Animation animation_alpha_out;

	private Context context;

	private static final int ANIM_INIT = 1;
	private static final int ANIM_TOUCH = 2;
	private static final int ANIM_AFTER_INIT = 3;

	public RoundBlurUtil(Activity mActivity) {
		this.context = mActivity.getApplicationContext();

		screenWidth = Utils.getScreenWidth(context);

		tiltX = screenWidth / 2;
		tiltY = screenWidth / 2;
		tiltRadius = screenWidth * 0.1875f;
	}

	public void init(RoundBlurView roundBlurview, final RoundView roundview, Bitmap finalBitmap, Bitmap previewBitmap) {
		this.roundBlurview = roundBlurview;
		this.roundview = roundview;
		this.finalBitmap = finalBitmap;
		this.previewBitmap = previewBitmap;

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
			setRoundBlurView(Constants.FINAL_IMAGE);
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
				lastFingerDis = Utils.distanceBetweenFingers(event);
			}
			break;
		case MotionEvent.ACTION_DOWN:
			preX = event.getRawX();
			preY = event.getRawY();

			if (event.getPointerCount() == 1) {
				isMoved = false;
				lastClickTime = System.currentTimeMillis();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (preX == -1 && preY == -1) {
				preX = event.getRawX();
				preY = event.getRawY();

				return true;
			}
			if (event.getPointerCount() == 1) {
				currentX = event.getRawX();
				currentY = event.getRawY();

				if (Math.abs(currentX - preX) > 1e-8 && Math.abs(currentY - preY) > 1e-8 || System.currentTimeMillis() - lastClickTime > 300) {
					if (!isMoved) {
						isMoved = true;
						startAnimation(roundview, animation_alpha_in, ANIM_TOUCH);
					}
					tiltX += currentX - preX;
					tiltY += currentY - preY;

					preX = currentX;
					preY = currentY;

					setRoundBlurView(Constants.PREVIEW_IMAGE);
				}
			} else if (event.getPointerCount() == 2) {
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
			preX = -1;
			preY = -1;
			break;
		case MotionEvent.ACTION_UP:
			if (event.getPointerCount() == 1) {

				if (!isMoved) {
					tiltX = event.getRawX();
					tiltY = event.getRawY();

					startAnimation(roundview, animation_alpha_in, ANIM_INIT);
				} else {
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
		roundBlurview.setData(blur_type, tiltX, tiltY, tiltRadius, previewBitmap, finalBitmap);
		roundBlurview.invalidate();

		roundview.drawRound(tiltX, tiltY, tiltRadius);
		roundview.invalidate();
	}
}
