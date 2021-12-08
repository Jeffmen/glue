package com.camp.glue.profile

import android.content.Context
import android.util.Log
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.facade.service.PretreatmentService

@Route(path = "/glue/pretreatment")
class PretreatmentServiceImpl : PretreatmentService {
    override fun init(context: Context?) {
    }

    override fun onPretreatment(context: Context?, postcard: Postcard?): Boolean {
        Log.d("PretreatmentServiceImpl", "onPretreatment")
        return true
    }

}