package com.camp.glue.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.AndroidProject.FD_INTERMEDIATES
import com.squareup.moshi.Moshi
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.*
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

const val CACHE_FILE_NAME = "register-cache.json"
const val TAG = "glue-auto-register"

class RegisterTransform(private val project: Project) : Transform() {
    private val listSetting = listOf(PluginSetting, ARouterSetting)
    private val moshi = Moshi.Builder().build()
    private val cache = getCache()

    override fun getName() = TAG

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    override fun isIncremental() = true

    override fun getScopes(): MutableSet<QualifiedContent.ScopeType> =
        TransformManager.SCOPE_FULL_PROJECT

    override fun transform(transformInvocation: TransformInvocation) {
        println("$TAG start ...")
        super.transform(transformInvocation)
        val start = System.currentTimeMillis()
        val outputProvider = transformInvocation.outputProvider
        val isIncremental = transformInvocation.isIncremental

        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        println("$TAG isIncremental:=$isIncremental")

        transformInvocation.inputs.forEach { input ->
            input.jarInputs.forEach {
                // 重名名输出文件,因为可能同名,会覆盖
                val hexName = DigestUtils.md5Hex(it.file.absolutePath)
                var destName = it.name.replace(".jar", "")
                destName = destName + "_" + hexName
                val destFile = getDestFile(destName, it, outputProvider)
                if (isIncremental) {
                    if (it.status != Status.NOTCHANGED) {
                        cache.clearModule(it.name)
                        if (it.status == Status.REMOVED) {
                            if (destFile.exists()) {
                                FileUtils.forceDelete(destFile)
                            }
                        } else {
                            listSetting.forEach { autoSetting ->
                                val registerModel = cache.findRegisterModel(autoSetting.autoGroup)
                                findClassInJar(
                                    it.name,
                                    it,
                                    destFile,
                                    autoSetting,
                                    registerModel
                                )
                            }
                            FileUtils.copyFile(it.file, destFile)
                        }
                    }
                } else {
                    cache.clearModule(it.name)
                    listSetting.forEach { autoSetting ->
                        val registerModel = cache.findRegisterModel(autoSetting.autoGroup)
                        findClassInJar(
                            it.name,
                            it,
                            destFile,
                            autoSetting,
                            registerModel
                        )
                    }
                    FileUtils.copyFile(it.file, destFile)
                }
            }
            input.directoryInputs.forEach {
                val destFile = getDestFile(it.name, it, outputProvider)
                if (isIncremental) {
                    it.changedFiles.forEach { (file, status) ->
                        if (status != Status.NOTCHANGED) {
                            cache.clearModule(it.name)
                            if (status == Status.REMOVED) {
                                if (destFile.exists()) {
                                    FileUtils.forceDelete(destFile)
                                }
                            } else {
                                listSetting.forEach { autoSetting ->
                                    val registerModel =
                                        cache.findRegisterModel(autoSetting.autoGroup)
                                    findClassInClass(
                                        it.name,
                                        file,
                                        autoSetting,
                                        registerModel
                                    )
                                }
                                FileUtils.copyDirectory(it.file, destFile)
                            }
                        }
                    }
                } else {
                    cache.clearModule(it.name)
                    listSetting.forEach { autoSetting ->
                        val registerModel = cache.findRegisterModel(autoSetting.autoGroup)
                        findClassInFolder(
                            it.name,
                            it.file,
                            autoSetting,
                            registerModel
                        )
                    }
                    FileUtils.copyDirectory(it.file, destFile)
                }
            }
        }
        listSetting.forEach { autoSetting ->
            val registerModel = cache.findRegisterModel(autoSetting.autoGroup)
            registerModel.initClassName?.let {
                println("$TAG init class path:=$it")
                val jarFile = File(it)
                val optJar = File(jarFile.parent, jarFile.name + ".opt")
                if (optJar.exists()) {
                    optJar.delete()
                }
                injectCodeToClass(
                    jarFile,
                    optJar,
                    autoSetting,
                    registerModel
                )
                if (jarFile.exists()) {
                    jarFile.delete()
                }
                optJar.renameTo(jarFile)
            }
        }
        writeJsonFile()
        println("$TAG cost time:=${System.currentTimeMillis() - start}ms")
    }

    private fun findClassInJar(
        moduleName: String,
        jarInput: JarInput,
        destFile: File,
        autoSetting: AutoSetting,
        cache: RegisterModel
    ) {
        val absolutePath = jarInput.file.absolutePath
        val jarFile = JarFile(jarInput.file)
        jarFile.entries().iterator().forEach {
            if (it.name == autoSetting.initClassFileName) {
                cache.initClassName = destFile.absolutePath
            } else if (it.name.startsWith(autoSetting.scanPackageName)) {
                scanClassInFile(
                    moduleName,
                    absolutePath,
                    jarFile.getInputStream(it),
                    autoSetting,
                    cache
                )
            }
        }
        jarFile.close()
    }

    private fun findClassInFolder(
        moduleName: String,
        file: File,
        autoSetting: AutoSetting,
        cache: RegisterModel
    ) {
        if (file.isFile) {
            findClassInClass(moduleName, file, autoSetting, cache)
        } else {
            file.listFiles()?.forEach {
                findClassInFolder(moduleName, it, autoSetting, cache)
            }
        }
    }

    private fun findClassInClass(
        moduleName: String,
        file: File,
        autoSetting: AutoSetting,
        cache: RegisterModel
    ) {
        val absolutePath = file.absolutePath
        if (absolutePath.contains(autoSetting.scanPackageName)) {
            scanClassInFile(moduleName, absolutePath, file.inputStream(), autoSetting, cache)
        }
    }

    private fun scanClassInFile(
        moduleName: String,
        absolutePath: String,
        inputStream: InputStream,
        autoSetting: AutoSetting,
        cache: RegisterModel
    ) {
        val cr = ClassReader(inputStream)
        val cw = ClassWriter(cr, 0)
        val cv = ScanClassVisitor(
            Opcodes.ASM7, cw, moduleName,
            absolutePath, autoSetting, cache
        )
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        inputStream.close()
    }

    private fun injectCodeToClass(
        jarFile: File,
        tempJar: File,
        autoSetting: AutoSetting,
        cache: RegisterModel
    ) {
        val file = JarFile(jarFile)
        val jarOutputStream = JarOutputStream(FileOutputStream(tempJar))

        file.entries().iterator().forEach {
            val zipEntry = ZipEntry(it.name)
            val inputStream = file.getInputStream(it)
            jarOutputStream.putNextEntry(zipEntry)
            if (it.name == autoSetting.initClassFileName) {
                val cr = ClassReader(inputStream)
                val cw = ClassWriter(cr, 0)
                val cv = InitClassVisitor(
                    Opcodes.ASM7,
                    cw,
                    autoSetting.initClassName,
                    autoSetting.initMethodName,
                    autoSetting.registerMethodName,
                    cache
                )
                cr.accept(cv, ClassReader.EXPAND_FRAMES)
                jarOutputStream.write(cw.toByteArray())
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
    }

    private fun getDestFile(
        name: String,
        input: QualifiedContent,
        output: TransformOutputProvider
    ): File {
        val format = if (input is JarInput) {
            Format.JAR
        } else {
            Format.DIRECTORY
        }
        return output.getContentLocation(name, input.contentTypes, input.scopes, format)
    }

    private fun getCache(): CacheModel {
        val cacheFile = getCacheFile()
        println("$TAG cache file:=${cacheFile.absolutePath}")
        return if (cacheFile.exists()) {
            readJsonFile(cacheFile)
        } else {
            CacheModel()
        }
    }

    private fun getCacheFile(): File {
        val baseDirFile = File(getCacheFileDir())
        if (!baseDirFile.isDirectory) {
            baseDirFile.mkdirs()
        }
        return File(baseDirFile, CACHE_FILE_NAME)
    }

    private fun writeJsonFile() {
        kotlin.runCatching {
            val json = moshi.adapter(CacheModel::class.java).toJson(cache)
            getCacheFile().writeText(json)
        }.onFailure {
            println("$TAG writeJsonFile fail:=$it")
        }
    }

    private fun readJsonFile(cacheFile: File): CacheModel {
        return kotlin.runCatching {
            val json = cacheFile.readText()
            moshi.adapter(CacheModel::class.java).fromJson(json)
        }.onFailure {
            println("$TAG readJsonFile fail:=$it")
        }.getOrNull() ?: CacheModel()
    }

    private fun getCacheFileDir(): String {
        return project.buildDir.absolutePath +
                File.separator + FD_INTERMEDIATES +
                File.separator + TAG + File.separator
    }
}