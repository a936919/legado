package io.legado.app.utils

import android.util.Log
import io.legado.app.constant.androidId
import io.legado.app.constant.androidIdInfo
import io.legado.app.data.appDb
import io.legado.app.service.help.ReadBook

object mqLog {
    fun d(s: String) {
        Log.d("hoodie13", s)
    }

    fun e(s: String) {
        Log.e("hoodie13", s)
    }

    fun debug() {
        val s = "🖼"
        //"\uD83D\uDDBC"\"
        d("${s.toStringArray().size} ${s.length}")
    }

    private fun logChapter() {
        val data = appDb.bookChapterDao.observeAll()
        d("${data.size} ${data}")
    }

    var addIndex = 0
    private fun upIndex() {
        ReadBook.book?.let {
            val mark = appDb.bookmarkDao.getByBook(it.bookUrl)
            for (m in mark) {
                m.chapterIndex -= 1
            }
            appDb.bookmarkDao.delByBook(it.bookUrl)
            appDb.bookmarkDao.insert(*mark.toTypedArray())
            d("$mark")
        }
    }
}