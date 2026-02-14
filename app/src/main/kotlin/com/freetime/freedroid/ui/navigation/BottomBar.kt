package com.freetime.freedroid.ui.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import org.fdroid.R
import org.fdroid.ui.FreeDroidContent

@Composable
fun BottomBar(
    numUpdates: Int,
    hasIssues: Boolean,
    currentNavKey: NavKey,
    onNav: (MainNavKey) -> Unit,
) {
    val res = LocalResources.current
    NavigationBar(
        modifier = Modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        topLevelRoutes.forEach { dest ->
            NavigationBarItem(
                icon = { NavIcon(dest, numUpdates, hasIssues) },
                label = { Text(stringResource(dest.label)) },
                selected = dest == currentNavKey,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = {
                    if (dest != currentNavKey) onNav(dest)
                },
                modifier = Modifier.semantics {
                    if (dest == NavigationKey.MyApps) {
                        if (numUpdates > 0) {
                            stateDescription =
                                res.getString(R.string.notification_channel_updates_available_title)
                        } else if (hasIssues) {
                            stateDescription =
                                res.getString(R.string.my_apps_header_apps_with_issue)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun NavigationRail(
    numUpdates: Int,
    hasIssues: Boolean,
    currentNavKey: NavKey,
    onNav: (MainNavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val res = LocalResources.current
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        topLevelRoutes.forEach { dest ->
            NavigationRailItem(
                icon = { NavIcon(dest, numUpdates, hasIssues) },
                label = { Text(stringResource(dest.label)) },
                selected = dest == currentNavKey,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = {
                    if (dest != currentNavKey) onNav(dest)
                },
                modifier = Modifier.semantics {
                    if (dest == NavigationKey.MyApps) {
                        if (numUpdates > 0) {
                            stateDescription =
                                res.getString(R.string.notification_channel_updates_available_title)
                        } else if (hasIssues) {
                            stateDescription =
                                res.getString(R.string.my_apps_header_apps_with_issue)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun NavIcon(dest: MainNavKey, numUpdates: Int, hasIssues: Boolean) {
    BadgedBox(
        badge = {
            if (dest == NavigationKey.MyApps && numUpdates > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Text(
                        text = numUpdates.toString(),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            } else if (dest == NavigationKey.MyApps && hasIssues) {
                Icon(
                    imageVector = Icons.Default.Error,
                    tint = MaterialTheme.colorScheme.error,
                    contentDescription = stringResource(R.string.my_apps_header_apps_with_issue),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    ) {
        Icon(
            dest.icon,
            contentDescription = stringResource(dest.label)
        )
    }
}

@Preview
@Composable
private fun Preview() {
    FreeDroidContent {
        Row {
            NavigationRail(
                numUpdates = 3,
                hasIssues = false,
                currentNavKey = NavigationKey.Discover,
                onNav = {},
            )
            BottomBar(
                numUpdates = 3,
                hasIssues = false,
                currentNavKey = NavigationKey.Discover,
                onNav = {},
            )
        }
    }
}

@Preview
@Composable
private fun PreviewIssues() {
    FreeDroidContent {
        Row {
            NavigationRail(
                numUpdates = 0,
                hasIssues = true,
                currentNavKey = NavigationKey.MyApps,
                onNav = {},
            )
            BottomBar(
                numUpdates = 0,
                hasIssues = true,
                currentNavKey = NavigationKey.MyApps,
                onNav = {},
            )
        }
    }
}
