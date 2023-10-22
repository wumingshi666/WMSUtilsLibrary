package com.wumingshi.wmsutilslibrary

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
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


/* activity.intentState?.data?.let {
       *//*  activity.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)*//*

            Log.e(TAG, "intentStateTest结果: ${it}")


            val path1 = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                .toString() + "/fileName"
            Log.e(TAG, "Test结果: ${path1}")

            val docId = DocumentsContract.getDocumentId(it)
            val split =
                docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]
           activity.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, "_id=?", arrayOf(split[1]), null, null)
               ?.use { cursor->
                       if (cursor.moveToFirst()) {
                           do {
                               Log.e(
                                   TAG,
                                   "Test结果: ${cursor.columnCount} ,${cursor.count},${cursor.extras}"
                               )

                               cursor.columnNames.forEach {name->
                                   Log.e(TAG, "Testname: $name")
                                   val columnIndex = cursor.getColumnIndex(name)
                                   if (columnIndex > -1) {
                                       if (name=="xmp"){
                                           val string = cursor.getBlob(columnIndex)
                                           Log.e(TAG, "Test1结果: ${string}")
                                       }else{
                                           val string = cursor.getString(columnIndex)
                                           Log.e(TAG, "Test2结果: ${string}")
                                       }


                                   }

                               }
                               repeat(cursor.columnCount){i->
                                   val string = cursor.getString(i)
                                   Log.e(TAG, "T222est结果: ${string}")
                               }

                               val columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                               if (columnIndex > -1) {

                                   repeat(cursor.columnCount){i->
                                       val string = cursor.getString(i)
                                       Log.e(TAG, "Test结果: ${string}")
                                   }


                               }
                           }while (cursor.moveToNext())
                           *//*while(cursor.moveToNext()) {
                               // 获取各列数据



                               // etc...
                           }*//*
                       }
                   }

            }
*/


/*


private fun getPathFromUri(uri: Uri): String? {

    val cursor = Utils.getApp().contentResolver.query(
        uri, arrayOf<String>("_data"), selection, selectionArgs, null
    )
    if (cursor == null) {
        Log.d("UriUtils", "$uri parse failed(cursor is null). -> $code")
        return null
    }
    try {
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex("_data")
            if (columnIndex > -1) {
                File(cursor.getString(columnIndex))
            } else {
                Log.d(
                    "UriUtils",
                    "$uri parse failed(columnIndex: $columnIndex is wrong). -> $code"
                )
                null
            }
        } else {
            Log.d("UriUtils", "$uri parse failed(moveToFirst return false). -> $code")
            null
        }
    } catch (e: Exception) {
        Log.d("UriUtils", "$uri parse failed. -> $code")
        null
    } finally {
        cursor.close()
    }

    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            // This is for checking Main Memory
            return if ("primary".equals(type, ignoreCase = true)) {
                if (split.size > 1) {
                    Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    Environment.getExternalStorageDirectory().toString() + "/"
                }
                // This is for checking SD Card
            } else {
                "storage" + "/" + docId.replace(":", "/")
            }
        }
    }


    return null
}
*/


/*

fun getRealPath(uri:Uri,context: Context): String? {
    val docId: String = DocumentsContract.getDocumentId(uri)
    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
        .toTypedArray()
    val type = split[0]
    val contentUri: Uri = when (type) {
        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        else -> MediaStore.Files.getContentUri("external")
    }
    val selection = "_id=?"
    val selectionArgs = arrayOf(
        split[1]
    )
    return getDataColumn(context, contentUri, selection, selectionArgs)
}

fun getDataColumn(
    context: Context,
    uri: Uri,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
        column
    )
    try {
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val column_index: Int = cursor.getColumnIndexOrThrow(column)
            val value: String = cursor.getString(column_index)
            return if (value.startsWith("content://") || !value.startsWith("/") && !value.startsWith(
                    "file://"
                )
            ) {
                null
            } else value
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        cursor?.close()
    }
    return null
}
*/


/*
object RealPathUtil {

    fun getRealPath(context: Context, fileUri: Uri): String? {

        return getRealPathFromUriAPI19(context, fileUri)

    }


    */
/**
 * Get a file path from a Uri. This will get the the path for Storage Access
 * Framework Documents, as well as the _data field for the MediaStore and
 * other file-based ContentProviders.
 *
 * @param context The context.
 * @param uri     The Uri to query.
 * @author paulburke
 *//*

    @SuppressLint("NewApi")
    private fun getRealPathFromUriAPI19(
        context: Context,
        uri: Uri
    ): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) { // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(
                uri.scheme,
                ignoreCase = true
            )
        ) { // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    */
/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context       The context.
 * @param uri           The Uri to query.
 * @param selection     (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 *//*

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (e: IllegalArgumentException) {

            return getFilePathFromURI(context, uri)

        } finally {
            cursor?.close()
        }
        return null
    }

    */
/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 *//*

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    */
/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 *//*

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    */
/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 *//*

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    */
/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 *//*

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    //部分文件可能无法读取到正确的URI  所以复制临时文件作为传输
    private fun getFilePathFromURI(
        context: Context,
        contentUri: Uri?
    ): String? { //copy file and send new file path
        val fileName = getFileName(contentUri)
        if (!TextUtils.isEmpty(fileName)) {
            val copyFile = File("${context.filesDir}${File.separator}${fileName}")
            copy(context, contentUri, copyFile)
            return copyFile.absolutePath
        }
        return null
    }

    private fun getFileName(uri: Uri?): String? {
        if (uri == null) return null
        var fileName: String? = null
        val path = uri.path ?: return null
        val cut = path.lastIndexOf('/')
        if (cut != -1) {
            fileName = path.substring(cut + 1)
        }
        return fileName
    }

    private fun copy(
        context: Context,
        srcUri: Uri?,
        dstFile: File?
    ) {
        try {
            val inputStream = context.contentResolver.openInputStream(srcUri!!)
                ?: return
            val outputStream: OutputStream = FileOutputStream(dstFile)
            copyFile(inputStream, outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        val BUFFER_SIZE = 1024 * 2
        val buffer = ByteArray(BUFFER_SIZE)
        val `in` = BufferedInputStream(inputStream, BUFFER_SIZE)
        val out = BufferedOutputStream(outputStream, BUFFER_SIZE)
        var n: Int
        try {
            while (`in`.read(buffer, 0, BUFFER_SIZE).also { n = it } != -1) {
                out.write(buffer, 0, n)
            }
            out.flush()
        } finally {
            try {
                out.close()
            } catch (e: IOException) {

            }
            try {
                `in`.close()
            } catch (e: IOException) {

            }
        }
    }

}*/
