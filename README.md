## 这个项目是实现类似Instagram的移轴效果。

### demo下载链接

<https://github.com/cashow/Android-Tilt-Shift/blob/master/demo/demo.apk>

### 径向移轴效果图

![tilt-shift-round](https://github.com/cashow/Android-Tilt-Shift/blob/master/demo/tilt-shift-round.gif)

### 线性移轴效果图

![tilt-shift-line](https://github.com/cashow/Android-Tilt-Shift/blob/master/demo/tilt-shift-line.gif)

--------------------

### 实现原理

移轴效果是利用了Paint的Xfermode里的Mode.DST_IN属性，这个属性的作用是将2张图片取交集。

我们需要2张图片，一张是原图的高斯模糊图片，还有一张是中心区域是透明的，其他区域是不透明的纯白图片。将这2张图片取交集，就能得到中心是透明的高斯模糊图片。

![tilt-shift-line](https://github.com/cashow/Android-Tilt-Shift/blob/master/demo/tilt-info.jpg)

--------------------

### 项目架构

```
com.cashow.tiltshift
    activity    // activity文件夹
        BaseActivity.java    // 存放activity常用的函数
        MainActivity.java    // 主界面的activity

    data    // 需要使用到的数据类文件夹
        Point.java    // 坐标类，含有x坐标和y坐标

    util    // 工具类文件夹
        BitmapUtils.java    // 处理bitmap的工具类
        BlurUtil.java    // 这个文件用来控制线性移轴和径向移轴的切换，并负责将触摸事件从activity
                            传到对应的 LinearBlurUtil 或者 RoundBlurUtil
        Constants.java    // 静态变量
        GaussianBlur.java    // 用来生成高斯模糊的类
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
