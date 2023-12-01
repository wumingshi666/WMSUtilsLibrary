package com.wumingshi.wmsutilslibrary

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException
import java.nio.charset.Charset

/**备忘日志
 * 编码风格: 从上往下 内部类统一放最上并收缩(私有类常亮类最底), 伴生对象 公开属性 ,私有属性 类最外部扩展函数
 * 单例 不要用 lateinit var instance: WMSAppLogUtil  用  val instance: WMSAppLogUtil = WMSAppLogUtil()
 * 是否初始化 属性放在类里不放在配置里
 * 需要传参的统一函数 initialize 需要检查初始化状态的统一函数 checkInitialized
 */


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
 * 可以自己创建也可以获取单例
 * @constructor Create empty W m s log util
 */
class WMSAppLogUtil : WMSBaseUtil() {
    private val className = this.javaClass.name
    private val packageName = this.javaClass.`package`?.name

    /**
     * 日志配置类
     *
     */
    class Config {
        /**
         * 是否初始化,会创建文件的顺带看看有没有权限
         */


        /**
         * 日志文件编码
         */
        var charset: Charset = Charsets.UTF_8 //写出编码

        /**
         * 日志文件总目录默认APP外部储存目录/logs 非APP私有目录需要权限
         */
        var filePath: File? = null

        /**
         * 栈深度 -1 =全部 一般就1即可确定日志打印位置
         */
        var stackDeep = 1 //栈深

        /**
         * 附加头信息
         */
        var head: String? = null
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

    companion object {
        /**
         * Instance
         * 单例对象
         */
        val instance: WMSAppLogUtil = WMSAppLogUtil()

    }


    val config = Config()


    private lateinit var mFile: File

    //日志文件携程作用域
    private val mLogCS = CoroutineScope(Dispatchers.IO + SupervisorJob())/* GlobalScope.launch(Dispatchers.IO) {
                       }*//*runBlocking {
        launch(Dispatchers.IO) {
        }
    }*/


    /**
     * 初始化顺带创建个文件测试
     *
     */
    fun initialize(application: Application = WMSAppUtil.getApplicationByReflect()): WMSAppLogUtil {
        this.mApplication = application
        config.filePath = config.filePath ?: mApplication.getExternalFilesDir(null)?.resolve("logs")
        config.head = config.head
            ?: ("****** Log Head ******\n" + "日志路径:${config.filePath?.path}\n" + "****** Log End ******\n")
        config.filePath?.let {
            if (!it.exists()) it.mkdirs()
        }

        mFile = File(
            config.filePath, "${WMSDateUtil.getLocalDate()}-${mApplication.packageName}.txt"
        )

        if (mFile.exists()) {
            mIsInit = mFile.canWrite()
        } else {
            mIsInit = mFile.createNewFile()
            iHead(config.head!!)
        }
        return this
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

    /**
     * I
     *
     * @param string
     * @param ownStackDeep 自身的堆栈深度会被排除,如果是扩展函数或者再次封装会用到
     */
    fun i(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTraceString()
            if (switchConsole) {
                Log.i(tagI, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagI] " + "{$string : $stack}")
            }
        }
    }


    /**
     * V
     *
     * @param string
     * @param ownStackDeep 自身的堆栈深度会被排除,如果是扩展函数或者再次封装会用到
     */
    fun v(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTraceString()
            if (switchConsole) {
                Log.v(tagV, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagV] " + "{$string : $stack}")
            }
        }
    }

    /**
     * D
     *
     * @param string
     * @param ownStackDeep 自身的堆栈深度会被排除,如果是扩展函数或者再次封装会用到
     */
    fun d(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTraceString()
            if (switchConsole) {
                Log.d(tagD, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagD] " + "{$string : $stack}")
            }
        }
    }

    /**
     * W
     *
     * @param string
     * @param ownStackDeep 自身的堆栈深度会被排除,如果是扩展函数或者再次封装会用到
     */
    fun w(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTraceString()
            if (switchConsole) {
                Log.w(tagW, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagW] " + "{$string : $stack}")
            }
        }
    }

    /**
     * E
     *
     * @param string
     * @param ownStackDeep 自身的堆栈深度会被排除,如果是扩展函数或者再次封装会用到
     */
    fun e(string: String = "") {
        config.takeIf { it.switchLog }?.run {
            val stack = getStackTraceString()
            if (switchConsole) {
                Log.e(tagE, "{$string : $stack}")
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tagE] " + "{$string : $stack}")
            }
        }
    }

    /**
     * E
     *
     * @param tag
     * @param string
     * @param e 全部的异常信息
     */
    fun e(tag: String, string: String, e: Throwable) {
        config.takeIf { it.switchLog }?.run {
            val stack = Log.getStackTraceString(e)
            if (switchConsole) {
                Log.e(tag, "{$string}", e)
            }
            if (switchFile) {
                log2File(WMSDateUtil.getLocalDateTimeMilliseconds() + " [$tag] " + "{$string : $stack}")
            }
        }
    }


    /**
     * 日志写到文件
     *
     * @param string
     */
    private fun log2File(string: String) {
        config.takeIf { mIsInit }?.run {
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
     * Get stack trace string
     * 去掉自身栈
     *
     * @param tr
     * @return
     */
    private fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }
        /*var i = 0
        val iterator = tr.iterator()
        while (iterator.hasNext()) {
            i++
            val element = iterator.next()
            if (element.className.contains(className)) {
                break
            }
        }*/
        val stackDeep: Int = if (config.stackDeep == -1) {
            tr.stackTrace.size
        } else 0 + config.stackDeep


        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        if (tr.stackTrace.size >= stackDeep) {
            // 截取范围
            tr.stackTrace = tr.stackTrace.copyOfRange(0, stackDeep)
        }
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    /**
     * 获取栈信息
     * 循环自定义定义
     *
     * @return
     */
    private fun getStackTraceString(): String {

        val stackTrace = Throwable().stackTrace.let {
            /*  it.filter { stackTraceElement ->
                  stackTraceElement.className.contains(className)
              }.toTypedArray()*/
            var i = 1
            val iterator = it.iterator()
            while (iterator.hasNext()) {
                i++
                val element = iterator.next()
                if (element.className.contains(this.className)) {
                    break
                }
            }
            //  Log.e("TAGAAAA", "i:${this.javaClass.`package`?.name} ")
            val stackDeep: Int = if (config.stackDeep == -1) {
                it.size
            } else i + config.stackDeep
            it.copyOfRange(i.coerceIn(0, it.size), stackDeep.coerceIn(0, it.size))
        }/*Throwable().stackTrace.copyOfRange(ownStackDeep, stackDeep).drop(2)//删除自身堆栈
            .run {
                if (config.stackDeep != -1) take(config.stackDeep) else this
            }*/

        val sb = StringBuilder()
        for (element in stackTrace) {
            sb.append("${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})\n")
        }
        return sb.toString()
    }
}


