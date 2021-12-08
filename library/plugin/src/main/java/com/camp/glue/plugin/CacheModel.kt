package com.camp.glue.plugin

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CacheModel(
    val list: MutableList<RegisterModel> = mutableListOf()
) {
    fun findRegisterModel(autoGroup: String): RegisterModel {
        return list.firstOrNull { it.autoGroup == autoGroup }
            ?: RegisterModel(autoGroup).apply {
                list.add(this)
            }
    }

    fun clearModule(moduleName: String){
        list.forEach {
            it.moduleList.remove(moduleName)
        }
    }
}

@JsonClass(generateAdapter = true)
data class RegisterModel(
    val autoGroup: String,
    var initClassName: String? = null,
    val moduleList: MutableMap<String, MutableList<FileSetting>> = mutableMapOf()
)

@JsonClass(generateAdapter = true)
data class FileSetting(
    val filePath: String,
    var generateClass: String
)
