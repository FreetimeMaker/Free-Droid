package com.freetime.freedroid.ui.repositories.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import app.cash.molecule.AndroidUiDispatcher
import app.cash.molecule.RecompositionMode.ContextClock
import app.cash.molecule.launchMolecule
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import com.freetime.freedroid.database.FDroidDatabase
import com.freetime.freedroid.database.Repository
import com.freetime.freedroid.download.Mirror
import com.freetime.freedroid.download.NetworkMonitor
import com.freetime.freedroid.index.RepoManager
import com.freetime.freedroid.repo.RepoUpdateManager
import com.freetime.freedroid.repo.RepoUpdateWorker
import com.freetime.freedroid.settings.OnboardingManager
import com.freetime.freedroid.settings.SettingsManager
import com.freetime.freedroid.ui.repositories.details.ArchiveState.UNKNOWN
import com.freetime.freedroid.updates.UpdatesManager
import com.freetime.freedroid.utils.IoDispatcher

@HiltViewModel(assistedFactory = RepoDetailsViewModel.Factory::class)
class RepoDetailsViewModel @AssistedInject constructor(
    app: Application,
    @Assisted private val repoId: Long,
    networkMonitor: NetworkMonitor,
    private val db: FDroidDatabase,
    private val repoManager: RepoManager,
    private val updateManager: UpdatesManager,
    repoUpdateManager: RepoUpdateManager,
    private val settingsManager: SettingsManager,
    private val onboardingManager: OnboardingManager,
    @param:IoDispatcher private val ioScope: CoroutineScope,
) : AndroidViewModel(app), RepoDetailsActions {

    private val log = KotlinLogging.logger {}
    private val moleculeScope =
        CoroutineScope(viewModelScope.coroutineContext + AndroidUiDispatcher.Main)

    private val repoFlow = MutableStateFlow<Repository?>(null)
    private val numAppsFlow: Flow<Int?> = repoFlow.map { repo ->
        if (repo != null) {
            db.getAppDao().getNumberOfAppsInRepository(repo.repoId)
        } else null
    }.flowOn(Dispatchers.IO).distinctUntilChanged()
    private val archiveStateFlow = MutableStateFlow(UNKNOWN)
    private val showOnboarding = onboardingManager.showRepoDetailsOnboarding
    private val updateFlow = repoUpdateManager.repoUpdateState.map {
        if (it?.repoId == repoId) it else null
    }

    val model: StateFlow<RepoDetailsModel> by lazy(LazyThreadSafetyMode.NONE) {
        moleculeScope.launchMolecule(mode = ContextClock) {
            RepoDetailsPresenter(
                repoFlow = repoFlow,
                numAppsFlow = numAppsFlow,
                archiveStateFlow = archiveStateFlow,
                showOnboardingFlow = showOnboarding,
                updateFlow = updateFlow,
                networkStateFlow = networkMonitor.networkState,
                proxyConfig = settingsManager.proxyConfig,
            )
        }
    }

    init {
        viewModelScope.launch {
            repoManager.repositoriesState.collect { repos ->
                val repo = repos.find { it.repoId == repoId }
                onRepoChanged(repo)
            }
        }
    }

    private fun onRepoChanged(repo: Repository?) {
        repoFlow.update { repo }
        archiveStateFlow.update { repo?.archiveState() ?: UNKNOWN }
    }

    override fun deleteRepository() {
        ioScope.launch {
            repoManager.deleteRepository(repoId)
            updateManager.loadUpdates()
        }
    }

    override fun updateUsernameAndPassword(username: String, password: String) {
        ioScope.launch {
            repoManager.updateUsernameAndPassword(repoId, username, password)
            withContext(Dispatchers.Main) {
                RepoUpdateWorker.updateNow(application, repoId)
            }
        }
    }

    override fun setMirrorEnabled(mirror: Mirror, enabled: Boolean) {
        ioScope.launch {
            repoManager.setMirrorEnabled(repoId, mirror, enabled)
        }
    }

    override fun deleteUserMirror(mirror: Mirror) {
        ioScope.launch {
            repoManager.deleteUserMirror(repoId, mirror)
        }
    }

    override fun setArchiveRepoEnabled(enabled: Boolean) {
        ioScope.launch {
            val repo = repoFlow.value ?: return@launch
            archiveStateFlow.value = ArchiveState.LOADING
            try {
                val archiveRepoId = repoManager.setArchiveRepoEnabled(
                    repository = repo,
                    enabled = enabled,
                    proxy = settingsManager.proxyConfig,
                )
                archiveStateFlow.value = enabled.toArchiveState()
                if (enabled && archiveRepoId != null) withContext(Dispatchers.Main) {
                    RepoUpdateWorker.updateNow(application, archiveRepoId)
                }
            } catch (e: Exception) {
                log.error(e) { "Error toggling archive repo: " }
                archiveStateFlow.value = repo.archiveState()
            }
        }
    }

    override fun onOnboardingSeen() = onboardingManager.onRepoDetailsOnboardingSeen()

    private fun Repository.archiveState(): ArchiveState {
        val isEnabled = repoManager.getRepositories().find { r ->
            r.isArchiveRepo && r.certificate == certificate
        }?.enabled
        return when (isEnabled) {
            true -> ArchiveState.ENABLED
            false -> ArchiveState.DISABLED
            null -> UNKNOWN
        }
    }

    private fun Boolean.toArchiveState(): ArchiveState {
        return if (this) ArchiveState.ENABLED else ArchiveState.DISABLED
    }

    @AssistedFactory
    interface Factory {
        fun create(repoId: Long): RepoDetailsViewModel
    }
}
