package com.camp.glue.manager

import com.camp.glue.FeatureComponent
import com.camp.glue.IComponent
import com.camp.glue.IComponentRoot

object ComponentManager {
    private const val TAG = "PluginManager"

    var logD: ((tag: String, message: String) -> Unit)? = null

    var logE: ((tag: String, message: String, throwable: Throwable) -> Unit)? = null

    private val pluginMap: MutableMap<Class<*>, FeatureComponent<*>> = HashMap()

    fun isApiAvailable(apiClass: Class<IComponent>) = pluginMap[apiClass] != null

    inline fun <reified T : IComponent> get() = safeGet(T::class.java)

    fun <T : IComponent> safeGet(apiClass: Class<T>): T {
        return (pluginMap[apiClass]?.api as? T) ?: EmptyApiProxy.newInstance(apiClass)
    }

    fun init() {}

    fun register(className: String) {
        if (className.isEmpty()) return
        kotlin.runCatching {
            val clazz = Class.forName(className)
            val obj = clazz.getConstructor().newInstance()
            if (obj is IComponentRoot) {
                register(obj)
            } else {
                logD?.invoke(TAG, "className don't implement IPluginRoot: $className")
            }
        }.onFailure {
            logE?.invoke(TAG, "register className don't fail: $className", it)
        }
    }

    @Synchronized
    fun register(pluginRoot: IComponentRoot) {
        pluginRoot.loadInto(pluginMap)
    }
}