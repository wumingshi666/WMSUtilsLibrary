package com.wumingshi.wmsutilslibrary

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


/**
 * 主要用于uri文件获取真实路径
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/9/24
 *
 */
object WMSUriUtil {
    /**
     * copy Size限制
     */
    var copyMaxSize: Long = 100 * 1024 * 1024 // 100MB

    private object Error {
        const val FILE_NOT_EXIST = "文件不存在"
        const val FILE_IS_NULL = "获取失败,结果是null"
        const val FILE_NOT_EXIST_AND_NULL = "函数结束,文件不存在而且所有结果是null"
    }

    /**
     * Uri 转 file 如果无法获取到就写到缓存
     *
     * 注意如果Compose组件重组了几次就会几次copy 需要判断一下uri和之前的是否一样
     * 或者用别的方式来让他只copy一次
     *
     * 支持如下
     * 华为content://media/external/file/102248
     * 模拟器 自带文件管理器 DocumentsUri
     * Android 10/14
     * content://com.android.providers.media.documents/document/image%3A26
     * content://com.android.externalstorage.documents/document/primary%3ADCIM%2Fabc.png
     * content://com.android.providers.media.documents/document/video%3A27
     * content:// ******** ??????: DCIM/abc.txt = Environment.getExternalStorageDirectory() + DCIM/abc.txt
     * content://com.android.providers.downloads.documents/document/msf%3A1000000034
     * content://com.android.providers.downloads.documents/raw:/storage/emulated/0/Download/abc.txt
     *
     * APP
     *
     * IDM+
     * content://idm.internet.download.manager.plus.provider/external_files/IDMP
     *
     * QQ打开的 APP私有路径  后续可以区分版本是否拷贝 还是获取真实路径
     * content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Android/data/
     * 下面是的发出去的图 再打开
     * content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Pictures/nim/5685780e8be51144b668319399cc77d2.jpg
     *
     *
     * @param context 上下文
     * @param uri  文件的uri
     * @param isCopy 如果最终没获取到是否copy 为了防止占用内存有大小限制默认 100mb/200mb
     * @return 真实File对象或null
     * @throws Exception 如果没有 Manifest.permission.WRITE_EXTERNAL_STORAGE /MANAGE_EXTERNAL_STORAGE 权限
     *
     * @requiresPermission Manifest.permission.WRITE_EXTERNAL_STORAGE
     *
     */
    fun uriToFile(
        context: Context , //=WMSAppUtil.getApplicationByReflect()
        uri: Uri,
        isCopy: Boolean = true
    ): File? {
        /*Log.e("TAG", "原始uriToFile: ${uri}")
        Log.e("TAG", "原始uriToFilePath: ${uri.path}")*/
        when (uri.scheme) {
            ContentResolver.SCHEME_FILE -> return uri.toFile()
            ContentResolver.SCHEME_CONTENT -> {
                val path =
                    getRealPathForExternalStorageUri(uri, context)
                        ?: getRealPathFromDocumentsIdUri(uri, context)
                        ?: getRealPathFromDocumentsDataUri(uri, context)
                        ?: getPathFromAppUri(uri)

                //可以增加测试路径是佛真的有文件 否则返回null
                return path?.let {
                    File(it).takeIf { file ->
                        file.exists().also { exists ->
                            if (!exists) {
                                Log.e(
                                    "TAG",
                                    Error.FILE_NOT_EXIST_AND_NULL + "uriToFile = Code:${4000}"
                                )
                            }
                        }
                    }
                }   // 以上方法获取的path 都 null 或 文件不存在就copy到缓存
                    ?: run {
                        if (isCopy) {
                            copyUriToCache(uri, context)
                        } else {
                            null
                        }
                    }

            }

            else -> {
                Log.e("TAG", "其他分支: ${uri}" + "uriToFile = Code:${4001}")
                return null
            }
        }

    }

    /**
     * 简单截断拼接字符串 返回path
     *
     * 支持如下
     * content://com.android.externalstorage.documents/document/primary%3ADCIM%2Fabc.txt
     * content:// ******** 15EB-0E1A:DCIM/abc.txt
     * content:// ******** ??????: DCIM/abc.txt = Environment.getExternalStorageDirectory() + DCIM/abc.txt
     *
     */
    private fun getRealPathForExternalStorageUri(uri: Uri, context: Context): String? {
        var code: Int = 4000
        var file: File?
        if (DocumentsContract.isDocumentUri(
                context, uri
            ) && uri.authority == "com.android.externalstorage.documents"
        ) {
            code++
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            code = 4002
            file = when (type) {
                // docId是primary类型
                "primary" -> {
                    Environment.getExternalStorageDirectory().resolve(split[1])
                }

                else -> {
                    code = 4003
                    /*val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.getExternalVolumeNames(WMSAppUtil.getApplicationByReflect())
                    } */
                    /* MediaStore.VOLUME_EXTERNAL_PRIMARY
                     Log.e("TAG", "getPathFromAppUri: ${path}", )*/
                    /*val paths = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.getExternalVolumeNames(context)
                    }
                    */
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && MediaStore.getExternalVolumeNames(
                            context
                        ).any { it.equals(type, ignoreCase = true) }
                    ) {//判断注意忽略大小写
                        //临时解决办法拼接获取SDCARD  /*15EB-0E1A:DCIM/abc.txt*/
                        Environment.getExternalStorageDirectory().parentFile?.parentFile?.resolve(
                            type
                        )?.resolve(split[1])
                    } else {
                        Environment.getExternalStorageDirectory().resolve(split[1])
                    }


                }
            }
            return file?.let {
                if (it.exists()) {
                    it.path
                } else {
                    Log.e(
                        "TAG", Error.FILE_NOT_EXIST + "getRealPathUriToString = Code:${code}"
                    )
                    null
                }
            } ?: run {
                Log.e("TAG", Error.FILE_IS_NULL + "getRealPathUriToString = Code:${code}")
                null
            }

        }
        return null

    }

    /**
     * 用DocumentsData 获取Uri 真实路径
     *
     * 支持如下
     * 华为的文件管理器跳转APP能获取到 content://media/external/file/102248
     *
     * @param uri
     * @param context
     * @return
     */
    private fun getRealPathFromDocumentsDataUri(uri: Uri, context: Context): String? {
        return uriContentResolverQuery(
            context,
            uri,
            null,
            null,
            arrayOf(MediaStore.Images.Media.DATA),
            column = MediaStore.Images.Media.DATA,
            valueGetter = { cursor, columnIndex -> cursor.getString(columnIndex) })
    }

    /**
     * 用DocumentsId 获取Uri 真实路径
     *
     * 支持如下
     * content://com.android.providers.downloads.documents/document/msf%3A1000000034
     * content://com.android.providers.media.documents/document/image%3A28
     * content://com.android.providers.downloads.documents/raw:/storage/emulated/0/Download/abc.txt
     *
     * @param uri
     * @param context
     * @return 路径 = string
     */
    private fun getRealPathFromDocumentsIdUri(
        uri: Uri, context: Context
    ): String? {
        var code: Int = 4000
        var file: File? = null
        if (DocumentsContract.isDocumentUri(context, uri)) {
            code++
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
            when {//这里可能有大小写之分，不行可以全转成小写
                //这里简单处理downloads的 raw
                // docId是raw:/storage/emulated/0/Download/abc.txt
                uri.authority == "com.android.providers.downloads.documents" && type == "raw" -> {
                    return split[1]
                }


                uri.authority == "com.android.providers.downloads.documents" || uri.authority == "com.android.providers.media.documents" -> {
                    code = 4002
                    val contentUri = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> MediaStore.Files.getContentUri("external")
                    }
                    contentUri?.let {
                        code = 4003
                        val path = uriContentResolverQuery(context,
                            contentUri,
                            "_id=?",
                            arrayOf(split[1]),
                            valueGetter = { cursor, columnIndex ->
                                cursor.getString(columnIndex)
                            })
                        file = path?.let { File(it) }
                    }
                }

                else -> {
                    code = 4004
                    //临时处理防止其他情况
                    val path = docId.substringAfter(":")
                    file = Environment.getExternalStorageDirectory().resolve(path)
                }

            }

            return file?.let {
                if (it.exists()) {
                    it.path
                } else {
                    Log.e(
                        "TAG", Error.FILE_NOT_EXIST + "getRealPathFromDocumentsUri = Code:${code}"
                    )
                    null
                }
            } ?: run {
                Log.e("TAG", Error.FILE_IS_NULL + "getRealPathFromDocumentsUri = Code:${code}")
                null
            }
        }

        return null
    }

    /**
     * 里面的暂时没用，但是保持原因不动 以后再优化 或参考
     *
     * @param uri
     * @param selection
     * @param selectionArgs
     * @param context
     * @return
     */
    private fun getPathFileFromUri(
        uri: Uri, selection: String, selectionArgs: Array<String>, context: Context
    ): String? {
        when (uri.authority) {
            "com.google.android.apps.photos.content" -> {
                if (uri.lastPathSegment != null) {
                    return uri.lastPathSegment
                }
            }

            "com.tencent.mtt.fileprovider" -> {
                val path = uri.path
                val fileDir = Environment.getExternalStorageDirectory()
                if (path != null) {
                    return File(fileDir, path.substring("/QQBrowser".length, path.length)).path
                }
            }

            "com.huawei.hidisk.fileprovider" -> {
                val path = uri.path
                if (path != null) {
                    return File(path.replace("/root", "")).path
                }
            }

        }
        return uriContentResolverQuery(context,
            uri,
            selection,
            selectionArgs,
            valueGetter = { cursor, columnIndex ->
                cursor.getString(columnIndex)
            })
    }

    /**
     * 从其他APP里打开uri的获取路径
     * 暂时未增加太多APP
     *
     * IDM+
     * content://idm.internet.download.manager.plus.provider/external_files/IDMP
     *
     * QQ打开的 APP私有路径  后续可以区分版本是否拷贝 还是获取真实路径
     * content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Android/data/
     * 下面是的发出去的图 再打开
     * content://com.tencent.mobileqq.fileprovider/external_files/storage/emulated/0/Pictures/nim/5685780e8be51144b668319399cc77d2.jpg
     *
     *
     * @param uri
     * @return
     */
    private fun getPathFromAppUri(uri: Uri): String? {
        val externals = arrayOf(
            "/external/",
            "/external_path/",
            //content://idm.internet.download.manager.plus.provider/external_files/IDMP
            "/external_files/"
        )
        val externalStorageDir = Environment.getExternalStorageDirectory().absolutePath
        uri.path?.let { path ->
            externals.firstOrNull { path.startsWith(it) }
                ?.let { path.replace(it, "/") }
                ?.let {
                    return if (it.startsWith(externalStorageDir)) it else externalStorageDir + it
                }
        }
        Log.e("TAG", Error.FILE_IS_NULL + "getPathFromAppUri = Code:${4000}")
        return null
    }

    /**
     * 获取 uri 文件大小
     *
     * @param uri
     * @param context
     * @return byte = Long
     */
    fun getUriSize(uri: Uri, context: Context): Long {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            return uriContentResolverQuery(context,
                uri,
                column = MediaStore.Images.Media.SIZE,
                valueGetter = { cursor, i ->
                    cursor.getLong(i)
                }) ?: -1L
        }
        return -1
    }


    /**
     * 获取 uri 文件名
     *
     * @param uri
     * @param context
     * @return name = string
     */
    fun getUriName(uri: Uri, context: Context): String? {
        var result: String? = null
        uri.scheme?.let {
            if (it == ContentResolver.SCHEME_CONTENT) {
                result = uriContentResolverQuery(context,
                    uri,
                    column = MediaStore.Images.Media.DISPLAY_NAME,
                    valueGetter = { cursor, i ->
                        cursor.getString(i)
                    })

            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut.plus(1))
            }
        }
        return result
    }


    /*    可以部分获取真实的路径
        * 华为自带的文件管理器跳转APP能获取到 content://media/external/file/102248*/
    /**
     * 从类似content://的 uri 查询数据 泛型
     *
     * valueGetter = { cursor, i -> cursor.getString(i) })
     *
     * @param context
     * @param uri
     * @return 如果获取失败返回null否则真实路径
     */
    fun <T> uriContentResolverQuery(
        context: Context,
        uri: Uri,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        projection: Array<String>? = null, /*=arrayOf(MediaStore.Images.Media.DATA)*/
        column: String = MediaStore.Images.Media.DATA,
        valueGetter: (cursor: Cursor, columnIndex: Int) -> T
    ): T? {
        context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            ?.use { cursor ->
                val columnIndex = cursor.getColumnIndex(column)
                if (columnIndex > -1) {
                    cursor.moveToFirst()
                    return valueGetter(cursor, columnIndex)
                }
            }
        Log.e("TAG", Error.FILE_IS_NULL + "uriContentResolverQuery = Code:${4000}")
        return null
    }

    /**
     * 把打开的Uri 文件写入到data/data APP缓存
     *
     * 文件大于 copyMaxSize:${copyMaxSize} 直接返回null
     *
     * 后续可能会增加如果存在就不copy 或者必须拷贝 因为可能没copy完整 需要覆盖
     * @param uri
     * @param context
     * @return File对象
     */
    private fun copyUriToCache(uri: Uri, context: Context): File? {
        if (getUriSize(uri, context) > copyMaxSize) {
            Log.e(
                "TAG",
                "copyUriToCache uri 文件大于 copyMaxSize:$copyMaxSize 尝试copyUriToExternalFiles 或 直接返回null"
            )
            copyUriToExternalFiles(uri, context)
            return null
        }

        Log.e("TAG", "copyUriToCache 执行 Uri copy 到缓存")

        try {
            // 打开 Uri 的输入流
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                // 创建缓存目录
                val cacheDir =
                    context.cacheDir.resolve(uri.toString().hashCode().toString()).also { path ->
                        !path.exists() && path.mkdirs()
                    }
                // 在缓存目录中创建一个临时文件
                val tempFile = File(
                    cacheDir,
                    getUriName(uri, context)/*uri.path  name*/ ?: uri.lastPathSegment
                    ?: uri.toString().hashCode().toString()
                )
                // 使用 OutputStream 将内容从输入流复制到临时文件
                val outputStream = FileOutputStream(tempFile)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                // 返回复制后的文件
                return tempFile
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null


    }


    /**
     * 把打开的Uri 文件写入到data/data APP缓存
     *
     * 文件大于 copyMaxSize*2:${copyMaxSize*2} 直接返回null
     *
     * 后续可能会增加如果存在就不copy 或者必须拷贝 因为可能没copy完整 需要覆盖
     * @param uri
     * @param context
     * @return File对象
     */
    private fun copyUriToExternalFiles(uri: Uri, context: Context): File? {
        if (getUriSize(uri, context) > copyMaxSize * 2) {
            Log.e(
                "TAG",
                "copyUriToExternalFiles uri 文件大于 copyMaxSize * 2:${copyMaxSize * 2} 直接返回null"
            )
            return null
        }

        Log.e("TAG", "copyUriToExternalFiles 执行 Uri copy 到缓存")

        try {
            // 打开 Uri 的输入流
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                // 创建缓存目录
                val cacheDir =/*context.getExternalFilesDir(null)*/
                    File(Environment.getExternalStorageState()).resolve(
                        uri.toString().hashCode().toString()
                    ).also { path ->
                        !path.exists() && path.mkdirs()
                    }

                // 在缓存目录中创建一个临时文件
                val tempFile = File(
                    cacheDir,
                    getUriName(uri, context)/*uri.path  name*/ ?: uri.lastPathSegment
                    ?: uri.toString().hashCode().toString()
                )
                // 使用 OutputStream 将内容从输入流复制到临时文件
                val outputStream = FileOutputStream(tempFile)
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                // 返回复制后的文件
                return tempFile
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null


    }
}
