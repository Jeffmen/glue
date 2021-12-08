package com.camp.glue.api

import android.content.Context
import com.camp.glue.IComponent

interface MainApi : IComponent {
    fun launch(context: Context)
}