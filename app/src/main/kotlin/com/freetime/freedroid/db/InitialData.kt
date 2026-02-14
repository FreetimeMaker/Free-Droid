package com.freetime.freedroid.db

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.freetime.freedroid.database.FDroidDatabase
import com.freetime.freedroid.database.FDroidFixture
import com.freetime.freedroid.repo.RepoPreLoader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitialData @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repoPreLoader: RepoPreLoader,
) : FDroidFixture {
    override fun prePopulateDb(db: FDroidDatabase) {
        repoPreLoader.addPreloadedRepositories(db)
        // we are kicking off the initial update from the UI,
        // not here to account for metered connection
    }
}
