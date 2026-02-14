package org.fdroid.database

import androidx.core.os.LocaleListCompat
import java.util.concurrent.Callable

 public interface FDroidDatabaseInt {
     public fun getRepositoryDao(): RepositoryDaoInt
     public fun getAppDao(): AppDaoInt
     public fun getVersionDao(): VersionDaoInt
     public fun getAppPrefsDao(): AppPrefsDaoInt
     public fun clearAllAppData()
     public fun afterLocalesChanged(locales: LocaleListCompat)
     public fun <T> runInTransaction(callable: Callable<T>): T
}
