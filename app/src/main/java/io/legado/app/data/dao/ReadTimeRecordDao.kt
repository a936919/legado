package io.legado.app.data.dao

import androidx.room.*
import io.legado.app.data.entities.ReadRecordShow
import io.legado.app.data.entities.rule.ReadTimeRecord

@Dao
interface ReadTimeRecordDao {

    @get:Query("select * from readTimeRecord order by durTime")
    val all: List<ReadTimeRecord>

    @get:Query("select bookName,author,coverUrl,sum(readTime) as readTime, durTime from readTimeRecord order by durTime")
    val allShow: List<ReadRecordShow>

    @get:Query("select sum(readTime) from readTimeRecord")
    val allTime: Long

    @Query("select bookName,author,coverUrl,sum(readTime) as readTime, durTime from readTimeRecord where date =:date")
    fun getBookByDay(date:String): List<ReadRecordShow>

    @Query("select sum(date) as Boolean from readTimeRecord where date =:date")
    fun getDay(date:String): Boolean

    @Query("select bookName,author,sum(readTime) as readTime,durTime from readTimeRecord where date >= :start and date<=:end")
    fun getBookByDay(start:Long,end:Long): List<ReadRecordShow>

    @Query("select sum(readTime) from readTimeRecord where bookName = :bookName and author =:author")
    fun getBookReadTime(bookName:String,author:String): Long?

    @Query("select sum(readTime) from readTimeRecord where date =:date")
    fun getReadTimeByDay(date:String): Long?

    @Query("select sum(readTime) from readTimeRecord where bookName = :bookName and author =:author and date =:date")
    fun getBookReadTimeByDay(bookName: String, author:String, date: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg readTimeRecord: ReadTimeRecord)

    @Update
    fun update(vararg readTimeRecord: ReadTimeRecord)

    @Delete
    fun delete(vararg readTimeRecord: ReadTimeRecord)

    @Query("delete from readTimeRecord")
    fun clear()

    @Query("delete from readTimeRecord where bookName = :bookName and author = :author")
    fun deleteBook(bookName: String,author: String)
}