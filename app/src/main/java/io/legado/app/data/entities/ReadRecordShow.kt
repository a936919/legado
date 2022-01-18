package io.legado.app.data.entities

import androidx.room.Ignore

data class ReadRecordShow(
    var bookName: String = "",
    var author: String = "",
    var coverUrl:String? = null,
    var durChapterIndex: Int = 0,               // 当前章节索引
    var totalChapterNum: Int = 0,               // 书籍目录总数
    var durChapterTitle: String? = null,        // 当前章节名称
    @Ignore
    var readTime: Long = 0L,
    var durChapterTime:Long = 0L,
    var status:Int =0,
    var bookUrl:String = "",
)