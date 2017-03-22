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

/**
 * 主界面的activity只需要与blurUtil进行沟通，所有的触摸事件和点击事件都传给blurUtil去处理
 */
public class MainActivity extends AppCompatActivity {

    // 图片控件
    private SquareImageView photo;

    // 隐藏移轴效果的TextView
    private TextView text_blur_close;
    // 切换成径向移轴的TextView
    private TextView text_blur_round;
    // 切换成线性移轴的TextView
    private TextView text_blur_linear;

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

    // 绑定view
    private void findView() {
        photo = (SquareImageView) findViewById(R.id.photo);
        text_blur_close = (TextView) findViewById(R.id.text_blur_close);
        text_blur_round = (TextView) findViewById(R.id.text_blur_round);
        text_blur_linear = (TextView) findViewById(R.id.text_blur_linear);
    }

    // 初始化view
    private void initView() {
        findView();
        setClickListener();

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

    // 设置点击事件
    private void setClickListener() {
        text_blur_close.setOnClickListener(clickListener);
        text_blur_linear.setOnClickListener(clickListener);
        text_blur_round.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.text_blur_close:
                    // 隐藏移轴效果
                    blurUtil.setBlurType(BlurUtil.BLUR_NONE);
                    break;
                case R.id.text_blur_linear:
                    // 切换成线性移轴状态
                    blurUtil.setBlurType(BlurUtil.BLUR_LINEAR);
                    break;
                case R.id.text_blur_round:
                    // 切换成径向移轴状态
                    blurUtil.setBlurType(BlurUtil.BLUR_ROUND);
                    break;
                default:
                    break;
            }
        }
    };
}
