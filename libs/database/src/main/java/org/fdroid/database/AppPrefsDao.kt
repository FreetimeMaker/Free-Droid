package org.fdroid.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
 public interface AppPrefsDaoInt {

    @Query("SELECT * FROM appPrefs WHERE packageName = :packageName")
    suspend  public fun getPrefs(packageName: String): AppPrefs?

    @Query("SELECT * FROM appPrefs WHERE packageName = :packageName")
     public fun getPrefsFlow(packageName: String): Flow<AppPrefs?>

    @Query("SELECT * FROM appPrefs")
    suspend  public fun getAllPrefs(): List<AppPrefs>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  public fun insert(prefs: AppPrefs)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend  public fun insert(prefsList: List<AppPrefs>)

    @Update
    suspend  public fun update(prefs: AppPrefs)

    @Delete
    suspend  public fun delete(prefs: AppPrefs)

    @Query("DELETE FROM appPrefs WHERE packageName = :packageName")
    suspend  public fun deleteByPackageName(packageName: String)

    @Query("UPDATE appPrefs SET ignoreUpdates = :ignoreUpdates WHERE packageName = :packageName")
    suspend  public fun updateIgnoreUpdates(packageName: String, ignoreUpdates: Boolean)

    @Query("UPDATE appPrefs SET ignoreVersionCode = :ignoreVersionCode WHERE packageName = :packageName")
    suspend  public fun updateIgnoreVersionCode(packageName: String, ignoreVersionCode: Long)

    @Query("UPDATE appPrefs SET canUpdate = :canUpdate WHERE packageName = :packageName")
    suspend  public fun updateCanUpdate(packageName: String, canUpdate: Boolean)
}
