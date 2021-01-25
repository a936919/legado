package io.legado.app.data.entities.rule

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.ForeignKey
import io.legado.app.App
import io.legado.app.data.entities.ReadRecord
import java.text.SimpleDateFormat
import java.util.*

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
        fun getDate():Long{
            val pattern = "yyyy-MM-dd"
            @SuppressLint("SimpleDateFormat")
            val format = SimpleDateFormat(pattern)
            val now = Date()
            val date = format.parse(format.format(now))?:Date(0)
            return date.time
        }
    }
}

