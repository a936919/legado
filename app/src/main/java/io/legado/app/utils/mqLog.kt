package io.legado.app.utils

import android.util.Log
import io.legado.app.App

object mqLog {
    fun d(s:String){
        Log.d("hoodie13",s)
    }
    fun e(s:String){
        Log.e("hoodie13",s)
    }
    fun debug() {
        logChapter()
    }

    private fun logChapter() {
       val data = App.db.bookChapterDao.observeAll()
        d("${data.size} ${data}")
    }
}