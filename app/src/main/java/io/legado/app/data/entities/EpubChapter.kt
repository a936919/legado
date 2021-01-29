package io.legado.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epubChapters")
data class EpubChapter (
    @PrimaryKey
    var index:Int=0,
    var parentHref:String?=null,
    var href:String?=null
)