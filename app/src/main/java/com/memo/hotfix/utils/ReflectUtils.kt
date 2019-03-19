package com.memo.hotfix.utils

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-18 13:11
 */
object ReflectUtils {

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun getField(obj: Any, clazz: Class<*>, field: String): Any {
        val declaredField = clazz.getDeclaredField(field)
        declaredField.isAccessible = true
        return declaredField.get(obj)
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun setField(obj: Any, clazz: Class<*>, field: String, value: Any) {
        val declaredField = clazz.getDeclaredField(field)
        declaredField.isAccessible = true
        declaredField.set(obj, value)
    }

    fun getPathList(baseDexClassLoader: Any): Any {
        return getField(
            baseDexClassLoader,
            Class.forName(Constant.BaseDexClassLoaderName),
            Constant.pathList
        )
    }

    fun getDexElement(paramObject: Any): Any {
        return getField(
            paramObject,
            paramObject.javaClass,
            Constant.dexElements
        )
    }

    fun setDexElement(obj: Any, clazz: Class<*>, value: Any) {
        setField(obj, clazz, Constant.dexElements, value)
    }
}