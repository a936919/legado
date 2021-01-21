package io.legado.app.data.entities.rule

import androidx.room.Entity
import androidx.room.ForeignKey
import io.legado.app.data.entities.ReadRecord

@Entity(
    tableName = "readTimeRecord",
    primaryKeys = ["androidId", "bookName","author","date"],
    foreignKeys = [(ForeignKey(
            entity = ReadRecord::class,
            parentColumns = ["androidId", "bookName","author"],
            childColumns = ["androidId", "bookName","author"],
            onDelete = ForeignKey.CASCADE
    ))]
)
data class ReadTimeRecord (
    var androidId: String = "",
    var bookName: String = "",
    var author: String = "",                    // 作者名称
    var coverUrl: String? = null,               // 封面Url(书源获取)
    var date:Long=0L,
    var readTime: Long = 0L,
    var durTime:Long = System.currentTimeMillis(),
)