package org.fdroid.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anti_feature")
public data class AntiFeature(
    @PrimaryKey
    public val repoId: Long,
    public val name: String,
    public val icon: Map<String, String>,
    public val description: Map<String, String>,
    public val label: Map<String, String>
) : RepoAttribute
