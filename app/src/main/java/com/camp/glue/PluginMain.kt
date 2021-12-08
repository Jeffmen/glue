package com.camp.glue

import android.content.Context
import android.widget.Toast
import com.camp.glue.annotation.ComponentApiImpl
import com.camp.glue.api.MainApi

@ComponentApiImpl
class PluginMain : FeatureComponent<MainApi> {

    override val api = object : MainApi {

        override fun launch(context: Context) {
            Toast.makeText(context, "MainApi", Toast.LENGTH_SHORT).show()
        }
    }
}