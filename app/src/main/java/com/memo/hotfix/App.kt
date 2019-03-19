package com.memo.hotfix

import android.content.Context
import android.support.multidex.MultiDex
import android.support.multidex.MultiDexApplication
import com.memo.hotfix.utils.HotfixUtils

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-18 11:48
 */
class App:MultiDexApplication(){
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        HotfixUtils.loadFixedDex(this)
    }
}