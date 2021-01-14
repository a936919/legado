package io.legado.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topPaths")
data class TopPath (
    @PrimaryKey
    var path: String = ""
)