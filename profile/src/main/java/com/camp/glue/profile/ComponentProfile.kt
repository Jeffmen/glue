package com.camp.glue.profile

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.camp.glue.FeatureComponent
import com.camp.glue.annotation.ComponentApiImpl
import com.camp.glue.api.ProfileApi

@ComponentApiImpl
class ComponentProfile : FeatureComponent<ProfileApi> {

    override val api = object : ProfileApi {

        override fun launch(context: Context) {
            Toast.makeText(context, "ProfileApi1", Toast.LENGTH_SHORT).show()
        }

        override fun getProfileFragment(): Fragment {
            return ProfileFragment()
        }

    }
}