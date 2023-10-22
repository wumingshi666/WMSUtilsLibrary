package com.wumingshi.wmsutilslibrary.exts




import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author 无名尸 WMS
 * 版本：1.0
 * 创建日期：2023/9/15
 *
 */


/*跳转页面
activity.startContractForResult(ActivityResultContracts.StartActivityForResult(), Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) {
//好像都是返回0
    if (it.resultCode == Activity.RESULT_OK) {
        Log.d(TAG, "get activity result successfully")
    } else {
        Log.w(TAG, "get activity result failed")
    }
}*/

/*val activity1 =  LocalContext.current as MainActivity
val activity = LocalView.current.context as ComponentActivity
WMSPermissionUtil.requestPermission( activity1,Manifest.permission.READ_EXTERNAL_STORAGE){}*/

/*activity.registerForActivityResult(
ActivityResultContracts.RequestMultiplePermissions(),
callback
).launch(permission)*/

private val nextLocalRequestCode = AtomicInteger()
/**
 * 启动一个基于 ActivityResultLauncher 的封装函数，用于处理各种类型的活动结果，如权限请求、启动其他活动等。
 * 基本等同于registerForActivityResult
 *
 * @param contract   ActivityResultContract 实例，用于定义输入和输出类型以及活动的行为。
 * @param input      输入参数，根据不同的 contract 类型而变化，例如权限请求时为权限字符串。
 * @param callback   用于处理活动结果的回调函数，将结果传递给调用者进行处理。
 *
 * 注意：这个函数会自动管理生命周期，并在当前 Activity 销毁时取消注册 ActivityResultLauncher。
 */
fun <I, O> ComponentActivity.startContractForResultWMS(
    contract: ActivityResultContract<I, O>,
    input: I,
    callback: ActivityResultCallback<O>
) {
    val key = "activity_rq_for_result#${nextLocalRequestCode.getAndIncrement()}"
    val registry = activityResultRegistry
    var launcher: ActivityResultLauncher<I>? = null
    val observer = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (Lifecycle.Event.ON_DESTROY == event) {
                launcher?.unregister()
                lifecycle.removeObserver(this)
            }
        }
    }
    lifecycle.addObserver(observer)
    val newCallback = ActivityResultCallback<O> {
        launcher?.unregister()
        lifecycle.removeObserver(observer)
        callback.onActivityResult(it)
    }
    launcher = registry.register(key, contract, newCallback)
    launcher.launch(input)
}