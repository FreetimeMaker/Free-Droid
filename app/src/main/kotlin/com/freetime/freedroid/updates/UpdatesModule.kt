package com.freetime.freedroid.updates

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.freetime.freedroid.CompatibilityChecker
import com.freetime.freedroid.CompatibilityCheckerImpl
import com.freetime.freedroid.UpdateChecker
import com.freetime.freedroid.database.DbAppChecker
import com.freetime.freedroid.database.FDroidDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UpdatesModule {
    @Provides
    @Singleton
    fun provideCompatibilityChecker(@ApplicationContext context: Context): CompatibilityChecker {
        return CompatibilityCheckerImpl(context.packageManager)
    }

    @Provides
    @Singleton
    fun provideUpdateChecker(compatibilityChecker: CompatibilityChecker): UpdateChecker {
        return UpdateChecker(compatibilityChecker)
    }

    @Provides
    @Singleton
    fun provideDbAppChecker(
        @ApplicationContext context: Context,
        db: FDroidDatabase,
        updateChecker: UpdateChecker,
        compatibilityChecker: CompatibilityChecker,
    ): DbAppChecker {
        return DbAppChecker(db, context, compatibilityChecker, updateChecker)
    }
}
