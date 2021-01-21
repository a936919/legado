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
    fun getReadTimeByDay(date:Long): Long?

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