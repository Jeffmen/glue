package com.camp.glue

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.camp.glue.api.MainApi
import com.camp.glue.api.ProfileApi
import com.camp.glue.manager.ComponentManager

@Route(path = "/app/main")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ARouter.openDebug()
        ARouter.openLog()
        ARouter.init(application)
        ComponentManager.logD = { tag, message ->
            Log.d(tag, message)
        }
        ComponentManager.logE = { tag, message, throwable ->
            Log.e(tag, message, throwable)
        }
        ComponentManager.init()
        ComponentManager.get<ProfileApi>().launch(this)
        ComponentManager.get<MainApi>().launch(this)

        val profileFragment = ComponentManager.get<ProfileApi>().getProfileFragment()
        supportFragmentManager.beginTransaction()
            .add(R.id.content, profileFragment)
            .commit()
    }
}