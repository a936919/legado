package io.legado.app.data.entities

import androidx.room.Entity
import io.legado.app.constant.BookType
import io.legado.app.data.entities.rule.ReadTimeRecord
import io.legado.app.utils.StringUtils

@Entity(tableName = "readRecord", primaryKeys = ["androidId", "bookName","author"])
data class ReadRecord(
        var androidId: String = "",
        var bookName: String = "",
        var author: String = "",                    // 作者名称
        var bookUrl: String = "",
        var origin: String = BookType.local,        // 书源URL(默认BookType.local)
        var coverUrl: String? = null,               // 封面Url(书源获取)
        var status:Int=0, //1为已读
){
    fun toReadTimeRecord()=ReadTimeRecord(androidId = androidId, bookName = bookName, author = author, date = getDayTime())
    private fun getDayTime():Long{
        var now = System.currentTimeMillis()
        now -= now % (1000 * 60 * 60 * 24) + (8 * 1000 * 60 * 60)
        return now
    }
}