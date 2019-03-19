package com.memo.hotfix.utils

import java.io.*

/**
 * title:
 * describe:
 *
 * @author zhou
 * @date 2019-03-18 12:36
 */
object FileUtils {

    @Throws(IOException::class)
    fun copy(from: File, to: File) {
        val fromFis = FileInputStream(from)
        val fromBis = BufferedInputStream(fromFis)

        val toFos = FileOutputStream(to)
        val toBos = BufferedOutputStream(toFos)

        val put = ByteArray(1024 * 5)
        var len: Int = fromBis.read(put)
        while (len != -1) {
            toBos.write(put, 0, len)
            len = fromBis.read(put)
        }
        //刷新
        toBos.flush()

        //关闭
        fromFis.close()
        fromBis.close()
        toFos.close()
        toBos.close()
    }
}