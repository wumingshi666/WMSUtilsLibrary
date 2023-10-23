package com.wumingshi.wmsutilslibrary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.wumingshi.wmsutilslibrary.exts.startContractForResultWMS

/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/9/14
 *
 */

/*
shouldShowRequestPermissionRationale根据先前权限请求中的用户首选项返回true或false。

如果用户刚刚拒绝许可(不是永远)，shouldShowRequestPermissionRationale将返回true。
如果永远拒绝许可，则返回false。
诀窍在于，即使用户允许，然后shouldShowRequestPermissionRationale也会返回false。

因此，我们可以结合两个条件来选择是否再次询问。

因此，如果用户不允许的许可权，并且shouldShowRequestPermissionRationale返回false，则意味着用户选择不再询问许可权。
*/
//跳转该App的系统设置页面Settings.ACTION_APPLICATION_DETAILS_SETTINGS

// 在当前文件的顶层定义扩展函数

/**
 * permission util
 * 请求权限工具类
 *
 */
object WMSPermissionUtil {


    /**
     * Config
     * 工具类配置项,如果更多配置可以考虑公开配置类.apply设置,少量的配置属性直接initialize
     *
     * @constructor Create empty Config
     */
    private object Config {
        var activity: ComponentActivity? = null
    }

    /**
     * Permission
     *
     * 权限常量类
     *
     */
    object Permission {
        /*    Android应用权限大全（Manifest.permission）_leekey_sjtu的博客-CSDN博客
    https://blog.csdn.net/qq_37689207/article/details/128753304*/


        /**
         * Read Media Images
         * 读取媒体图片  大于安卓13
         */
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES

        /**
         * Read Media AUDIO
         * 读取媒体音频  大于安卓13
         */
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO

        /**
         * Read Media VIDEO
         * 读取媒体视频  大于安卓13
         */
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        const val READ_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO

        /**
         * Read Media Visual User Selected
         * 读取所选媒体视觉对象用户 大于安卓14
         */
        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        const val READ_MEDIA_VISUAL_USER_SELECTED =
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED


        /**
         * 读存储权限 小于安卓13
         * @see
         */
        const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE

        /**
         * 写存储权限 小于安卓11
         */
        const val WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE

        @RequiresApi(Build.VERSION_CODES.R)
        /**
         * 此权限的检查用 Environment.isExternalStorageManager()  checkSelfPermission封装了他
         * 此权限的申请用 Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION) 需要跳到设置里手动开启
         * 需要重写 默认的 call 自行跳转 自行检查权限
         */
        const val MANAGE_EXTERNAL_STORAGE = Manifest.permission.MANAGE_EXTERNAL_STORAGE


        // 位置权限
        const val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

        // 相机权限
        const val CAMERA = Manifest.permission.CAMERA

        // 录音权限麦克风权限
        const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO

        // 联系人权限
        const val READ_CONTACTS = Manifest.permission.READ_CONTACTS
        const val WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS

        // 日历权限
        const val READ_CALENDAR = Manifest.permission.READ_CALENDAR
        const val WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR

        // SMS权限
        const val READ_SMS = Manifest.permission.READ_SMS
        const val SEND_SMS = Manifest.permission.SEND_SMS
        const val RECEIVE_SMS = Manifest.permission.RECEIVE_SMS

        // 电话权限
        const val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
        const val CALL_PHONE = Manifest.permission.CALL_PHONE
        const val READ_CALL_LOG = Manifest.permission.READ_CALL_LOG
        const val WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG

        // 系统权限
        const val INSTALL_PACKAGES = Manifest.permission.INSTALL_PACKAGES
        const val DELETE_PACKAGES = Manifest.permission.DELETE_PACKAGES
        const val SYSTEM_ALERT_WINDOW = Manifest.permission.SYSTEM_ALERT_WINDOW

        // 开机启动权限
        const val RECEIVE_BOOT_COMPLETED = Manifest.permission.RECEIVE_BOOT_COMPLETED

        // 辅助功能权限
        const val BIND_ACCESSIBILITY_SERVICE = Manifest.permission.BIND_ACCESSIBILITY_SERVICE

        // 有背景活动权限
        @RequiresApi(Build.VERSION_CODES.P)
        const val FOREGROUND_SERVICE = Manifest.permission.FOREGROUND_SERVICE

        // VPN权限
        const val BIND_VPN_SERVICE = Manifest.permission.BIND_VPN_SERVICE

        //WiFi权限
        const val CHANGE_WIFI_STATE = Manifest.permission.CHANGE_WIFI_STATE
        const val ACCESS_WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE

        //NFC权限
        const val NFC = Manifest.permission.NFC
    }

    /**
     * Initialize
     *
     *传入MainActivity,只是为了简化函数调用不需要每次都传参初始化一次即可
     * @param activity
     * @return 返回自身方便赋值简短命名
     */
    fun initialize(activity: ComponentActivity): WMSPermissionUtil {
        Config.activity = activity
        return this
    }

    /**
     * Check initialized
     *
     * 检查初始化
     * @return
     */
    private fun checkInitialized(): Config {//直接使用非空断言,以后如果统一风格或扩展再用这个
        if (Config.activity == null) {
            throw NullPointerException("Activity not initialized")
        }
        return Config
    }


    /**
     * Request permission
     *
     * 简化函数需先调用 initialize 具体说明看重载函数
     * @param permission
     * @param callback
     * @receiver
     */
    fun requestPermission(
        permission: String,
        callback: (isGranted: Boolean, isDenied: Boolean) -> Unit,
    ) = requestPermission(Config.activity!!, permission, callback)

    /**
     *
     * 请求单个权限
     *
     * @param activity Activity 上下文 调用 ComponentActivity 里的函数
     * @param permission 权限常量
     * @param callback isGranted: true 就是同意了 false 拒绝了，isDenied: true是永久拒绝了 false 是有权限或没永久拒绝
     * @receiver
     */
    fun requestPermission(
        activity: ComponentActivity,
        permission: String,
        callback: (isGranted: Boolean, isDenied: Boolean) -> Unit,
    ) {
        activity.startContractForResultWMS(
            ActivityResultContracts.RequestPermission(), permission
        ) {
            //这里如果有权限it=true  isDeniedPermission=false isDenied就=false
            // it=false isDeniedPermission=true  isDenied就=false
            // it=false isDeniedPermission=false  isDenied就=true
            callback(it, it == isDeniedUIPermission(activity, permission))
        }
    }


    /**
     * Request permissions
     *
     * 简化函数需先调用 initialize 具体说明看重载函数
     * @param permissions
     * @param callback
     * @receiver
     */
    fun requestPermissions(permissions: Array<String>, callback: (Map<String, Boolean>) -> Unit) =
        requestPermissions(Config.activity!!, permissions, callback)

    /**
     * 请求多个权限，可以自行遍历权限是否全部同意或用isGrantedPermissions。
     * 需要手动设置的权限(不弹UI的权限)不建议多个
     *
     * @param activity Activity 上下文，调用 ComponentActivity 中的函数。
     * @param permissions 权限数组，包含要请求的多个权限常量。
     * @param callback 回调函数，将返回一个包含权限名称和对应是否同意的映射。
     *                 映射的键为权限名称，值为布尔值，true 表示同意，false 表示拒绝。
     * @receiver
     */
    fun requestPermissions(
        activity: ComponentActivity,
        permissions: Array<String>,
        callback: (Map<String, Boolean>) -> Unit
    ) {
        activity.startContractForResultWMS(
            ActivityResultContracts.RequestMultiplePermissions(), permissions
        ) { map ->
            callback(
                map
            )
        }


    }

    /**
     * Goto permissions settings
     *
     * 简化函数需先调用 initialize 具体说明看重载函数
     * @param intent
     * @param callback
     * @receiver
     */
    fun gotoPermissionsSettings(
        intent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", Config.activity!!.application.packageName, null)
        }, callback: (ActivityResult) -> Unit
    ) = gotoPermissionsSettings(Config.activity!!, intent, callback)

    /**
     * Goto permissions settings
     * 跳到对应的权限设置
     *
     * @param activity
     * @param intent 默认跳到APP设置 可以自定义意图调到对应的设置 Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
     * @param callback 返回APP时的回调 可以再次检查权限
     * @receiver
     */
    fun gotoPermissionsSettings(
        activity: ComponentActivity,
        intent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.application.packageName, null)
        },
        callback: (ActivityResult) -> Unit
    ) {
        WMSActivityUtil.startIntent(
            activity, intent
        ) {
            callback(it)
        }
    }


    /**
     * Is denied u i permission
     *
     * 简化函数需先调用 initialize 具体说明看重载函数
     *
     * @param permission
     */
    fun isDeniedUIPermission(permission: String) =
        isDeniedUIPermission(Config.activity!!, permission)

    /**
     * Is denied permission
     *
     * 检查是否永久拒绝权限(就是是否显示权限UI)
     * 必须申请权限回调里 先申请权限如果被拒绝返回true是没被永久拒绝 false是被永久拒绝
     * 内部就是 shouldShowRequestPermissionRationale 是否显示权限UI
     * @param activity  Activity 上下文 调用 ComponentActivity 里的函数
     * @param permission 权限常量 Permission.
     * @return
     */
    fun isDeniedUIPermission(
        activity: ComponentActivity, permission: String
    ): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }

    /**
     * Is denied u i permissions
     *
     * 简化函数需先调用 initialize 具体说明看重载函数
     * @param permission
     * @return
     */
    fun isDeniedUIPermissions(
        permission: Array<String>
    ) = isDeniedUIPermissions(Config.activity!!, permission)

    /**
     * Is denied permissions
     *
     * 检查是否永久拒绝权限(就是是否显示权限UI)
     * 必须申请权限回调里 先申请权限如果任意一个被拒绝但是没被永久拒绝返回true  false是被永久拒绝
     * 内部就是 shouldShowRequestPermissionRationale 是否显示权限UI
     * @param activity  Activity 上下文 调用 ComponentActivity 里的函数
     * @param permission 权限常量数组 Permission.
     * @return
     */
    fun isDeniedUIPermissions(
        activity: ComponentActivity, permission: Array<String>
    ): Boolean {/*permission.forEach {
           with(activity) {
                if (!isGrantedPermission(this, it)){
                   return isDeniedPermission(this, it)
                }
            }
        }*/
        return permission.none {
            !isGrantedPermission(activity, it) && !isDeniedUIPermission(activity, it)
        }


    }


    /**
     * Is granted permission
     *
     * 简化函数需先调用 initialize 具体说明看重载函数
     * @param permission
     * @return
     */
    fun isGrantedPermission(permission: String) = isGrantedPermission(Config.activity!!, permission)

    /**
     * Check self permission
     *
     * 检查是否有权限
     *
     * @param activity  Activity 上下文 调用 ComponentActivity 里的函数
     * @param permission 权限常量 Permission.
     * @return true有权限
     */
    fun isGrantedPermission(activity: ComponentActivity, permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && permission == Permission.MANAGE_EXTERNAL_STORAGE) {
            return Environment.isExternalStorageManager()
        }
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Is granted permissions
     *
     * 简化函数需先调用 initialize 具体说明看重载函数
     * @param permission
     */
    fun isGrantedPermissions(permission: Array<String>) =
        isGrantedPermissions(Config.activity!!, permission)

    /**
     * Is granted permissions
     *
     * 是否同意权限全部
     *
     * @param activity
     * @param permission
     * @return
     */
    fun isGrantedPermissions(activity: ComponentActivity, permission: Array<String>): Boolean {
        var isAllGranted = false
        isGrantedPermissions(activity, permission) { map ->
            isAllGranted = map.values.all { it }
        }
        return isAllGranted
    }

    /**
     * Is granted permissions
     *
     * @param activity
     * @param permission
     * @param callback
     * @receiver
     */
    private fun isGrantedPermissions(
        activity: ComponentActivity,
        permission: Array<String>,
        callback: (Map<String, Boolean>) -> Unit
    ) {
        val map = mutableMapOf<String, Boolean>()
        permission.forEach {
            map[it] = isGrantedPermission(activity, it)
        }
        callback(map)
    }


}
