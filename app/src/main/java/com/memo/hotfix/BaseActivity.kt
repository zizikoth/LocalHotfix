package com.memo.hotfix

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-18 11:53
 */
abstract class BaseActivity : AppCompatActivity() {

    protected val mActivity: Activity by lazy { this }

    protected val mContext: Context by lazy { this }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bindLayoutRes())
        initialize()
    }

    /**
     * 绑定布局id
     */
    @LayoutRes
    protected abstract fun bindLayoutRes(): Int

    /**
     * 初始化操作
     */
    protected abstract fun initialize()

    /**
     * 显示toast
     */
    protected fun showToast(message: String?) {
        if (!message.isNullOrEmpty()) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}