package com.camp.glue.plugin

interface AutoSetting {
    val autoGroup: String
    val initClassName: String
    val initClassFileName: String
    val initMethodName: String
    val registerMethodName: String
    val scanInterfaceList: List<String>
    val scanPackageName: String
}

object PluginSetting : AutoSetting {
    private const val INIT_CLASS_NAME = "com/camp/glue/manager/ComponentManager"
    private const val INIT_CLASS_FILE_NAME = "$INIT_CLASS_NAME.class"
    private const val INIT_METHOD = "init"
    private const val REGISTER_METHOD = "register"
    private const val SCAN_PACKAGE_NAME = "com/camp/glue/plugin/"
    private val SCAN_INTERFACE_LIST = listOf("com/camp/glue/IComponentRoot")


    override val autoGroup: String
        get() = "plugin"
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