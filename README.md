## 这个项目是实现类似Instagram的移轴效果。

### 径向移轴效果图

![tilt-shift-round](https://github.com/cashow/Android-Tilt-Shift/blob/master/demo/tilt-shift-round.gif)

### 线性移轴效果图

![tilt-shift-line](https://github.com/cashow/Android-Tilt-Shift/blob/master/demo/tilt-shift-line.gif)

--------------------

### 项目架构

```
com.cashow.tiltshift
    activity    // activity文件夹
        MainActivity.java    // 主界面的activity

    data    // 需要使用到的数据类文件夹
        Point.java    // 坐标类，含有x坐标和y坐标

    util    // 工具类文件夹
        BitmapUtils.java    // 处理bitmap的工具类
        BlurUtil.java    // 这个文件用来控制线性移轴和径向移轴的切换，并负责将触摸事件从activity
                            传到对应的 LinearBlurUtil 或者 RoundBlurUtil
        Constants.java    // 静态变量
        LinearBlurUtil.java    // 用来记录和控制线性移轴相关的数据及业务逻辑
        RoundBlurUtil.java    // 用来记录和控制径向移轴相关的数据及业务逻辑
        Utils.java    // 常用的工具类

    view    // 这个文件夹用来存放自定义view
        LinearBlurView.java    // 线性移轴的效果图
        LineView.java    // 线性移轴的辅助线
        RoundBlurView.java    // 径向移轴的效果图
        RoundView.java    // 径向移轴的辅助线
        SquareImageView.java    // 正方形的ImageView
        SquareRelativeLayout.java    // 正方形的RelativeLayout
```

### TODO

将移轴效果做成module
