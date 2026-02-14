package org.fdroid.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
 public interface RepositoryDaoInt {

    @Query("SELECT * FROM repository ORDER BY name")
     public fun getRepositories(): List<Repository>

    @Query("SELECT * FROM repository WHERE repoId = :repoId")
     public fun getRepository(repoId: Long): Repository?

    @Query("SELECT * FROM repository WHERE repoId = :repoId")
     public fun getRepositoryFlow(repoId: Long): Flow<Repository?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     public fun insertOrReplace(repository: Repository): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     public fun insertOrReplace(repositories: List<Repository>): List<Long>

    @Update
     public fun update(repository: Repository)

    @Delete
     public fun delete(repository: Repository)

    @Query("DELETE FROM repository WHERE repoId = :repoId")
     public fun deleteById(repoId: Long)

    @Query("UPDATE repository SET timestamp = :timestamp WHERE repoId = :repoId")
     public fun updateTimestamp(repoId: Long, timestamp: Long)
}
