package com.camp.glue.api

import android.content.Context
import androidx.fragment.app.Fragment
import com.camp.glue.IComponent

interface ProfileApi: IComponent {

    fun launch(context: Context)

    fun getProfileFragment(): Fragment
}