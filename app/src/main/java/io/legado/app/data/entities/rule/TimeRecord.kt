package io.legado.app.data.entities.rule

import androidx.room.Entity
import androidx.room.ForeignKey
import io.legado.app.App
import io.legado.app.data.entities.ReadRecord

@Entity(
    tableName = "timeRecord",
    primaryKeys = ["androidId","bookName","author", "date"],
    foreignKeys = [(ForeignKey(
            entity = ReadRecord::class,
            parentColumns = ["androidId", "bookName","author"],
            childColumns = ["androidId", "bookName","author"],
            onDelete = ForeignKey.CASCADE
    ))]
)
data class TimeRecord (
    var androidId: String = App.androidId,
    var bookName:String = "",
    var author:String = "",
    var date:Long = 0L,
    var readTime: Long = 0L,
    var listenTime:Long = 0L
){
    companion object{
        fun getDayTime():Long{
            var now = System.currentTimeMillis()
            now -= now % (1000 * 60 * 60 * 24) + (8 * 1000 * 60 * 60)
            return now
        }
    }
}