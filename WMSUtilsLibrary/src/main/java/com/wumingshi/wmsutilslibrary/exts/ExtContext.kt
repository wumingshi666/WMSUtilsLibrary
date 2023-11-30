package com.wumingshi.wmsutilslibrary.exts

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import androidx.activity.ComponentActivity

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


tailrec fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

tailrec fun Context.getComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getComponentActivity()
    else -> null
}

tailrec fun Context.getWindow(): Window? =
    when (this) {
        is Activity -> window
        is ContextWrapper -> baseContext.getWindow()
        else -> null
    }