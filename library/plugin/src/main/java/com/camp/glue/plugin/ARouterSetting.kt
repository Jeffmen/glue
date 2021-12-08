package com.camp.glue.plugin

object ARouterSetting : AutoSetting {
    private const val INIT_CLASS_NAME = "com/alibaba/android/arouter/core/LogisticsCenter"
    private const val INIT_CLASS_FILE_NAME = "$INIT_CLASS_NAME.class"
    private const val INIT_METHOD = "loadRouterMap"
    private const val REGISTER_METHOD = "register"
    private const val SCAN_PACKAGE_NAME = "com/alibaba/android/arouter/routes/"
    private const val INTERFACE_PACKAGE_NAME = "com/alibaba/android/arouter/facade/template/"
    private val SCAN_INTERFACE_LIST = listOf(
        "${INTERFACE_PACKAGE_NAME}IRouteRoot",
        "${INTERFACE_PACKAGE_NAME}IInterceptorGroup",
        "${INTERFACE_PACKAGE_NAME}IProviderGroup"
    )

    override val autoGroup: String
        get() = "aRouter"
    override val initClassName: String
        get() = INIT_CLASS_NAME
    override val initClassFileName: String
        get() = INIT_CLASS_FILE_NAME
    override val initMethodName: String
        get() = INIT_METHOD
    override val registerMethodName: String
        get() = REGISTER_METHOD
    override val scanInterfaceList: List<String>
        get() = SCAN_INTERFACE_LIST
    override val scanPackageName: String
        get() = SCAN_PACKAGE_NAME
}