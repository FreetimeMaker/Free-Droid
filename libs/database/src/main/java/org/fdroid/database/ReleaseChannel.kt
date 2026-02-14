package org.fdroid.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "releaseChannel")
public data class ReleaseChannel(
    @PrimaryKey
    public val repoId: Long,
    public val name: String,
    public val description: String?
) : RepoAttribute
