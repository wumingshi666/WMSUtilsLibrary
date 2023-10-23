package com.wumingshi.wmsutilslibrary.exts

import android.util.Log

/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/10/24
 *
 */
const val TAG = "TAG"

/**
 * 简单loge控制台 方便调试 TAG="TAG"
 *
 */
fun String.loge()=Log.e(TAG, this)