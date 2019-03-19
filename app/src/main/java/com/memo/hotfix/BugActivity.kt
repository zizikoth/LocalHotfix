package com.memo.hotfix

import android.Manifest
import android.content.Context
import android.os.Environment
import android.support.v4.app.ActivityCompat
import com.memo.hotfix.utils.Constant
import com.memo.hotfix.utils.FileUtils
import com.memo.hotfix.utils.HotfixUtils
import com.memo.hotfix.utils.LogUtils
import kotlinx.android.synthetic.main.activity_bug.*
import java.io.File

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-18 18:53
 */
class BugActivity : BaseActivity() {
    /**
     * 绑定布局id
     */
    override fun bindLayoutRes(): Int = R.layout.activity_bug

    /**
     * 初始化操作
     */
    override fun initialize() {
        ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

        //模拟bug 点击不做任何操作
        mBtnShow.setOnClickListener {
            mTvResult.text = "Bug修复了"
        }

        //点击修复bug
        mBtnFix.setOnClickListener { fixBug() }
    }

    private fun fixBug() {
        //将补丁包patch.dex放到手机的根目录下面 然后将文件从本地复制到私有目录下面
        //实际上是从服务器下载到本地目录下面
        val from: File = File(Environment.getExternalStorageDirectory(), Constant.PATCH_DEX)
        val to: File =
            File(getDir(Constant.DEX_DIR, Context.MODE_PRIVATE).absolutePath + File.separator + Constant.PATCH_DEX)
        if (to.exists()) {
            //如果之前的补丁包存在 就删除
            val isDelete = to.delete()
            LogUtils.i("补丁包删除$isDelete")
        }

        if (from.exists()) {
            //复制 模拟服务器下载
            FileUtils.copy(from, to)
            showToast("补丁加载成功")
            LogUtils.i("copy成功${to.exists()}")
            from.delete()
            HotfixUtils.loadFixedDex(mContext)
        } else {
            showToast("补丁包不存在")
        }
    }
}