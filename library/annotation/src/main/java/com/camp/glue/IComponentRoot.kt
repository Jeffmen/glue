package com.camp.glue

const val GENERATED_PACKAGE_NAME = "com.camp.glue.plugin"
const val GENERATED_NAME_SUFFIX = "\$Load"

interface IComponentRoot {

    fun loadInto(routes: MutableMap<Class<*>, FeatureComponent<*>>)

}