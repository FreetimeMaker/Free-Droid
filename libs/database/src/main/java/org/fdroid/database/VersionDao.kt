package org.fdroid.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
 public interface VersionDaoInt {

    @Query("SELECT * FROM version WHERE repoId = :repoId AND packageName = :packageName ORDER BY versionCode DESC")
    suspend  public fun getVersions(repoId: Long, packageName: String): List<Version>

    @Query("SELECT * FROM version WHERE packageName = :packageName ORDER BY versionCode DESC LIMIT 1")
    suspend  public fun getLatestVersion(packageName: String): Version?

    @Query("SELECT * FROM version WHERE repoId = :repoId AND packageName = :packageName AND versionCode = :versionCode")
    suspend  public fun getVersion(repoId: Long, packageName: String, versionCode: Long): Version?

    @Query("SELECT * FROM highestVersion WHERE packageName = :packageName")
    suspend  public fun getHighestVersion(packageName: String): HighestVersion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  public fun insert(version: Version)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  public fun insert(versions: List<Version>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  public fun insertHighestVersion(highestVersion: HighestVersion)

    @Update
    suspend  public fun update(version: Version)

    @Delete
    suspend  public fun delete(version: Version)

    @Query("DELETE FROM version WHERE repoId = :repoId")
    suspend  public fun deleteByRepository(repoId: Long)

    @Query("DELETE FROM version WHERE repoId = :repoId AND packageName = :packageName")
    suspend  public fun deleteByApp(repoId: Long, packageName: String)

    @Query("DELETE FROM highestVersion WHERE packageName = :packageName")
    suspend  public fun deleteHighestVersion(packageName: String)
}
