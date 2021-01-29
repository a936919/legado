package io.legado.app.data.dao

import androidx.room.*
import io.legado.app.data.entities.EpubChapter

@Dao
interface EpubChapterDao {

    @Query("select * from epubChapters Where parentHref = :parentHref ")
    fun get(parentHref: String): List<EpubChapter>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg chapter: EpubChapter)

    @Query("delete from epubChapters")
    fun clear()

    @Delete
    fun delete(vararg chapter: EpubChapter)

    @Update
    fun update(vararg chapter: EpubChapter)

}