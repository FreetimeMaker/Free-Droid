package org.fdroid.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.core.os.LocaleListCompat
import android.content.Context
import java.util.concurrent.Callable
import kotlinx.coroutines.runBlocking

@Database(
    entities = [
        Repository::class,
        App::class,
        Version::class,
        HighestVersion::class,
        AppPrefs::class,
        Category::class,
        AntiFeature::class,
        ReleaseChannel::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
 public abstract class FDroidDatabase : RoomDatabase(), FDroidDatabaseInt {

    public override abstract fun getRepositoryDao(): RepositoryDaoInt
    public override abstract fun getAppDao(): AppDaoInt
    public override abstract fun getVersionDao(): VersionDaoInt
    public override abstract fun getAppPrefsDao(): AppPrefsDaoInt

    public override fun clearAllAppData() {
        runInTransaction {
            runBlocking {
                getAppDao().deleteAllApps()
            }
        }
    }

     public override fun afterLocalesChanged(locales: LocaleListCompat) {
        // Implementation for locale changes
    }

     public override fun <T> runInTransaction(callable: Callable<T>): T {
        return super.runInTransaction(callable)
    }

    companion  public object {
         public fun getDatabase(context: Context): FDroidDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FDroidDatabase::class.java,
                "fdroid_database"
            ).build()
        }
    }
}
