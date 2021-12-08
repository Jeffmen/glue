package com.camp.glue.profile

import android.content.Context
import android.util.Log
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.facade.annotation.Interceptor
import com.alibaba.android.arouter.facade.callback.InterceptorCallback
import com.alibaba.android.arouter.facade.template.IInterceptor

@Interceptor(priority = 1)
class TestInterceptor: IInterceptor {
    override fun init(context: Context?) {
    }

    override fun process(postcard: Postcard?, callback: InterceptorCallback?) {
        Log.d("TestInterceptor", "process")
        callback?.onContinue(postcard)
    }
}