package com.cashow.tiltshift.util;

/**
 * 这个文件是用来存放静态变量
 */
public interface Constants {
    // 移轴区域的最小半径
    int BLUR_MIN_WIDTH = 40;

    // bitmap的最大宽度和高度
    double MAX_WIDTH = 800.0;

    // 进行移轴操作时的2个状态：
    // PREVIEW_IMAGE: 正在进行移轴操作，这时需要显示高模糊度的bitmap
    // FINAL_IMAGE: 移轴操作已经完成，这时需要显示低模糊度的bitmap
    int PREVIEW_IMAGE = 0;
    int FINAL_IMAGE = 1;
}
