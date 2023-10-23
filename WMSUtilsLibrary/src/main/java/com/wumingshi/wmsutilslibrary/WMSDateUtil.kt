package com.wumingshi.wmsutilslibrary

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/10/24
 *
 */

/**
 * W m s date util
 *
 * 日期时间工具类
 * 临时用后续完善
 * @constructor Create empty W m s date util
 */
object WMSDateUtil {

object Format{
    /********************************************LocalDateTime格式字符串函数************************************/
    const val FORMAT_DATE_1= "yyyy-MM-dd HH:mm:ss" // 时间格式19字符
    const val FORMAT_DATE_2= "yyyy-MM-dd HH:mm" // 时间格式19字符
    const val FORMAT_DATE_3= "yyyy-MM-dd HH:mm:ss.SSS" // 时间格式19字符
    const val FORMAT_DATE_4= "yyyyMMdd HHmmss" // 时间格式15字符
    const val FORMAT_DATE_5= "yyyyMMddHHmmss" // 时间格式14字符
    const val FORMAT_DATE_6= "yyyyMMdd" // 时间格式8字符
    const val FORMAT_DATE_7= "yyMMdd" // 时间格式6字符
    const val FORMAT_DATE_8= "yyyy-MM-dd" // 时间格式10字符
    /********************************************Time格式字符串函数************************************/
    const val FORMAT_TIME_1= "HH:mm" // 时间格式6字符
    const val FORMAT_TIME_2= "HH:mm:ss" // 时间格式6字符

    /*版权声明：本文为CSDN博主「两颗木木三」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
    原文链接：https://blog.csdn.net/qq_36279799/article/details/132586550*/
}


    /**
     * Get local date
     *
     * yyyy-MM-dd
     * @return
     */
    fun getLocalDate(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        dateFormat(Format.FORMAT_DATE_8,LocalDateTime.now())
    } else {
        getDateInstance().format(System.currentTimeMillis())
    }

    fun getLocalDateTimeMilliseconds(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        dateFormat(Format.FORMAT_DATE_3,LocalDateTime.now())
    } else {
        SimpleDateFormat(Format.FORMAT_DATE_3, Locale.getDefault()).format(System.currentTimeMillis())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun dateFormat(pattern: String, time: LocalDateTime): String {
        val df = DateTimeFormatter.ofPattern(pattern)
        return df.format(time)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateFormat(pattern: String, date: LocalDate) : String {
        val df = DateTimeFormatter.ofPattern(pattern)
        return df.format(date)
    }




}