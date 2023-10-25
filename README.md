Android kotlin 工具库

## 使用方法

**暂时先不发布到jitpack上了**
**直接Releases WMSUtilsLibrary-debug.aar**

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}


//implementation("com.github.wumingshi666:WMSUtilsLibrary:v1.0.1")

```

- [WMSUriUtil](WMSUtilsLibrary/src/main/java/com/wumingshi/wmsutilslibrary/WMSUriUtil.kt) -> [Demo](example/src/main/java/com/wumingshi/wmsutilslibrary/MainActivity.kt#L142)

- 主要用于获取uri文件的真实路径

- ```
  uriToFile
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
       */
  ```

-------------------------------------

- [WMSPermissionUtil](WMSUtilsLibrary/src/main/java/com/wumingshi/wmsutilslibrary/WMSPermissionUtil.kt) -> [Demo](example/src/main/java/com/wumingshi/wmsutilslibrary/MainActivity.kt#L76)
- 权限请求
- ```
  gotoPermissionsSettings ->根据意图调到APP设置或其他设置
  isDeniedUIPermission    ->检查一个权限是否显示权限请求UI
  isDeniedUIPermissions   ->检查多个权限是否显示权限请求UI
  isGrantedPermission     ->检查一个是否有权限
  isGrantedPermissions    ->检查多个是否有权限
  Permission              ->权限常量
  requestPermission       ->请求一个权限
  requestPermissions      ->请求多个权限
  ```


- [WMSAppLogUtil](WMSUtilsLibrary/src/main/java/com/wumingshi/wmsutilslibrary/WMSAppLogUtil.kt) -> [Demo](example/src/main/java/com/wumingshi/wmsutilslibrary/MainActivity.kt#L82)
- 简单日志工具类
- ```
  isInit                  ->是否初始化
  instance                ->单例对象
  config                  ->日志配置项
  d                       ->d级别
  e                       ->e级别
  getFile                 ->获取当前日志文件
  i                       ->i级别
  initialize              ->初始化
  v                       ->
  w                       ->
  ```
  - Config
  - ```
    isInit                  ->是否初始化
    charset                 ->日志文件编码
    filePath                ->日志文件目录
    head                    ->日志文件头信息
    isInit                  ->是否初始化成功(创建文件)
    stackDeep               ->堆栈深度
    switchConsole           ->控制台开关
    switchFile              ->文件开关
    switchHead              ->头信息开关
    switchLog               ->总开关
    tag                     ->TAG
    tagD                    ->
    tagE                    ->
    tagI                    ->
    tagV                    ->
    tagW                    ->
    ```



- [WMSCrashHandlerUtil](WMSUtilsLibrary/src/main/java/com/wumingshi/wmsutilslibrary/WMSCrashHandlerUtil.kt) -> [Demo](example/src/main/java/com/wumingshi/wmsutilslibrary/MainActivity.kt#L84)
- 简单全局异常类 demo里的代码非主线程相关的异常可以不蹦
- ```
  config                  ->
  defaultHandler          ->原始默认的处理程序
  initialize              ->初始化
  isInit                  ->是否初始化
  throwable               ->异常
  throwableString         ->异常全部字符串
  uncaughtException       ->未捕获异常回调
  ```
  - Config
  - ```
    callBack              ->未铺货异常处理回调(可以在这里记录异常)
    tag                   ->异常TAG
    ```
  
  