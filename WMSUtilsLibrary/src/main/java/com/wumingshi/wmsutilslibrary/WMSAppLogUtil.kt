package com.wumingshi.wmsutilslibrary

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.nio.charset.Charset

/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/10/24
 *
 */

/*Log.v(tag, msg) - verbose级别,输出冗长的调试信息
Log.d(tag, msg) - debug级别,输出调试信息
Log.i(tag, msg) - info级别,输出重要的信息
Log.w(tag, msg) - warn级别,输出警告信息
Log.e(tag, msg) - error级别,输出错误信息
从v到e级别依次递增,v级别输出的日志最多,e级别输出的日志最少。
一般在开发调试时,使用Log.v和Log.d输出详细日志,可以帮助调试。
在发布产品时,只使用Log.i、Log.w、Log.e输出RELEASE级别的日志信息。
另外,还可以使用Log.println()方法指定优先级*/



/**
 * W m s log util
 *
 * 日志类
 * @constructor Create empty W m s log util
 */
object WMSAppLogUtil {
    val config=Config


    /**
     * Application
     */
    private lateinit var mApplication: Context
    private lateinit var mFile: File



    /**
     * 日志文件携程作用域
     */
    private val mLogCS = CoroutineScope(Dispatchers.IO + SupervisorJob())/* GlobalScope.launch(Dispatchers.IO) {
                       }*//*runBlocking {
        launch(Dispatchers.IO) {
        }
    }*/

    /**
     * 日志配置类
     *
     */
    object Config {
        /**
         * 是否初始化
         */
       var isInit = false //是否初始化,会创建文件的顺带看看有没有权限
        /**
         * 日志文件编码
         */
        var charset: Charset = Charsets.UTF_8 //写出编码

        /**
         * 日志文件总目录默认APP外部储存目录/logs 非APP私有目录需要权限
         */
        var filePath: File? = null

        /**
         * 栈深度 -1 =全部
         */
        var stackDeep = -1 //栈深

        /**
         * 附加头信息
         */
        var head:String? =null
             //一些头信息

        /**
         * 全局Tag
         */
        var tag = "TAG" //全局TAG

        /**
         * Tag v
         */
        var tagV = "$tag-V"

        /**
         * Tag d
         */
        var tagD = "$tag-D"

        /**
         * Tag i
         */
        var tagI = "$tag-I"

        /**
         * Tag e
         */
        var tagE = "$tag-E"

        /**
         * Tag w
         */
        var tagW = "$tag-W"

        /**
         * 日志总开关
         */
        var switchLog = true //总开关

        /**
         * 头信息开关
         */
        var switchHead = true //头信息开关

        /**
         * 控制台开关
         */
        var switchConsole = true //控制台开关

        /**
         * 文件开关
         */
        var switchFile = false  //文件开关
    }


    /**
     * 初始化顺带创建个文件测试
     *
     */
    fun initialize(application: Context = WMSAppUtil.getApplicationByReflect()): WMSAppLogUtil {
        this.mApplication = application

        config.filePath = config.filePath ?: application.getExternalFilesDir(null)?.resolve("logs")
        config.head= config.head
            ?: ("****** Log Head ******\n" + "日志路径:${Config.filePath?.path}\n" + "****** Log End ******\n")
        config.filePath?.let {
            if (!it.exists()) it.mkdirs()
        }


        mFile = File(
            config.filePath, "${WMSDateUtil.getLocalDate()}-${mApplication.packageName}.txt"
        )

        if (mFile.exists()) {
            config.isInit = mFile.canWrite()
        } else {
            config.isInit = mFile.createNewFile()
            iHead(config.head!!)
        }
        return this
    }

    private fun checkInitialized(){//直接使用非空断言,以后如果统一风格或扩展再用这个

    }

    /**
     * Get file
     *
     * 返回日志文件
     * @return
     */
    fun getFile(): File {
        return this.mFile
    }


    private fun iHead(string: String) {
        config.takeIf { it.switchHead }?.run {
            if (switchConsole) {
                Log.i(tagI, string)
            }
            if (switchFile) {
                log2File(string)
            }
        }
    }


    fun i(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTrace()
            if (switchConsole) {
                Log.i(tagI, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagI] " + "{$string : $stack}")
            }
        }
    }


    fun v(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTrace()
            if (switchConsole) {
                Log.v(tagV, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagV] " + "{$string : $stack}")
            }
        }
    }

    fun d(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTrace()
            if (switchConsole) {
                Log.d(tagD, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagD] " + "{$string : $stack}")
            }
        }
    }


    fun w(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTrace()
            if (switchConsole) {
                Log.w(tagW, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagW] " + "{$string : $stack}")
            }
        }
    }

    fun e(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTrace()
            if (switchConsole) {
                Log.e(tagE, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagE] " + "{$string : $stack}")
            }
        }
    }


    /**
     * 日志写到文件
     *
     * @param string
     */
    private fun log2File(string: String) {
        config.takeIf { config.isInit }?.run {
            mLogCS.launch {
                /*if (!WMSDate.isToday(mDate)) { //判断是否过了一天是否要重新创建一个日志文件每天一个
                    mFile.appendText("******NextDay******\n", charset)
                    init()
                }*/
                mFile.appendText(string + "\n", charset)
            }
        }
    }

    /**
     * 获取栈信息
     *
     * @return
     */
    private fun getStackTrace(): String {
        val stackTrace = Throwable().stackTrace.drop(2)//删除自身堆栈
            .run {
                if (config.stackDeep != -1) take(config.stackDeep) else this
            }
        var string = ""
        for (element in stackTrace) {
            string += "${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})\n"
        }
        return string
    }

}