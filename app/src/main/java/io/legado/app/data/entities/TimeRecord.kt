package io.legado.app.data.entities

import android.annotation.SuppressLint
import androidx.room.Entity
import io.legado.app.App
import io.legado.app.constant.androidIdInfo
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "timeRecord",
    primaryKeys = ["androidId","bookName","author", "date"],
)
data class TimeRecord (
    var androidId: String = androidIdInfo,
    var bookName:String = "",
    var author:String = "",
    var date:Long = 0L,
    var readTime: Long = 0,
    var listenTime:Long = 0,
){
    companion object{

        fun getDate():Long{
            val pattern = "yyyy-MM-dd"
            @SuppressLint("SimpleDateFormat")
            val format = SimpleDateFormat(pattern)
            val now = Date()
            val date = format.parse(format.format(now))?:Date(0)
            return date.time
        }

        fun formatDuring(mss: Long): String {
            val hours = mss / (1000 * 60 * 60)
            val minutes = mss % (1000 * 60 * 60) / (1000 * 60)
            val seconds = mss % (1000 * 60) / 1000
            val h = if (hours > 0) "${hours}小时" else ""
            val m = if (minutes > 0) "${minutes}分钟" else ""
            val s = if (mss < 1000 * 60) "${java.lang.Long.max(seconds, 1)}秒" else ""
            return "$h$m$s"
        }
    }

    fun equals(timeRecord:TimeRecord)= timeRecord.bookName == bookName && timeRecord.androidId == androidId && timeRecord.author == author

}

