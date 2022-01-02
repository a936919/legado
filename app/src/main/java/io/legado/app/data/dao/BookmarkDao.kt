package io.legado.app.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.legado.app.data.entities.Bookmark


@Dao
interface BookmarkDao {

    @get:Query("select * from bookmarks")
    val all: List<Bookmark>

    @Query("select * from bookmarks  where bookName = :bookName and bookAuthor = :bookAuthor ")
    fun getByBook(bookName: String,bookAuthor: String): List<Bookmark>

    @Query("delete from bookmarks  where bookName = :bookName and bookAuthor = :bookAuthor ")
    fun delByBook(bookName: String,bookAuthor: String)

    @Query("select * from bookmarks where bookName = :bookName and bookAuthor = :bookAuthor")
    fun observeByBook(
        bookName: String,
        bookAuthor: String
    ): LiveData<List<Bookmark>>
    @Query("select count(bookName) as Boolean from bookmarks where bookName = :bookName and bookAuthor = :bookAuthor ")
    fun haveBook(bookName: String,bookAuthor: String): Boolean

    @Query(
        """
        SELECT * FROM bookmarks where bookName = :bookName and bookAuthor = :bookAuthor 
        and chapterName like '%'||:key||'%' or content like '%'||:key||'%'
    """
    )
    fun liveDataSearch(
        bookName: String,
        bookAuthor: String,
        key: String
    ): LiveData<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg bookmark: Bookmark)

    @Update
    fun update(bookmark: Bookmark)

    @Delete
    fun delete(vararg bookmark: Bookmark)

}