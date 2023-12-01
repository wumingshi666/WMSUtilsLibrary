package com.wumingshi.wmsutilslibrary

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wumingshi.wmsutilslibrary.exts.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import kotlin.random.Random

/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/10/25
 *
 */

@Composable
fun TestRequestPermission() {
    val ca = LocalContext.current as ComponentActivity
    val permissionUtil = WMSPermissionUtil.initialize(ca)
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        arrayOf(
            WMSPermissionUtil.Permission.MANAGE_EXTERNAL_STORAGE,
            WMSPermissionUtil.Permission.CAMERA
        )
    } else {
        arrayOf(
            WMSPermissionUtil.Permission.READ_EXTERNAL_STORAGE,
            WMSPermissionUtil.Permission.WRITE_EXTERNAL_STORAGE,
            WMSPermissionUtil.Permission.CAMERA
        )
    }
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.fromParts(
                "package", ca.application.packageName, null
            )
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", ca.application.packageName, null)
        }
    }
    var mIsGranted by remember {
        mutableStateOf(
            permissionUtil.isGrantedPermissions(
                ca, permissions
            )
        )
    }
    var mShowText by remember { mutableStateOf(if (mIsGranted) "已有权限" else "未授予权限") }
    Box {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "$mShowText=$mIsGranted")
            Button(onClick = {
                WMSPermissionUtil.requestPermissions(permissions) { map ->
                    mIsGranted = map.values.all { it } || WMSPermissionUtil.isGrantedPermissions(
                        permissions
                    )
                    mShowText = if (mIsGranted) "全部同意授权" else "个别或全部拒绝授权"
                    if (!mIsGranted && !WMSPermissionUtil.isDeniedUIPermissions(
                            permissions
                        )
                    ) {
                        mShowText = "个别永久拒绝授权"
                        WMSPermissionUtil.gotoPermissionsSettings(intent = intent) {
                            mIsGranted = WMSPermissionUtil.isGrantedPermissions(permissions)
                            mShowText = if (mIsGranted) "手动授权" else "手动拒绝授权"
                        }
                    }
                }
            }) {
                Text(text = "申请权限")
            }
        }
    }
}

@Composable
fun TestGetRealPath() {
    val ca = LocalContext.current as ComponentActivity
    var intent by remember { mutableStateOf(Intent()) }
    var file: File? by remember { mutableStateOf(null) }
    var showText by remember { mutableStateOf(file?.path ?: "打开APP切后台从别的APP跳转进来") }
    DisposableEffect(Unit) {
        val listener = Consumer<Intent> {
            intent = it
        }
        ca.addOnNewIntentListener(listener)
        onDispose {
            ca.removeOnNewIntentListener(listener)
        }
    }
    LaunchedEffect(intent) {
        launch(Dispatchers.IO) {
            file = intent.data?.let {
                Log.e(TAG, "TestGetRealPath: $it")
                showText = "已有意图点击获取"
                WMSUriUtil.uriToFile(ca, it)
            }
        }
    }
    Box {
        Column {
            Text(text = showText)
            Button(onClick = {
                showText = "路径=${file?.path}"
            }) {
                Text(text = "获取真实路径")
            }
        }
    }
}


@Composable
fun TestDate() {
    Box {
        Button(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WMSDateUtil.dateFormat(WMSDateUtil.Format.FORMAT_DATE_1, LocalDateTime.now()).loge()
                WMSDateUtil.dateFormat(WMSDateUtil.Format.FORMAT_DATE_8, LocalDate.now()).loge()
                WMSDateUtil.dateFormat(WMSDateUtil.Format.FORMAT_DATE_3, LocalDateTime.now()).loge()
            }
            WMSDateUtil.getLocalDateTimeMilliseconds().loge()
            SimpleDateFormat(
                WMSDateUtil.Format.FORMAT_DATE_3, Locale.getDefault()
            ).format(System.currentTimeMillis()).loge()
        }) {
            Text(text = "打印日期")
        }
    }
}


@Composable
fun TestAppLog() {
    val current = LocalContext.current
    val application = current.applicationContext as Application
    var isInit by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        if (!WMSAppLogUtil.instance.isInit) {
            if (!WMSPermissionUtil.isGrantedPermissions(arrayOf(WMSPermissionUtil.Permission.WRITE_EXTERNAL_STORAGE))) return@LaunchedEffect
            WMSAppLogUtil.instance.apply {
                config.apply {
                    tag = ""
                    switchFile = true
                    stackDeep = 1
                    filePath = Environment.getExternalStorageDirectory().resolve("logs")
                        .also { it.mkdirs() }

                }
            }.initialize(application)
            isInit = WMSAppLogUtil.instance.isInit
        }
    }
    Box {
        Column {
            Text(text = if (isInit) WMSAppLogUtil.instance.getFile().path else "未初始化")
            Button(onClick = {
                WMSAppLogUtil.instance.e("日志测试:${('a'..'z').random()}=${Random.nextInt()}")
                "日志扩展函数测试".WMSAppLogUtilLoge()
                666666.WMSAppLogUtilLoge()
                'a'.WMSAppLogUtilLoge()
            }) {
                Text(text = "打印日志")

            }
        }
    }
}

private fun Any.WMSAppLogUtilLoge() = WMSAppLogUtil.instance.e(this.toString())

@Composable
fun TestCrash(nav: NavHostController = rememberNavController()) {
    //在里面会永远false
    var checkedState by remember { mutableStateOf(false) }
    NavHost(navController = nav, startDestination = "CrashHome") {
        //当前页
        composable("CrashHome") {
            val scope = rememberCoroutineScope()
            val current = LocalContext.current
            val application = current.applicationContext as Application
            var isInit by remember { mutableStateOf(false) }
            val logCrash = remember { WMSAppLogUtil() }

            LaunchedEffect(Unit) {
                if (!logCrash.isInit) {
                    if (!WMSPermissionUtil.isGrantedPermissions(arrayOf(WMSPermissionUtil.Permission.WRITE_EXTERNAL_STORAGE))) return@LaunchedEffect
                    logCrash.apply {
                        config.apply {
                            switchFile = true
                            filePath = Environment.getExternalStorageDirectory().resolve("crash")
                                .also { it.mkdirs() }
                        }

                    }.initialize(application)
                }

                if (!WMSCrashHandlerUtil.isInit) {
                    WMSCrashHandlerUtil.apply {
                        config.apply {
                            callBack = { t, e ->
                                logCrash.e(tag, "Uncaught Exception: Thread=${t}", e)
                                checkedState.loge()
                                if (checkedState) {
                                    true
                                } else {
                                    scope.launch(Dispatchers.Main + SupervisorJob()) {
                                        nav.navigate("CrashLog")
                                    }
                                    "main" == t.name
                                }
                            }
                        }
                    }.initialize(application)
                }
                isInit = logCrash.isInit && WMSCrashHandlerUtil.isInit
            }

            Box {
                Column {
                    Text(text = if (isInit) "日志&&全局异常初始化完成" else "未初始化")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            checkedState.loge()
                            scope.launch(Dispatchers.IO + SupervisorJob()) {
                                "abc".substring(6)
                            }
                        }) {
                            Text(text = "触发异常")
                        }

                        Checkbox(
                            checked = checkedState,
                            onCheckedChange = { checkedState = it },
                        )
                        Text("选中触发异常就会崩溃")
                    }
                    Button(onClick = {
                        "abc".substring(6)
                    }) {
                        Text(text = "触发主线程异常")
                    }
                }
            }
        }

        //目标页
        composable("CrashLog") {
            Box {
                BasicText(text = "${WMSCrashHandlerUtil.throwable}")
            }
        }
    }
}