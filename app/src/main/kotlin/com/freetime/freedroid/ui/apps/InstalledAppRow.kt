package com.freetime.freedroid.ui.apps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freetime.freedroid.R
import com.freetime.freedroid.database.AppIssue
import com.freetime.freedroid.database.NoCompatibleSigner
import com.freetime.freedroid.database.UpdateInOtherRepo
import com.freetime.freedroid.ui.FDroidContent
import com.freetime.freedroid.ui.utils.AsyncShimmerImage
import com.freetime.freedroid.ui.utils.BadgeIcon
import com.freetime.freedroid.ui.utils.Names

@Composable
fun InstalledAppRow(
    app: MyInstalledAppItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    appIssue: AppIssue? = null,
) {
    Column(modifier = modifier) {
        ListItem(
            leadingContent = {
                BadgedBox(badge = {
                    if (appIssue != null) BadgeIcon(
                        icon = Icons.Filled.Error,
                        color = if (appIssue is UpdateInOtherRepo) {
                            MaterialTheme.colorScheme.inverseSurface
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        contentDescription =
                        stringResource(R.string.my_apps_header_apps_with_issue),
                    )
                }) {
                    AsyncShimmerImage(
                        model = app.iconModel,
                        error = painterResource(R.drawable.ic_repo_app_default),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { hideFromAccessibility() },
                    )
                }
            },
            headlineContent = {
                Text(app.name)
            },
            supportingContent = {
                Text(app.installedVersionName)
            },
            colors = ListItemDefaults.colors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    Color.Transparent
                }
            ),
            modifier = modifier,
        )
    }
}

@Preview
@Composable
fun InstalledAppRowPreview() {
    val app = InstalledAppItem(
        packageName = "",
        name = Names.randomName,
        installedVersionName = "1.0.1",
        installedVersionCode = 10001,
        lastUpdated = System.currentTimeMillis() - 5000,
    )
    FDroidContent {
        Column {
            InstalledAppRow(app, false)
            InstalledAppRow(app, true)
            InstalledAppRow(app, false, appIssue = UpdateInOtherRepo(2L))
            InstalledAppRow(app, false, appIssue = NoCompatibleSigner())
        }
    }
}
