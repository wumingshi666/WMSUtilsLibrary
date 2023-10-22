package com.wumingshi.wmsutilslibrary.exts

import java.io.File

/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/9/23
 *
 */




/**
 * 删除目录下所有文件 包括子目录 不包含自身目录
 *从下面的扩展函数修改而来
 *
 * public fun File.deleteRecursively(): Boolean = walkBottomUp().fold(true) { res, it -> (it.delete() || !it.exists()) && res }
 * @return
 */
fun File.deleteDirectoryAll(): Boolean {
    return walkBottomUp().fold(true) { res, it ->
        // 检查是否是根目录，如果是根目录不删除
        if (it == this@deleteDirectoryAll) {
            res
        } else {
            (it.delete() || !it.exists()) && res
        }
    }
}