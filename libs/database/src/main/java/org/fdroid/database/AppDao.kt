package org.fdroid.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
 public interface AppDaoInt {

    @Query("SELECT * FROM app WHERE packageName = :packageName")
     public fun getApp(packageName: String): LiveData<App?>

    @Query("SELECT * FROM app WHERE repoId = :repoId AND packageName = :packageName")
    suspend  public fun getApp(repoId: Long, packageName: String): App?

    @Query("SELECT COUNT(*) FROM app")
    suspend  public fun countApps(): Int

    @Query("SELECT * FROM app")
    suspend  public fun getAllApps(): List<App>

    @Query("SELECT * FROM app WHERE packageName IN (:packageNames)")
    suspend  public fun getApps(packageNames: List<String>): List<App>

    @Query("SELECT * FROM app WHERE repoId = :repoId")
    suspend  public fun getAppsByRepository(repoId: Long): List<App>

    @Query("SELECT packageName FROM app WHERE repoId = :repoId")
    suspend  public fun getRepositoryIdsForApp(repoId: Long): List<String>

    @Query("SELECT COUNT(*) FROM app")
     public fun getNumberOfAppsInCategory(category: String): Int = 0 // Placeholder

    @Query("SELECT COUNT(*) FROM app WHERE repoId = :repoId")
     public fun getNumberOfAppsInRepository(repoId: Long): Int

    @Query("SELECT * FROM app WHERE packageName LIKE '%' || :query || '%'")
    suspend  public fun getAppSearchItems(query: String): List<App>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  public fun insert(app: App)

    @Update
    suspend  public fun update(app: App)

    @Delete
    suspend  public fun delete(app: App)

    @Query("DELETE FROM app WHERE repoId = :repoId")
    suspend  public fun deleteByRepository(repoId: Long)

    @Query("DELETE FROM app")
    suspend  public fun deleteAllApps()
}
