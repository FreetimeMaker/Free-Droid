package org.fdroid.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Ignore
import androidx.room.ColumnInfo
import androidx.core.os.LocaleListCompat
import org.fdroid.index.v2.LocalizedFileV2
import kotlinx.serialization.Serializable

@Entity(tableName = "app")
public data class App(
    @PrimaryKey
    @ColumnInfo(name = "repoId")
    public val repoIdValue: Long,
    @ColumnInfo(name = "packageName")
    public val packageNameValue: String,
    public val metadata: AppMetadata
) : MinimalApp() {
    
    @Ignore
    public override fun getPackageName(): String = packageNameValue
    
    @Ignore
    public override fun getName(): String = metadata.name.values.firstOrNull() ?: ""
    
    @Ignore
    public override fun getSummary(): String = metadata.summary.values.firstOrNull() ?: ""
    
    @Ignore
    public override fun getIcon(locales: LocaleListCompat): LocalizedFileV2? {
        // Find best matching locale or fallback to first available
        return metadata.icon
    }
    
    @Ignore
    public override fun getRepoId(): Long = repoIdValue
}

@Serializable
public data class AppMetadata(
    public val name: Map<String, String>,
    public val summary: Map<String, String>,
    public val description: Map<String, String>,
    public val icon: LocalizedFileV2?,
    public val featureGraphic: LocalizedFileV2?,
    public val promoGraphic: LocalizedFileV2?,
    public val tvBanner: LocalizedFileV2?,
    public val video: String?,
    public val authorName: String?,
    public val phoneScreenshots: Map<String, List<LocalizedFileV2>>,
    public val sevenInchScreenshots: Map<String, List<LocalizedFileV2>>,
    public val tenInchScreenshots: Map<String, List<LocalizedFileV2>>,
    public val tvScreenshots: Map<String, List<LocalizedFileV2>>,
    public val wearScreenshots: Map<String, List<LocalizedFileV2>>
)
