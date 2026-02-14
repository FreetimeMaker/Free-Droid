package com.freetime.freedroid.ui.discover

import android.annotation.SuppressLint
import android.app.Application
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode.ContextClock
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.engine.ProxyConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import com.freetime.freedroid.database.AppOverviewItem
import com.freetime.freedroid.database.FDroidDatabase
import com.freetime.freedroid.database.Repository
import com.freetime.freedroid.download.DownloadRequest
import com.freetime.freedroid.download.NetworkMonitor
import com.freetime.freedroid.download.PackageName
import com.freetime.freedroid.download.getImageModel
import com.freetime.freedroid.index.RepoManager
import com.freetime.freedroid.install.InstalledAppsCache
import com.freetime.freedroid.repo.RepoUpdateManager
import com.freetime.freedroid.settings.SettingsManager
import com.freetime.freedroid.ui.categories.CategoryItem
import com.freetime.freedroid.ui.search.SearchManager
import com.freetime.freedroid.utils.IoDispatcher
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val app: Application,
    savedStateHandle: SavedStateHandle,
    private val db: FDroidDatabase,
    networkMonitor: NetworkMonitor,
    private val settingsManager: SettingsManager,
    private val searchManager: SearchManager,
    private val repoManager: RepoManager,
    private val repoUpdateManager: RepoUpdateManager,
    private val installedAppsCache: InstalledAppsCache,
    @param:IoDispatcher private val ioScope: CoroutineScope,
) : AndroidViewModel(app) {

    private val log = KotlinLogging.logger { }
    private val moleculeScope =
        CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)
    private val collator = Collator.getInstance(Locale.getDefault())

    private val newApps = db.getAppDao().getNewAppsFlow().map { list ->
        val proxyConfig = settingsManager.proxyConfig
        list.mapNotNull {
            val repository = repoManager.getRepository(it.repoId) ?: return@mapNotNull null
            it.toAppDiscoverItem(repository, proxyConfig)
        }
    }
    private val recentlyUpdatedApps = db.getAppDao().getRecentlyUpdatedAppsFlow().map { list ->
        val proxyConfig = settingsManager.proxyConfig
        list.mapNotNull {
            val repository = repoManager.getRepository(it.repoId) ?: return@mapNotNull null
            it.toAppDiscoverItem(repository, proxyConfig)
        }
    }
    private val mostDownloadedApps = MutableStateFlow<List<AppDiscoverItem>?>(null)
    private val categories = db.getRepositoryDao().getLiveCategories().asFlow().map { categories ->
        categories.map { category ->
            CategoryItem(
                id = category.id,
                name = category.getName(localeList) ?: "Unknown Category",
            )
        }.sortedWith { c1, c2 -> collator.compare(c1.name, c2.name) }
    }
    private val hasRepoIssues = repoManager.repositoriesState.map { repos ->
        repos.any { it.enabled && it.errorCount >= 5 }
    }

    val localeList = LocaleListCompat.getDefault()
    val discoverModel: StateFlow<DiscoverModel> by lazy(LazyThreadSafetyMode.NONE) {
        @SuppressLint("StateFlowValueCalledInComposition") // see comment below
        moleculeScope.launchMolecule(mode = ContextClock) {
            DiscoverPresenter(
                newAppsFlow = newApps,
                recentlyUpdatedAppsFlow = recentlyUpdatedApps,
                mostDownloadedAppsFlow = mostDownloadedApps,
                categoriesFlow = categories,
                repositoriesFlow = repoManager.repositoriesState,
                searchTextFieldState = searchManager.textFieldState,
                isFirstStart = settingsManager.isFirstStart,
                // not observing the flow, but just taking the current value,
                // because we kick off repo updates from the UI depending on this state
                networkState = networkMonitor.networkState.value,
                repoUpdateStateFlow = repoUpdateManager.repoUpdateState,
                hasRepoIssuesFlow = hasRepoIssues,
            )
        }
    }

    init {
        loadMostDownloadedApps()
    }

    private fun loadMostDownloadedApps() {
        viewModelScope.launch(ioScope.coroutineContext) {
            val packageNames = try {
                app.assets.open("most_downloaded_apps.json").use { inputStream ->
                    @OptIn(ExperimentalSerializationApi::class)
                    Json.decodeFromStream<List<String>>(inputStream)
                }
            } catch (e: Exception) {
                log.error(e) { "Error loading most downloaded apps: " }
                return@launch
            }
            db.getAppDao().getAppsFlow(packageNames).collect { apps ->
                val proxyConfig = settingsManager.proxyConfig
                mostDownloadedApps.value = apps.mapNotNull {
                    val repository = repoManager.getRepository(it.repoId) ?: return@mapNotNull null
                    it.toAppDiscoverItem(repository, proxyConfig)
                }
            }
        }
    }

    private fun AppOverviewItem.toAppDiscoverItem(
        repository: Repository,
        proxyConfig: ProxyConfig?,
    ): AppDiscoverItem {
        val isInstalled = installedAppsCache.isInstalled(packageName)
        val imageModel =
            getIcon(localeList)?.getImageModel(repository, proxyConfig) as? DownloadRequest
        return AppDiscoverItem(
            packageName = packageName,
            name = getName(localeList) ?: "Unknown App",
            lastUpdated = lastUpdated,
            isInstalled = isInstalled,
            imageModel = if (isInstalled) {
                PackageName(packageName, imageModel)
            } else {
                imageModel
            },
        )
    }
}
