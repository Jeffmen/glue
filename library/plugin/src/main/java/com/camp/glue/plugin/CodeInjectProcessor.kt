package com.camp.glue.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class InitClassVisitor(
    api: Int,
    cv: ClassVisitor,
    private val initClassName: String,
    private val initMethod: String,
    private val registerMethod: String,
    private val cache: RegisterModel
) : ClassVisitor(api, cv) {

    override fun visitMethod(
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        val mv = cv.visitMethod(access, name, desc, signature, exceptions)
        //注入代码到指定的方法之中
        if (name == initMethod) {
            return InitMethodVisitor(Opcodes.ASM6, mv, access, initClassName, registerMethod, cache)
        }
        return mv
    }
}

class InitMethodVisitor(
    api: Int,
    mv: MethodVisitor,
    private val access: Int,
    private val initClassName: String,
    private val registerMethod: String,
    private val cache: RegisterModel
) : MethodVisitor(api, mv) {

    override fun visitInsn(opcode: Int) {
        if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
            cache.moduleList.forEach {
                it.value.forEach {
                    println("register class:=${it.generateClass}")
                    val className = it.generateClass.replace("/", ".")
                    if (access == 10) {
                        mv.visitLdcInsn(className)
                        mv.visitMethodInsn(
                            Opcodes.INVOKESTATIC,
                            initClassName,
                            registerMethod,
                            "(Ljava/lang/String;)V",
                            false
                        )
                    } else {
                        mv.visitVarInsn(Opcodes.ALOAD, 0)
                        mv.visitLdcInsn(className)
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            initClassName,
                            registerMethod,
                            "(Ljava/lang/String;)V",
                            false
                        )
                    }
                }
            }
        }
        super.visitInsn(opcode)
    }
}