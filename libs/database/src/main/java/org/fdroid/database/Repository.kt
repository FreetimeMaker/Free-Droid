package org.fdroid.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.fdroid.index.v2.LocalizedFileV2

@Entity(tableName = "repository")
public data class Repository(
    @PrimaryKey
    public val repoId: Long,
    public val address: String,
    public val name: String,
    public val description: String?,
    public val version: Int,
    public val timestamp: Long,
    public val icon: LocalizedFileV2?,
    public val fingerprint: String,
    public val lastUpdated: Long,
    public val isArchive: Boolean = false
)
