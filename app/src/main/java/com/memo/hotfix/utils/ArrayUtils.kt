package com.memo.hotfix.utils

import java.lang.reflect.Array


/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-18 13:31
 */
object ArrayUtils {

    /**
     * 合并两个列表返回
     * arrayL 在前 arrayR在后
     */
    fun combineArray(arrayL: Any, arrayR: Any): Any {
        //获取数据类型
        val clazz = arrayL.javaClass.componentType
        //左边数组长度
        val lLength: Int = Array.getLength(arrayL)
        //右边数组长度
        val rLength: Int = Array.getLength(arrayR)
        //总长度
        val totalLength: Int = lLength + rLength
        //新建一个数组
        val result: Any = Array.newInstance(clazz, totalLength)

        //先加左边的 后加右边的
        for (index in 0 until totalLength) {
            if (index < lLength) {
                //如果是在arrayL里面先添加到新数组
                Array.set(result, index, Array.get(arrayL, index))
            } else {
                //这个时候arrayL里面都添加完了 添加arrayR到新数组
                Array.set(result, index, Array.get(arrayR, index - lLength))
            }
        }
        LogUtils.i("补丁包 $lLength   原有包 $rLength  最终 ${Array.getLength(result)}")
        return result
    }
}