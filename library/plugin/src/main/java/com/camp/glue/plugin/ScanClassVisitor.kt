package com.camp.glue.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ScanClassVisitor(
    api: Int,
    cv: ClassVisitor,
    private val moduleName: String,
    private val absolutePath: String,
    private val autoSetting: AutoSetting,
    private val cache: RegisterModel
) : ClassVisitor(api, cv) {

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor {
        println("visitAnnotation descriptor:=${descriptor}")
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        println("visitMethod name:=${name}")
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }

    private fun isFlag(access: Int, flag: Int) = (access and flag) == flag

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        //抽象类、接口、非public等类无法调用其无参构造方法
        if (isFlag(access, Opcodes.ACC_ABSTRACT) ||
            isFlag(access, Opcodes.ACC_INTERFACE) ||
            !isFlag(access, Opcodes.ACC_PUBLIC)
        ) {
            return
        }
        autoSetting.scanInterfaceList.forEach { scanInterface ->
            interfaces?.forEach {
                if (it == scanInterface) {
                    cache.moduleList[moduleName]?.add(FileSetting(absolutePath, name)) ?: kotlin.run {
                        cache.moduleList[moduleName] = mutableListOf(FileSetting(absolutePath, name))
                    }
                }
            }
        }
    }
}