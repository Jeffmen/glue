package com.camp.glue.compiler

import com.camp.glue.FeatureComponent
import com.camp.glue.GENERATED_NAME_SUFFIX
import com.camp.glue.GENERATED_PACKAGE_NAME
import com.camp.glue.IComponentRoot
import com.camp.glue.annotation.ComponentApiImpl
import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import com.sun.tools.javac.code.Type
import javax.annotation.processing.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
class ComponentProcessor : AbstractProcessor() {

    private val annotation = ComponentApiImpl::class.java

    override fun getSupportedAnnotationTypes(): Set<String> {
        return linkedSetOf(annotation.canonicalName)
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        println("PluginProcessor start!")

        for (element in roundEnvironment.getElementsAnnotatedWith(annotation)) {
            val simpleName = element.simpleName.toString()
            val implClassName = (element as TypeElement).qualifiedName
            val generatedClassName = "${simpleName}${GENERATED_NAME_SUFFIX}"

            (element.interfaces.find {
                it.toString().startsWith(FeatureComponent::class.qualifiedName ?: "")
            } as? Type)?.typeArguments?.let { apiClass ->

                val method = MethodSpec.methodBuilder("loadInto")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ClassName.get("java.lang", "Override"))
                    .addParameter(MutableMap::class.java, "routes")
                    .addStatement("routes.put(${apiClass}.class, new ${implClassName}())")

                val buildClass = TypeSpec.classBuilder(generatedClassName)
                    .addSuperinterface(IComponentRoot::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(method.build())
                    .build()

                JavaFile.builder(GENERATED_PACKAGE_NAME, buildClass)
                    .build()
                    .writeTo(processingEnv.filer)
            }
        }

        println("PluginProcessor end!")
        return true
    }
}
