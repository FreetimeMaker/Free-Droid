package org.fdroid.index.v2

import com.freetime.freedroid.CompatibilityChecker
import com.freetime.freedroid.database.DbV2DiffStreamReceiver
import com.freetime.freedroid.database.DbV2StreamReceiver
import com.freetime.freedroid.database.FDroidDatabase
import com.freetime.freedroid.database.FDroidDatabaseInt
import com.freetime.freedroid.database.Repository
import com.freetime.freedroid.database.RepositoryDaoInt
import com.freetime.freedroid.download.DownloaderFactory
import com.freetime.freedroid.index.IndexFormatVersion
import com.freetime.freedroid.index.IndexFormatVersion.ONE
import com.freetime.freedroid.index.IndexFormatVersion.TWO
import com.freetime.freedroid.index.IndexParser
import com.freetime.freedroid.index.IndexUpdateListener
import com.freetime.freedroid.index.IndexUpdateResult
import com.freetime.freedroid.index.IndexUpdater
import com.freetime.freedroid.index.RepoUriBuilder
import com.freetime.freedroid.index.TempFileProvider
import com.freetime.freedroid.index.defaultRepoUriBuilder
import com.freetime.freedroid.index.parseEntry
import com.freetime.freedroid.index.setIndexUpdateListener

public const val SIGNED_FILE_NAME: String = "entry.jar"

public class IndexV2Updater(
    database: FDroidDatabase,
    private val tempFileProvider: TempFileProvider,
    private val downloaderFactory: DownloaderFactory,
    private val repoUriBuilder: RepoUriBuilder = defaultRepoUriBuilder,
    private val compatibilityChecker: CompatibilityChecker,
    private val listener: IndexUpdateListener? = null,
) : IndexUpdater() {

    public override val formatVersion: IndexFormatVersion = TWO
    private val db: FDroidDatabaseInt = database as FDroidDatabaseInt
    override val repoDao: RepositoryDaoInt = db.getRepositoryDao()

    override fun updateRepo(repo: Repository): IndexUpdateResult {
        val (_, entry) = getCertAndEntry(repo, repo.certificate)
        // don't process repos that we already did process in the past
        if (entry.timestamp <= repo.timestamp) return IndexUpdateResult.Unchanged
        // get diff, if available
        val diff = entry.getDiff(repo.timestamp)
        return if (diff == null || repo.formatVersion == ONE) {
            // no diff found (or this is upgrade from v1 repo), so do full index update
            val streamReceiver = DbV2StreamReceiver(db, repo.repoId, compatibilityChecker)
            val streamProcessor = IndexV2FullStreamProcessor(streamReceiver)
            processStream(repo, entry.index, entry.version, streamProcessor)
        } else {
            // use available diff
            val streamReceiver = DbV2DiffStreamReceiver(db, repo.repoId, compatibilityChecker)
            val streamProcessor = IndexV2DiffStreamProcessor(streamReceiver)
            processStream(repo, diff, entry.version, streamProcessor)
        }
    }

    private fun getCertAndEntry(repo: Repository, certificate: String): Pair<String, Entry> {
        val file = tempFileProvider.createTempFile(null)
        val downloader = downloaderFactory.createWithTryFirstMirror(
            repo = repo,
            uri = repoUriBuilder.getUri(repo, SIGNED_FILE_NAME),
            indexFile = FileV2.fromPath("/$SIGNED_FILE_NAME"),
            destFile = file,
        ).apply {
            if (listener != null) setListener { bytesRead, _ ->
                // don't report a total for entry.jar,
                // because we'll download another file afterwards
                // and progress reporting would jump to 100% two times.
                listener.onDownloadProgress(repo, bytesRead, -1)
            }
        }
        try {
            downloader.download()
            val verifier = EntryVerifier(file, certificate, null)
            return verifier.getStreamAndVerify { inputStream ->
                IndexParser.parseEntry(inputStream)
            }
        } finally {
            file.delete()
        }
    }

    private fun processStream(
        repo: Repository,
        entryFile: EntryFileV2,
        repoVersion: Long,
        streamProcessor: IndexV2StreamProcessor,
    ): IndexUpdateResult {
        val file = tempFileProvider.createTempFile(entryFile.sha256)
        val downloader = downloaderFactory.createWithTryFirstMirror(
            repo = repo,
            uri = repoUriBuilder.getUri(repo, entryFile.name.trimStart('/')),
            indexFile = entryFile,
            destFile = file,
        ).apply {
            setIndexUpdateListener(listener, repo)
        }
        try {
            downloader.download()
            file.inputStream().use { inputStream ->
                db.runInTransaction {
                    // ensure somebody else hasn't updated the repo in the meantime
                    val currentTimestamp = repoDao.getRepository(repo.repoId)?.timestamp
                    if (currentTimestamp != repo.timestamp) throw ConcurrentModificationException(
                        "Repo timestamp expected ${repo.timestamp}, but was $currentTimestamp"
                    )
                    // still the expected timestamp, so go on processing...
                    streamProcessor.process(repoVersion, inputStream) { i ->
                        listener?.onUpdateProgress(repo, i, entryFile.numPackages)
                    }
                    // update RepositoryPreferences with timestamp
                    val repoPrefs = repoDao.getRepositoryPreferences(repo.repoId)
                        ?: error("No repo prefs for ${repo.repoId}")
                    val updatedPrefs = repoPrefs.copy(
                        lastUpdated = System.currentTimeMillis(),
                        errorCount = 0,
                        lastError = null,
                    )
                    repoDao.updateRepositoryPreferences(updatedPrefs)
                }
            }
        } finally {
            file.delete()
        }
        return IndexUpdateResult.Processed
    }
}
