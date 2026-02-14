package org.fdroid.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
public data class Category(
    @PrimaryKey
    public val repoId: Long,
    public val name: String,
    public val icon: String?,
    public val description: String?
)
