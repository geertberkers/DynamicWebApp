package data

import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.SiteConfig

@Serializable
private data class SiteConfigSerializable(
    val id: Int,
    val url: String = "",
    val title: String = "",
    val iconUrl: String? = null
) {
    fun toSiteConfig() = SiteConfig(id, url, title, iconUrl)
    
    companion object {
        fun fromSiteConfig(config: SiteConfig) = SiteConfigSerializable(
            id = config.id,
            url = config.url,
            title = config.title,
            iconUrl = config.iconUrl
        )
    }
}

class SiteSettingsRepository(private val settings: Settings) {
    private val json = Json { ignoreUnknownKeys = true }
    private val sitesKey = "sites"
    
    fun loadSites(): List<SiteConfig> {
        val jsonString = settings.getStringOrNull(sitesKey)
        return if (jsonString != null) {
            try {
                val serializable = json.decodeFromString<List<SiteConfigSerializable>>(jsonString)
                serializable.map { it.toSiteConfig() }
            } catch (e: Exception) {
                getDefaultSites()
            }
        } else {
            getDefaultSites()
        }
    }
    
    fun saveSites(sites: List<SiteConfig>) {
        val serializable = sites.map { SiteConfigSerializable.fromSiteConfig(it) }
        val jsonString = json.encodeToString(serializable)
        settings.putString(sitesKey, jsonString)
    }
    
    private fun getDefaultSites(): List<SiteConfig> {
        return listOf(
            SiteConfig(id = 0),
            SiteConfig(id = 1),
            SiteConfig(id = 2),
            SiteConfig(id = 3),
            SiteConfig(id = 4)
        )
    }
}

