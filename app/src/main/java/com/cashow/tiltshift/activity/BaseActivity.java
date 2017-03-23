package com.cashow.tiltshift.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kaopiz.kprogresshud.KProgressHUD;

/**
 * 这个文件是用来存放Activity通用的函数
 */
public class BaseActivity extends AppCompatActivity {
    protected AppCompatActivity mActivity;
    protected Context mContext;
    private KProgressHUD progressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        mContext = getApplicationContext();

        progressHUD = KProgressHUD.create(mActivity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
    }

    /**
     * 显示loading动画
     */
    public void showLoading(String label, String detailsLabel, boolean cancellable) {
        progressHUD.setCancellable(cancellable);
        if (label != null && label.length() > 0) {
            progressHUD.setLabel(label);
        } else {
            progressHUD.setLabel(null);
        }
        if (detailsLabel != null && detailsLabel.length() > 0) {
            progressHUD.setDetailsLabel(detailsLabel);
        } else {
            progressHUD.setDetailsLabel(null);
        }
        progressHUD.show();
    }


    /**
     * 显示只含有label的loading动画
     */
    public void showLoadingWithLabel(String label, boolean cancellable) {
        showLoading(label, "", cancellable);
    }

    /**
     * 显示只含有detailsLabel的loading动画
     */
    public void showLoadingWithDetailsLabel(String detailsLabel, boolean cancellable) {
        showLoading("", detailsLabel, cancellable);
    }

    /**
     * 显示不含有label和detailsLabel的loading动画
     */
    public void showLoading(boolean cancellable) {
        showLoading("", "", cancellable);
    }

    /**
     * 隐藏loading动画
     */
    public void hideLoading() {
        progressHUD.dismiss();
    }
}
