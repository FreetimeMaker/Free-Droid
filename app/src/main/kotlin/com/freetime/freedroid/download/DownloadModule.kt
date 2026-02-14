package com.freetime.freedroid.download

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.freetime.freedroid.BuildConfig
import com.freetime.freedroid.settings.SettingsManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {

    private const val USER_AGENT = "F-Droid ${BuildConfig.VERSION_NAME}"

    @Provides
    @Singleton
    fun provideHttpManager(settingsManager: SettingsManager): HttpManager {
        return HttpManager(userAgent = USER_AGENT, proxyConfig = settingsManager.proxyConfig)
    }

    @Provides
    @Singleton
    fun provideDownloaderFactory(
        downloaderFactoryImpl: DownloaderFactoryImpl,
    ): DownloaderFactory = downloaderFactoryImpl
}
