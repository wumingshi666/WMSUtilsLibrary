package com.wumingshi.wmsutilslibrary.exts

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity

/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/10/25
 *
 */

/**
 * Get activity
 *
 * LocalContext.current.getActivity()
 * @return
 */
fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}