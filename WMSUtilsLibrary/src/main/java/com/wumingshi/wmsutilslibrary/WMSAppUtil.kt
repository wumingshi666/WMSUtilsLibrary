package com.wumingshi.wmsutilslibrary

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import com.wumingshi.wmsutilslibrary.exts.deleteDirectoryAll

/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/9/14
 *
 */
object WMSAppUtil {
    @SuppressLint("PrivateApi")
    fun getApplicationByReflect(): Application {
        lateinit var application: Application
        try {
            application = Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication")
                .invoke(null) as Application

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return application
    }

    /**
     * Get external storage directory
     *
     * 获取外部存储目录  封装  Environment.getExternalStorageDirectory()
     */
    fun getExternalStorageDirectory()= Environment.getExternalStorageDirectory()


    /**
     * Goto app settings
     *
     * 跳到APP设置页面
     * @param activity Activity 上下文 调用 ComponentActivity 里的函数
     */
    fun gotoAppSettings(activity: ComponentActivity,callback:(ActivityResult)->Unit={}) {
        //跳转该App的系统设置页面Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        WMSActivityUtil.startIntent(
            activity,
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.application.packageName, null)
            }) {
            callback(it)
        }
    }


    /**
     * Clear cache
     * 删除 缓存目录文件 context.cacheDir
     *
     * @param context
     */
    fun clearCache(context: Context= getApplicationByReflect()):Boolean{

       return context.cacheDir.deleteDirectoryAll()
    }




}