package org.fdroid.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appPrefs")
 public data class AppPrefs(
    @PrimaryKey
     public val packageName: String,
     public val ignoreUpdates: Boolean = false,
     public val ignoreVersionCode: Long = 0,
     public val canUpdate: Boolean = true
)
