package io.legado.app.utils

import android.util.Log
import io.legado.app.App
import io.legado.app.service.help.ReadBook

object mqLog {
    fun d(s: String) {
        Log.d("hoodie13", s)
    }

    fun e(s: String) {
        Log.e("hoodie13", s)
    }

    fun debug() {
    }

    private fun logChapter() {
        val data = App.db.bookChapterDao.observeAll()
        d("${data.size} ${data}")
    }

    var addIndex = 0
    private fun upIndex() {
        ReadBook.book?.let {
            val mark = App.db.bookmarkDao.getByBook(it.bookUrl)
            for (m in mark) {
                m.chapterIndex -= 1
            }
            App.db.bookmarkDao.delByBook(it.bookUrl)
            App.db.bookmarkDao.insert(*mark.toTypedArray())
            d("$mark")
        }
    }
}