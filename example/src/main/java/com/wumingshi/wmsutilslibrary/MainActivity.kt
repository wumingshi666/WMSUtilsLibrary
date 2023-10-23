package com.wumingshi.wmsutilslibrary

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.util.Consumer
import com.wumingshi.wmsutilslibrary.ui.theme.WMSUtilsLibraryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

const val TAG = "TAG"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WMSUtilsLibraryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        TestRequestPermission()
                        Divider()
                        TestGetRealPath()
                        Divider()
                    }

                }
            }
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }
}


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