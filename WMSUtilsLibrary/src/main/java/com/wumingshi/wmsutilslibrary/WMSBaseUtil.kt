package com.wumingshi.wmsutilslibrary

import android.app.Application


/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/10/25
 *
 */

/**
 * W m s base util
 *
 * 抽象基础工具类
 * 目前只是省掉了 定义是否初始化的变量 和 toString
 * 和简单规范了子类规则
 *
 * @constructor Create empty W m s base util
 */
abstract class WMSBaseUtil {
    /**
     * Is init
     * 是否初始化
     */
    val isInit: Boolean get() = mIsInit
    protected var mIsInit: Boolean = false

    /**
     * application
     */
    protected lateinit var mApplication: Application
    override fun toString(): String {
        return "{isInit=$isInit,this=${super.toString()}}"
    }

    /**
     * Initialize
     * 只是定义规则无任何内容具体实现自己写并修改 mIsInit变量
     *
     */
    protected open fun initialize() {}

    /**
     * Check initialized
     * 初始化不一定够用也许有延迟的初始化数据,可以这里检查数据
     *
     */
    protected open fun checkInitialized() {}


}

