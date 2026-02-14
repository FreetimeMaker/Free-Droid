package org.fdroid.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.fdroid.index.v2.FileV2

@Entity(tableName = "version")
 public data class Version(
    @PrimaryKey
     public val repoId: Long,
     public val packageName: String,
     public val versionCode: Long,
     public val versionName: String,
     public val file: FileV2?,
     public val releaseDate: Long?,
     public val sdkVersion: Int?,
     public val targetSdkVersion: Int?,
     public val minSdkVersion: Int?,
     public val maxSdkVersion: Int?,
     public val source: String?,
     public val signature: String?,
     public val signer: String?,
     public val hash: String?,
     public val hashType: String?,
     public val size: Long?,
     public val installed: Boolean = false,
     public val compatible: Boolean = true
)

@Entity(tableName = "highestVersion")
 public data class HighestVersion(
    @PrimaryKey
     public val packageName: String,
     public val repoId: Long,
     public val versionCode: Long
)
