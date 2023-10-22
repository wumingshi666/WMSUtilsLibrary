package com.wumingshi.wmsutilslibrary

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.wumingshi.wmsutilslibrary.exts.startContractForResultWMS


/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/9/18
 *
 */
object WMSActivityUtil {

    /**
     *
     * 简单的意图跳转主要用于跳转到设置
     * Start intent/StartActivity
     * 合约是 StartActivityForResult()
     * 使用startContractForResultWMS 跳转到意图 主要是系统设置页面
     *
     * @param activity
     */
    fun startIntent(activity: ComponentActivity, intent: Intent,callback:(ActivityResult)->Unit) {
        activity.startContractForResultWMS(
            ActivityResultContracts.StartActivityForResult(),intent,callback)
    }

    /**
     *
     * 简单的意图跳转
     * startIntentSenderRequest/StartActivity
     * 合约是 StartIntentSenderForResult()
     * 使用startContractForResultWMS 跳转到意图
     *
     * @param activity
     */
    fun startIntentSenderRequest(activity: ComponentActivity, intentSenderRequest: IntentSenderRequest,callback:(ActivityResult)->Unit) {
        activity.startContractForResultWMS(
            ActivityResultContracts.StartIntentSenderForResult(),intentSenderRequest,callback)
    }
}