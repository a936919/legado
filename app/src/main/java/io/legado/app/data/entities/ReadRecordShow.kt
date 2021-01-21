package io.legado.app.data.entities

data class ReadRecordShow(
    var bookName: String = "",
    var author: String = "",
    var coverUrl:String = "",
    var readTime: Long = 0L,
    var durTime:Long = 0L,
    var status:Int =0
)