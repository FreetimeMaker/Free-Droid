@file:Suppress("DEPRECATION")

package org.fdroid.index.v1

import mu.KotlinLogging
import com.freetime.freedroid.CompatibilityChecker
import com.freetime.freedroid.database.DbV1StreamReceiver
import com.freetime.freedroid.database.FDroidDatabase
import com.freetime.freedroid.database.FDroidDatabaseInt
import com.freetime.freedroid.database.Repository
import com.freetime.freedroid.database.RepositoryDaoInt
import com.freetime.freedroid.download.DownloaderFactory
import com.freetime.freedroid.index.IndexFormatVersion
import com.freetime.freedroid.index.IndexFormatVersion.ONE
import com.freetime.freedroid.index.IndexUpdateListener
import com.freetime.freedroid.index.IndexUpdateResult
import com.freetime.freedroid.index.IndexUpdater
import com.freetime.freedroid.index.RepoUriBuilder
import com.freetime.freedroid.index.TempFileProvider
import com.freetime.freedroid.index.defaultRepoUriBuilder
import com.freetime.freedroid.index.setIndexUpdateListener
import com.freetime.freedroid.index.v2.FileV2

public const val SIGNED_FILE_NAME: String = "index-v1.jar"

public class IndexV1Updater(
    database: FDroidDatabase,
    private val tempFileProvider: TempFileProvider,
    private val downloaderFactory: DownloaderFactory,
    private val repoUriBuilder: RepoUriBuilder = defaultRepoUriBuilder,
    private val compatibilityChecker: CompatibilityChecker,
    private val listener: IndexUpdateListener? = null,
) : IndexUpdater() {

    private val log = KotlinLogging.logger {}
    public override val formatVersion: IndexFormatVersion = ONE
    private val db: FDroidDatabaseInt = database as FDroidDatabaseInt
    override val repoDao: RepositoryDaoInt = db.getRepositoryDao()

    override fun updateRepo(repo: Repository): IndexUpdateResult {
        // Normally, we shouldn't allow repository downgrades and assert the condition below.
        // However, F-Droid is concerned that late v2 bugs will require users to downgrade to v1,
        // as it happened already with the migration from v0 to v1.
        if (repo.formatVersion != null && repo.formatVersion != ONE) {
            log.error { "Format downgrade for ${repo.address}" }
        }
        val file = tempFileProvider.createTempFile(null)
        val downloader = downloaderFactory.createWithTryFirstMirror(
            repo = repo,
            uri = repoUriBuilder.getUri(repo, SIGNED_FILE_NAME),
            indexFile = FileV2.fromPath("/$SIGNED_FILE_NAME"),
            destFile = file,
        ).apply {
            cacheTag = repo.lastETag
            setIndexUpdateListener(listener, repo)
        }
        try {
            downloader.download()
            if (!downloader.hasChanged()) return IndexUpdateResult.Unchanged
            val eTag = downloader.cacheTag

            val verifier = IndexV1Verifier(file, repo.certificate, null)
            db.runInTransaction {
                verifier.getStreamAndVerify { inputStream ->
                    listener?.onUpdateProgress(repo, 0, 0)
                    val streamReceiver = DbV1StreamReceiver(db, repo.repoId, compatibilityChecker)
                    val streamProcessor = IndexV1StreamProcessor(streamReceiver, repo.timestamp)
                    streamProcessor.process(inputStream)
                }
                // update RepositoryPreferences with timestamp and ETag (for v1)
                val updatedPrefs = repo.preferences.copy(
                    lastUpdated = System.currentTimeMillis(),
                    lastETag = eTag,
                    errorCount = 0,
                    lastError = null,
                )
                repoDao.updateRepositoryPreferences(updatedPrefs)
            }
        } catch (e: OldIndexException) {
            if (e.isSameTimestamp) return IndexUpdateResult.Unchanged
            else throw e
        } finally {
            file.delete()
        }
        return IndexUpdateResult.Processed
    }
}
