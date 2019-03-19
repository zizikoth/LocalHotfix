package com.memo.hotfix.utils

import android.content.Context
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader
import java.io.File

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-18 12:27
 */
object HotfixUtils {

    /*** 补丁包集合 ***/
    private val patchDexSet: HashSet<File> by lazy { HashSet<File>() }

    fun loadFixedDex(mContext: Context) {
        //先清空
        patchDexSet.clear()
        //获取补丁包目录
        val patchDexDir: File = mContext.getDir(Constant.DEX_DIR, Context.MODE_PRIVATE)
        //遍历这个补丁包下面的所有的文件
        val listFiles: Array<File> = patchDexDir.listFiles()
        for (file in listFiles) {
            if (file.name.endsWith(Constant.DEX_SUFFIX) && Constant.MAIN_DEX != file.name) {
                //找到文件夹下面的补丁包 放入自己的补丁包集合中
                patchDexSet.add(file)
            }
        }
        LogUtils.i("补丁包集合 ${patchDexSet.size}")
        if (patchDexSet.size > 0) {
            //类加载器加载
            createDexClassLoader(mContext, patchDexDir)
        }
    }

    private fun createDexClassLoader(mContext: Context, patchDexDir: File) {
        //临时dex解压目录 因为类加载器加载的是类而不是dex 所以需要将dex进行解压
        val optDirPath: String = patchDexDir.absolutePath + File.separator + Constant.DEX_OPT
        //创建
        val optDir = File(optDirPath)
        if (!optDir.exists()) {
            optDir.mkdirs()
        }
        for (dex in patchDexSet) {
            //自己创建一个补丁DexClassLoader
            val patchClassLoader = DexClassLoader(dex.absolutePath, optDirPath, null, mContext.classLoader)
            //每次获取一个补丁文件，需要插桩一次

            //⚠️⚠️⚠️！！！最重要的环节！！！⚠️⚠️⚠️
            hotFix(patchClassLoader, mContext)
        }

    }

    private fun hotFix(patchClassLoader: DexClassLoader, mContext: Context) {
        //这里分为6步
        //1.获取原有的PathClassLoader
        val pathClassLoader: PathClassLoader = mContext.classLoader as PathClassLoader

        try {

            //2.获取补丁包列表 dexElement
            val patchDexElement = ReflectUtils.getDexElement(ReflectUtils.getPathList(patchClassLoader))

            //3.获取原有的pathList
            val oriPathList = ReflectUtils.getPathList(pathClassLoader)

            //4.获取原有包列表 dexElement
            val oriDexElement = ReflectUtils.getDexElement(oriPathList)

            //5.合并成为一个新的 补丁包在前 原有包在后的dexElement
            val finalDexElement = ArrayUtils.combineArray(patchDexElement, oriDexElement)

            //6.用合成后的dexElement重新赋值原有的pathList里面的dexElement属性
            ReflectUtils.setDexElement(oriPathList, oriPathList.javaClass, finalDexElement)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}