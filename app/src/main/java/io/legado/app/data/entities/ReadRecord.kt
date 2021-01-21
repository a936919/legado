package io.legado.app.data.entities

import androidx.room.Entity
import io.legado.app.App
import io.legado.app.constant.BookType

@Entity(tableName = "readRecord", primaryKeys = ["androidId", "bookName","author"])
data class ReadRecord(
    var androidId: String =  App.androidId,
    var bookName: String = "",
    var author: String = "",                    // 作者名称
    var bookUrl: String = "",
    var origin: String = BookType.local,        // 书源URL(默认BookType.local)
    var coverUrl: String = "",               // 封面Url(书源获取)
    var status:Int=0, //1为已读
    var durChapterIndex: Int = 0,               // 当前章节索引
    var totalChapterNum: Int = 0,               // 书籍目录总数
    var durChapterTitle: String = "",        // 当前章节名称
    var durChapterPos: Int = 0,                 // 当前阅读的进度(首行字符的索引位置)
    var readTime:Long = 0L,
    var durChapterTime:Long = System.currentTimeMillis()
)