package com.freetime.freedroid.repo

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.freetime.freedroid.CompatibilityChecker
import com.freetime.freedroid.database.FDroidDatabase
import com.freetime.freedroid.download.DownloaderFactory
import com.freetime.freedroid.download.HttpManager
import com.freetime.freedroid.index.RepoManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideRepoManager(
        @ApplicationContext context: Context,
        db: FDroidDatabase,
        downloaderFactory: DownloaderFactory,
        httpManager: HttpManager,
        compatibilityChecker: CompatibilityChecker,
    ): RepoManager = RepoManager(
        context = context,
        db = db,
        downloaderFactory = downloaderFactory,
        httpManager = httpManager,
        compatibilityChecker = compatibilityChecker,
    )
}
