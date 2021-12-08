package com.camp.glue.manager

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.jvm.Throws
import kotlin.reflect.KClass

@kotlin.annotation.Target
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SpecifyClassValue(val returnValue: KClass<*>)

@kotlin.annotation.Target
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SpecifyIntegerValue(val returnValue: Int = 0)

@kotlin.annotation.Target
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SpecifyStringValue(val returnValue: String = "")

@kotlin.annotation.Target
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class SpecifyBooleanValue(val returnValue: Boolean = false)

internal class EmptyApiProxy : InvocationHandler {
    @Throws(Throwable::class)
    override operator fun invoke(proxy: Any?, method: Method, objects: Array<Any?>?): Any? {
        return method.getAnnotation(SpecifyClassValue::class.java)?.returnValue
            ?: method.getAnnotation(SpecifyIntegerValue::class.java)?.returnValue
            ?: method.getAnnotation(SpecifyStringValue::class.java)?.returnValue
            ?: method.getAnnotation(SpecifyBooleanValue::class.java)?.returnValue
            ?: defaultValueByType(method.returnType)
    }

    private fun defaultValueByType(type: Class<*>): Any? {
        return when (type) {
            Boolean::class.javaPrimitiveType -> false
            Int::class.javaPrimitiveType -> 0
            Short::class.javaPrimitiveType -> 0.toShort()
            Char::class.javaPrimitiveType -> 0.toChar()
            Byte::class.javaPrimitiveType -> 0.toByte()
            Long::class.javaPrimitiveType -> 0.toLong()
            Float::class.javaPrimitiveType -> 0.toFloat()
            Double::class.javaPrimitiveType -> 0.toDouble()
            else -> {
                kotlin.runCatching {
                    Class.forName(type.canonicalName).getConstructor().newInstance()
                }.getOrNull()
            }
        }
    }

    companion object {
        fun <T> newInstance(clazz: Class<T>): T {
            return Proxy.newProxyInstance(
                clazz.classLoader,
                arrayOf<Class<*>>(clazz),
                EmptyApiProxy()
            ) as T
        }
    }
}