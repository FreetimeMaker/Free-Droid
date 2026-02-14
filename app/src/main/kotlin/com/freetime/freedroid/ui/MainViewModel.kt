package com.freetime.freedroid.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import com.freetime.freedroid.settings.SettingsManager
import com.freetime.freedroid.updates.UpdatesManager
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsManager: SettingsManager,
    updatesManager: UpdatesManager,
) : ViewModel() {
    val dynamicColors = settingsManager.dynamicColorFlow
    val numUpdates = updatesManager.numUpdates
    val hasAppIssues = updatesManager.appsWithIssues.map { !it.isNullOrEmpty() }
}
