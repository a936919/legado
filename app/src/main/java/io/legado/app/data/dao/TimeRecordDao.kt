package io.legado.app.data.dao

import androidx.room.*
import io.legado.app.data.entities.rule.TimeRecord

@Dao
interface TimeRecordDao {

    @get:Query("select * from timeRecord")
    val all: List<TimeRecord>

    @get:Query("select sum(readTime) from timeRecord")
    val allTime: Long

    @Query("select sum(date) as Boolean from timeRecord where date =:date")
    fun getDay(date:Long): Boolean

    @Query("select sum(readTime) from timeRecord where date =:date")
    fun getReadTime(date:Long): Long?

    @Query("select sum(listenTime) from timeRecord where date =:date")
    fun getListenTime(date:Long): Long?

    @Query("select readTime from timeRecord where androidId = :id and bookName =:name and author = :author  and date =:date")
    fun getReadTime(id:String, name:String, author:String, date:Long): Long?

    @Query("select listenTime from timeRecord where androidId = :id and bookName =:name and author = :author  and date =:date")
    fun getListenTime(id:String, name:String, author:String, date:Long): Long?

    @Query("select sum(readTime) from timeRecord where bookName =:name and author = :author  and date =:date")
    fun getReadTime(name:String, author:String, date:Long): Long?

    @Query("select sum(listenTime) from timeRecord where bookName =:name and author = :author  and date =:date")
    fun getListenTime(name:String, author:String, date:Long): Long?

    @Query("select sum(readTime) from timeRecord where bookName =:name and author = :author")
    fun getReadTime(name:String, author:String): Long?

    @Query("select sum(listenTime) from timeRecord where bookName =:name and author = :author")
    fun getListenTime(name:String, author:String): Long?

    @Query("select * from timeRecord where bookName =:name and author = :author order by -date")
    fun getRecord(name:String, author:String): List<TimeRecord>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg timeRecord: TimeRecord)

    @Update
    fun update(vararg timeRecord: TimeRecord)

    @Delete
    fun delete(vararg timeRecord: TimeRecord)

    @Query("delete from timeRecord")
    fun clear()

    @Query("delete from timeRecord where date = :date")
    fun deleteDay(date:Long)
}