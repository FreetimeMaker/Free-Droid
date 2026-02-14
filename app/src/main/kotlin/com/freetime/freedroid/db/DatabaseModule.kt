package com.freetime.freedroid.db

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.freetime.freedroid.database.FDroidDatabase
import com.freetime.freedroid.database.FDroidDatabaseHolder
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideFDroidDatabase(
        @ApplicationContext context: Context,
        initialData: InitialData,
    ): FDroidDatabase {
        return FDroidDatabaseHolder.getDb(context, "fdroid_db", initialData)
    }
}
