package com.cashow.tiltshift.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cashow.tiltshift.R;
import com.cashow.tiltshift.util.BlurUtil;
import com.cashow.tiltshift.view.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 主界面的activity只需要与blurUtil进行沟通，所有的触摸事件和点击事件都传给blurUtil去处理
 */
public class MainActivity extends AppCompatActivity {

    // 图片控件
    @BindView(R.id.photo)
    SquareImageView photo;

    // 隐藏移轴效果的TextView
    @BindView(R.id.text_blur_close)
    TextView text_blur_close;
    // 切换成径向移轴的TextView
    @BindView(R.id.text_blur_round)
    TextView text_blur_round;
    // 切换成线性移轴的TextView
    @BindView(R.id.text_blur_linear)
    TextView text_blur_linear;

    // 处理移轴效果切换的util
    private BlurUtil blurUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 隐藏状态栏
        hideStatusBar();
        initView();
    }

    // 初始化view
    private void initView() {
        ButterKnife.bind(this);

        // 初始化blurUtil
        blurUtil = new BlurUtil(this);

        // 处理触摸事件
        photo.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return blurUtil.handleTouchEvent(event);
            }
        });
    }

    // 隐藏状态栏
    private void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @OnClick(R.id.text_blur_close)
    public void closeBlur(View view) {
        // 隐藏移轴效果
        blurUtil.setBlurType(BlurUtil.BLUR_NONE);
    }

    @OnClick(R.id.text_blur_linear)
    public void changeToLinear(View view) {
        // 切换成线性移轴状态
        blurUtil.setBlurType(BlurUtil.BLUR_LINEAR);
    }

    @OnClick(R.id.text_blur_round)
    public void changeToRound(View view) {
        // 切换成径向移轴状态
        blurUtil.setBlurType(BlurUtil.BLUR_ROUND);
    }
}
