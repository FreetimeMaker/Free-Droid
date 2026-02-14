package org.fdroid.repo

import android.net.Uri
import androidx.core.os.LocaleListCompat
import kotlinx.serialization.SerializationException
import com.freetime.freedroid.database.Repository
import com.freetime.freedroid.download.DownloaderFactory
import com.freetime.freedroid.index.IndexConverter
import com.freetime.freedroid.index.IndexFormatVersion
import com.freetime.freedroid.index.IndexParser
import com.freetime.freedroid.index.RepoUriBuilder
import com.freetime.freedroid.index.SigningException
import com.freetime.freedroid.index.TempFileProvider
import com.freetime.freedroid.index.parseV1
import com.freetime.freedroid.index.v1.IndexV1Verifier
import com.freetime.freedroid.index.v1.SIGNED_FILE_NAME
import com.freetime.freedroid.index.v2.FileV2
import java.io.File

internal class RepoV1Fetcher(
    private val tempFileProvider: TempFileProvider,
    private val downloaderFactory: DownloaderFactory,
    private val repoUriBuilder: RepoUriBuilder,
) : RepoFetcher {

    private val locales: LocaleListCompat = LocaleListCompat.getDefault()

    @Throws(SigningException::class, SerializationException::class)
    override suspend fun fetchRepo(
        uri: Uri,
        repo: Repository,
        receiver: RepoPreviewReceiver,
        fingerprint: String?,
    ): File {
        // download and verify index-v1.jar
        val indexFile = tempFileProvider.createTempFile(null)
        val entryDownloader = downloaderFactory.create(
            repo = repo,
            uri = repoUriBuilder.getUri(repo, SIGNED_FILE_NAME),
            indexFile = FileV2.fromPath("/$SIGNED_FILE_NAME"),
            destFile = indexFile,
        )
        entryDownloader.download()
        val verifier = IndexV1Verifier(indexFile, null, fingerprint)
        val (cert, indexV1) = verifier.getStreamAndVerify { inputStream ->
            IndexParser.parseV1(inputStream)
        }
        val version = indexV1.repo.version
        val indexV2 = IndexConverter().toIndexV2(indexV1)
        val receivedRepo = RepoV2StreamReceiver.getRepository(
            repo = indexV2.repo,
            version = version.toLong(),
            formatVersion = IndexFormatVersion.ONE,
            certificate = cert,
            username = repo.username,
            password = repo.password,
        )
        receiver.onRepoReceived(receivedRepo)
        indexV2.packages.forEach { (packageName, packageV2) ->
            val app = RepoV2StreamReceiver.getAppOverViewItem(packageName, packageV2, locales)
            receiver.onAppReceived(app)
        }
        return indexFile
    }
}
