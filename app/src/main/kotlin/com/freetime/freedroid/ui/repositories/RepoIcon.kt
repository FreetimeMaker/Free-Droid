package com.freetime.freedroid.ui.repositories

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.os.LocaleListCompat
import io.ktor.client.engine.ProxyConfig
import com.freetime.freedroid.R
import com.freetime.freedroid.database.Repository
import com.freetime.freedroid.download.getImageModel
import com.freetime.freedroid.ui.utils.AsyncShimmerImage

@Composable
fun RepoIcon(repo: Repository, proxy: ProxyConfig?, modifier: Modifier = Modifier) {
    AsyncShimmerImage(
        model = repo.getIcon(LocaleListCompat.getDefault())?.getImageModel(repo, proxy),
        contentDescription = null,
        error = painterResource(R.drawable.ic_repo_app_default),
        modifier = modifier,
    )
}
