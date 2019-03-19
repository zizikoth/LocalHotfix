package com.memo.hotfix.utils

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-18 12:28
 */
object Constant {

    /*** 主包名称 ***/
    const val MAIN_DEX = "classes.dex"

    /*** 补丁包名称 固定 实际上应该是服务器返回的名称 ***/
    const val PATCH_DEX = "patch.dex"

    /*** 补丁包的存放路径 ***/
    const val DEX_DIR = "dexDir"

    /*** 包后缀 ***/
    const val DEX_SUFFIX = ".dex"

    /*** 临时Dex解压路径 ***/
    const val DEX_OPT = "dex_opt"

    /*** 名称 ***/
    const val BaseDexClassLoaderName = "dalvik.system.BaseDexClassLoader"

    /*** 属性名称 ***/
    const val pathList = "pathList"

    /*** PathList里面的dexElement 属性名称 ***/
    const val dexElements = "dexElements"

}