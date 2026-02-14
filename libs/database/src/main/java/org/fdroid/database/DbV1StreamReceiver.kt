package org.fdroid.database

import androidx.core.os.LocaleListCompat
import com.freetime.freedroid.CompatibilityChecker
import com.freetime.freedroid.index.IndexFormatVersion.ONE
import com.freetime.freedroid.index.v1.IndexV1StreamReceiver
import com.freetime.freedroid.index.v2.AntiFeatureV2
import com.freetime.freedroid.index.v2.CategoryV2
import com.freetime.freedroid.index.v2.MetadataV2
import com.freetime.freedroid.index.v2.PackageVersionV2
import com.freetime.freedroid.index.v2.ReleaseChannelV2
import com.freetime.freedroid.index.v2.RepoV2

/**
 * Note that this class expects that its [receive] method with [RepoV2] gets called first.
 * A different order of calls is not supported.
 */
@Deprecated("Use DbV2StreamReceiver instead")
internal class DbV1StreamReceiver(
    private val db: FDroidDatabaseInt,
    private val repoId: Long,
    private val compatibilityChecker: CompatibilityChecker,
) : IndexV1StreamReceiver {

    private val locales: LocaleListCompat = LocaleListCompat.getDefault()

    override fun receive(repo: RepoV2, version: Long) {
        db.getRepositoryDao().clear(repoId)
        db.getRepositoryDao().update(repoId, repo, version, ONE)
    }

    override fun receive(packageName: String, m: MetadataV2) {
        db.getAppDao().insert(repoId, packageName, m, locales)
    }

    override fun receive(packageName: String, v: Map<String, PackageVersionV2>) {
        db.getVersionDao().insert(repoId, packageName, v) {
            compatibilityChecker.isCompatible(it.manifest)
        }
    }

    override fun updateRepo(
        antiFeatures: Map<String, AntiFeatureV2>,
        categories: Map<String, CategoryV2>,
        releaseChannels: Map<String, ReleaseChannelV2>,
    ) {
        val repoDao = db.getRepositoryDao()
        repoDao.insertAntiFeatures(antiFeatures.toRepoAntiFeatures(repoId))
        repoDao.insertCategories(categories.toRepoCategories(repoId))
        repoDao.insertReleaseChannels(releaseChannels.toRepoReleaseChannel(repoId))

        db.afterUpdatingRepo(repoId)
    }

    override fun updateAppMetadata(packageName: String, preferredSigner: String?) {
        db.getAppDao().updatePreferredSigner(repoId, packageName, preferredSigner)
    }

}
