package com.camp.glue.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class RegisterPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val isApp = target.plugins.hasPlugin(AppPlugin::class.java)
        if (isApp) {
            println("project(${target.name}) apply glue-auto-register plugin")
            val android = target.extensions.getByType(AppExtension::class.java)
            android.registerTransform(RegisterTransform(target))
        }
    }
}