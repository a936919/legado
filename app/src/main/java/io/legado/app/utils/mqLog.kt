package io.legado.app.utils

import android.util.Log
import io.legado.app.data.appDb

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
}