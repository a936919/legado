package io.legado.app.data.dao

import androidx.room.*
import io.legado.app.data.entities.ReadRecord
import io.legado.app.data.entities.ReadRecordShow

@Dao
interface ReadRecordDao {

    @get:Query("select * from readRecord")
    val all: List<ReadRecord>

    @get:Query("select bookName, author,coverUrl,durChapterIndex,totalChapterNum,durChapterTitle,sum(readTime) as readTime, durChapterTime,bookUrl,status from readRecord group by bookName,author order by -durChapterTime")
    val allShow: List<ReadRecordShow>

    @get:Query("select sum(readTime) from readRecord")
    val allTime: Long

    @Query("select sum(readTime) from readRecord where androidId = :id and  bookName = :bookName and author = :author")
    fun getReadTime(id:String,bookName: String,author: String): Long?

    @Query("select sum(listenTime) from readRecord where androidId = :id and bookName = :bookName and author = :author")
    fun getListenTime(id:String,bookName: String,author: String): Long?

    @Query("select sum(readTime) from readRecord where bookName = :bookName and author = :author")
    fun getReadTime(bookName: String,author: String): Long?

    @Query("select sum(listenTime) from readRecord where bookName = :bookName and author = :author")
    fun getListenTime(bookName: String,author: String): Long?

    @Query("select * from readRecord where bookName = :bookName and author = :author and androidId = :id")
    fun getBook(id:String,bookName: String,author:String): ReadRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg readRecord: ReadRecord)

    @Update
    fun update(vararg record: ReadRecord)

    @Delete
    fun delete(vararg record: ReadRecord)

    @Query("delete from readRecord")
    fun clear()

    @Query("delete from readRecord where bookName = :bookName and author = :author")
    fun deleteByName(bookName: String,author: String)
}