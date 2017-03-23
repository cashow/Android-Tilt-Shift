#!/bin/bash

# 开始生成debug版本的apk
./gradlew 'assembleDebug'

# debug版本的apk的文件名
filename='app/build/outputs/apk/app-debug.apk'

# 新的文件名
newFileName="demo/demo.apk"

# 移动apk
mv -f $filename $newFileName
