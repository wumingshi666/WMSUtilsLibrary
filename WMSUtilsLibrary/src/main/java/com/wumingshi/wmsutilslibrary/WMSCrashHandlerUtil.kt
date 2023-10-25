package com.wumingshi.wmsutilslibrary

import android.app.Application
import android.util.Log


/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/10/24
 *
 */

object WMSCrashHandlerUtil : Thread.UncaughtExceptionHandler {

    object Config {
        var tag = "GlobalExceptionHandler"

        /**
         * Call back
         *
         * 异常回调
         * 如果回调返回true继续走默认的处理程序 getDefaultUncaughtExceptionHandler.uncaughtException
         * 如果false 自行处理 非主线程不会崩溃
         * 最好还是记录完日志让它崩溃
         *
         * 回调里可以写 WMSAppLogUtil.e(tag, "Uncaught Exception: Thread=${t.name}", e)
         * 也可以主动杀死进程exitProcess(0)
         * 也可以判断 "main" == t.name 是不是主线程然后自行处理或让默认程序处理
         *
         *
         */
        var callBack: (t: Thread, e: Throwable) -> Boolean = { _, _ ->
            /*WMSAppLogUtil.instance.e(tag, "Uncaught Exception: Thread=${t}", e)
             GlobalScope.launch(Dispatchers.Main+SupervisorJob()) {
                 //nav.navigate("CrashLog")
                 //或跳到新Activity FLAG_ACTIVITY_NEW_TASK
             }
             "main" == t.name*/
            true
        }
    }

    /**
     * 是否初始化,会创建文件的顺带看看有没有权限
     */
    val isInit: Boolean get() = mIsInit
    val config = Config

    /**
     * 最近一次异常信息,如果要获取全部直接日志文件读取
     */
    val throwable: Throwable? get() = mThrowable

    /**
     * Throwable string
     * 全部异常String信息
     */
    val throwableString: String? get() = throwable?.let { Log.getStackTraceString(it) }


    /**
     * Default handler
     *
     * 原始的默认处理程序
     */
    val defaultHandler get() = mThrowable


    private var mIsInit: Boolean = false
    private lateinit var mContext: Application
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null

    private var mThrowable: Throwable? = null

    /**
     * Initialize
     *
     *
     * @param application
     */
    fun initialize(application: Application) {
        this.mContext = application
        this.mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        mIsInit = true
    }

    private fun checkInitialized() {//直接使用非空断言,以后如果统一风格或扩展再用这个

    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        this.mThrowable = e
        /* Log.e("GlobalExceptionHandler", "Uncaught Exception", e)*/
        if (config.callBack(t, e)) {
            mDefaultHandler?.uncaughtException(t, e)
        }

        /*android.os.Process.killProcess(android.os.Process.myPid())*/
        //默认异常处理
    }
}