package com.camp.glue.profile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter

const val PATH = "/profile/activity"

@Route(path = PATH)
class ARouterActivity : AppCompatActivity() {

    @Autowired(name = "text")
    @JvmField
    var text: String = "origin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        setContentView(R.layout.fragment_profile)
        findViewById<TextView>(R.id.tv_text).text = text
        findViewById<TextView>(R.id.tv_text).setOnClickListener {
            ARouter.getInstance().build("/app/main")
                .navigation(it.context)
        }
    }
}