package io.legado.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "epubChapters",
    primaryKeys = ["bookUrl","href"],
    indices = [(Index(value = ["bookUrl"], unique = false)),
        (Index(value = ["bookUrl", "href"], unique = true))],
    foreignKeys = [(androidx.room.ForeignKey(
        entity = Book::class,
        parentColumns = ["bookUrl"],
        childColumns = ["bookUrl"],
        onDelete = androidx.room.ForeignKey.CASCADE
    ))]
)
data class EpubChapter (
    var bookUrl:String="",
    var href:String="",
    var parentHref:String?=null,
)