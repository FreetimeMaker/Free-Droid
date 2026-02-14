package org.fdroid.database

import androidx.core.os.LocaleListCompat
import org.fdroid.index.v2.LocalizedFileV2

public abstract class MinimalApp {
    public abstract fun getPackageName(): String
    public abstract fun getName(): String
    public abstract fun getSummary(): String
    public abstract fun getIcon(locales: LocaleListCompat): LocalizedFileV2?
    public abstract fun getRepoId(): Long
}
