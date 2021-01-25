package io.legado.app.data.entities.rule

import androidx.room.Entity
import androidx.room.ForeignKey
import io.legado.app.App
import io.legado.app.data.entities.ReadRecord

@Entity(
    tableName = "timeRecord",
    primaryKeys = ["androidId", "date","bookName","author"],
)
data class TimeRecord (
    var androidId: String = App.androidId,
    var date:Long = 0L,
    var bookName:String = "",
    var author:String = "",
    var readTime: Long = 0L,
    var listenTime:Long = 0L
){
    init {
        date = getDayTime()
    }
    companion object{
        fun getDayTime():Long{
            var now = System.currentTimeMillis()
            now -= now % (1000 * 60 * 60 * 24) + (8 * 1000 * 60 * 60)
            return now
        }
    }
}