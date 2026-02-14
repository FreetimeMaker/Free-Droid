package org.fdroid.repo

import androidx.core.os.LocaleListCompat
import com.freetime.freedroid.LocaleChooser.getBestLocale
import com.freetime.freedroid.database.AppOverviewItem
import com.freetime.freedroid.database.LocalizedIcon
import com.freetime.freedroid.database.Repository
import com.freetime.freedroid.database.RepositoryPreferences
import com.freetime.freedroid.database.toCoreRepository
import com.freetime.freedroid.database.toMirrors
import com.freetime.freedroid.database.toRepoAntiFeatures
import com.freetime.freedroid.database.toRepoCategories
import com.freetime.freedroid.database.toRepoReleaseChannel
import com.freetime.freedroid.index.IndexFormatVersion
import com.freetime.freedroid.index.v2.IndexV2StreamReceiver
import com.freetime.freedroid.index.v2.PackageV2
import com.freetime.freedroid.index.v2.RepoV2

internal open class RepoV2StreamReceiver(
    private val receiver: RepoPreviewReceiver,
    private val certificate: String,
    private val username: String?,
    private val password: String?,
) : IndexV2StreamReceiver {

    companion object {
        fun getRepository(
            repo: RepoV2,
            version: Long,
            formatVersion: IndexFormatVersion,
            certificate: String,
            username: String?,
            password: String?,
        ) = Repository(
            repository = repo.toCoreRepository(
                version = version,
                formatVersion = formatVersion,
                certificate = certificate
            ),
            mirrors = repo.mirrors.toMirrors(REPO_ID),
            antiFeatures = repo.antiFeatures.toRepoAntiFeatures(REPO_ID),
            categories = repo.categories.toRepoCategories(REPO_ID),
            releaseChannels = repo.releaseChannels.toRepoReleaseChannel(REPO_ID),
            preferences = RepositoryPreferences(
                repoId = REPO_ID,
                weight = 0,
                enabled = true,
                username = username,
                password = password,
            ),
        )

        fun getAppOverViewItem(
            packageName: String,
            p: PackageV2,
            locales: LocaleListCompat,
        ) = AppOverviewItem(
            repoId = REPO_ID,
            packageName = packageName,
            added = p.metadata.added,
            lastUpdated = p.metadata.lastUpdated,
            name = p.metadata.name.getBestLocale(locales),
            summary = p.metadata.summary.getBestLocale(locales),
            internalName = p.metadata.name,
            internalSummary = p.metadata.summary,
            antiFeatures = p.versions.values.lastOrNull()?.antiFeatures,
            localizedIcon = p.metadata.icon?.map { (locale, file) ->
                LocalizedIcon(
                    repoId = 0L,
                    packageName = packageName,
                    type = "icon",
                    locale = locale,
                    name = file.name,
                    sha256 = file.sha256,
                    size = file.size,
                    ipfsCidV1 = file.ipfsCidV1,
                )
            },
            isCompatible = true, // not concerned with compatibility at this point
        )
    }

    private val locales: LocaleListCompat = LocaleListCompat.getDefault()

    override fun receive(repo: RepoV2, version: Long) {
        receiver.onRepoReceived(
            getRepository(
                repo = repo,
                version = version,
                formatVersion = IndexFormatVersion.TWO,
                certificate = certificate,
                username = username,
                password = password,
            )
        )
    }

    override fun receive(packageName: String, p: PackageV2) {
        receiver.onAppReceived(getAppOverViewItem(packageName, p, locales))
    }

    override fun onStreamEnded() {
    }

}
