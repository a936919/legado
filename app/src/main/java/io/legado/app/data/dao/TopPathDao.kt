package io.legado.app.data.dao

import androidx.room.*
import io.legado.app.data.entities.RuleSub
import io.legado.app.data.entities.TopPath

@Dao
interface TopPathDao {

    @get:Query("select * from topPaths")
    val all: List<TopPath>

    @Query("select count(path) as Boolean from topPaths where path = :newPath")
    fun isTopPath(newPath: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg topPath: TopPath)

    @Delete
    fun delete(vararg topPath: TopPath)

    @Update
    fun update(vararg topPath: TopPath)
}
