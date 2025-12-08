package data

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.PredefinedSite

@Serializable
data class SitesResponse(
    val sites: List<PredefinedSiteSerializable>
)

@Serializable
data class PredefinedSiteSerializable(
    val name: String,
    val category: String,
    val url: String,
    val description: String
) {
    fun toPredefinedSite() = PredefinedSite(name, category, url, description)
    
    companion object {
        fun fromPredefinedSite(site: PredefinedSite) = PredefinedSiteSerializable(
            name = site.name,
            category = site.category,
            url = site.url,
            description = site.description
        )
    }
}

class PredefinedSitesRepository(
    private val settings: Settings,
    private val httpClient: HttpClient? = null
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val customSitesKey = "custom_predefined_sites"
    private val remoteSitesUrlKey = "remote_sites_url"
    private val defaultSitesUrl = "https://files.gb-coding.nl/hosting/lovable-sites.json"
    private val defaultSitesLoadedKey = "default_sites_loaded"
    
    private val defaultSites = listOf(
            // Centrale homepage
            PredefinedSite(
                name = "GB App",
                category = "📝 Website",
                url = "https://app.gb-coding.nl",
                description = "Centrale homepage met overzicht van al je projecten en blogs."
            ),
            
            // Productiviteit & Planning
            PredefinedSite(
                name = "Urenregistratie",
                category = "📝 Productiviteit & Planning",
                url = "https://hours.gb-coding.nl",
                description = "Urenregistratie voor projecten of werk."
            ),
            PredefinedSite(
                name = "Kilometerregistratie",
                category = "📝 Productiviteit & Planning",
                url = "https://km.gb-coding.nl",
                description = "Kilometerregistratie voor ritten en zakelijke kilometers."
            ),
            PredefinedSite(
                name = "Todo Lijst",
                category = "📝 Productiviteit & Planning",
                url = "https://todo.gb-coding.nl",
                description = "Persoonlijke en professionele takenlijst met overzichtelijke interface."
            ),
            
            // Beheer & Licenties
            PredefinedSite(
                name = "Licentiebeheer",
                category = "💻 Beheer & Licenties",
                url = "https://license.gb-coding.nl",
                description = "Beheer software- en toollicenties centraal."
            ),
            
            // Leren & Skills
            PredefinedSite(
                name = "Skills & Tutorials",
                category = "🎓 Leren & Skills",
                url = "https://skills.gb-coding.nl",
                description = "Tips, tutorials en programmeerkennis op één plek."
            ),
            
            // Financieel & Investeringen
            PredefinedSite(
                name = "Vaste Lasten",
                category = "📊 Financieel & Investeringen",
                url = "https://bills.gb-coding.nl",
                description = "Houd je vaste lasten bij en krijg direct overzicht."
            ),
            PredefinedSite(
                name = "Investeringen",
                category = "📊 Financieel & Investeringen",
                url = "https://invest.gb-coding.nl",
                description = "Beheer en volg je investeringen eenvoudig."
            ),
            
            // Herstelgerelateerd
            PredefinedSite(
                name = "Persoonlijk Herstel",
                category = "📊 Herstelgerelateerd",
                url = "https://stap10.gb-coding.nl",
                description = "Volg persoonlijke doelen en programma's."
            ),
            PredefinedSite(
                name = "Meeting Log",
                category = "📊 Herstelgerelateerd",
                url = "https://meetings.gb-coding.nl",
                description = "Persoonlijke log voor bezoeken van meetings."
            ),
            PredefinedSite(
                name = "JSONHoster",
                category = "📝 Productiviteit & Planning",
                url = "https://files.gb-coding.nl",
                description = "Hosten van JSON files."
            )
        )
    
    fun getAllSites(): List<PredefinedSite> {
        val customSites = loadCustomSites()
        val remoteSites = loadRemoteSites()
        // If we have remote sites loaded, use those instead of defaults
        // Otherwise use default sites as fallback
        val baseSites = if (remoteSites.isNotEmpty()) remoteSites else defaultSites
        return baseSites + customSites
    }
    
    suspend fun loadDefaultSitesIfNeeded(): Boolean {
        // Only load if we haven't loaded them before or if we don't have remote sites
        val hasRemoteSites = loadRemoteSites().isNotEmpty()
        val defaultSitesLoaded = settings.getBoolean(defaultSitesLoadedKey, false)
        
        if (hasRemoteSites && defaultSitesLoaded) {
            return true // Already have sites loaded
        }
        
        if (httpClient == null) {
            return false
        }
        
        return try {
            val result = loadSitesFromUrl(defaultSitesUrl)
            if (result.isSuccess) {
                settings.putBoolean(defaultSitesLoadedKey, true)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun loadSitesFromUrl(url: String): Result<List<PredefinedSite>> {
        return try {
            if (httpClient == null) {
                return Result.failure(Exception("HTTP client not available"))
            }
            
            // Try to load from the original URL first
            var finalUrl = url
            var response: HttpResponse = httpClient.get(url)
            
            // Check for redirects (3xx status codes) and follow them
            var redirectCount = 0
            val maxRedirects = 5 // Prevent infinite redirect loops
            
            while (response.status.value in 300..399 && redirectCount < maxRedirects) {
                val location = response.headers["Location"]
                if (location != null) {
                    // Resolve relative URLs to absolute URLs
                    finalUrl = when {
                        location.startsWith("http://") || location.startsWith("https://") -> location
                        location.startsWith("//") -> {
                            val protocol = url.substringBefore("://")
                            "$protocol:$location"
                        }
                        location.startsWith("/") -> {
                            val protocol = url.substringBefore("://")
                            val host = url.substringAfter("://").substringBefore("/")
                            "$protocol://$host$location"
                        }
                        else -> {
                            val baseUrl = url.substringBeforeLast("/")
                            "$baseUrl/$location".replace("//", "/").replace(":/", "://")
                        }
                    }
                    // Try again with the redirect URL
                    response = httpClient.get(finalUrl)
                    redirectCount++
                } else {
                    break // No Location header, can't follow redirect
                }
            }
            
            // Check HTTP status code before trying to deserialize
            if (response.status.value !in 200..299) {
                val errorMessage = when (response.status.value) {
                    404 -> "Bestand niet gevonden (404). Controleer of de URL correct is."
                    403 -> "Toegang geweigerd (403). Je hebt geen rechten om dit bestand te openen."
                    500 -> "Serverfout (500). De server heeft een probleem."
                    else -> "HTTP fout ${response.status.value}: ${response.status.description}"
                }
                return Result.failure(Exception(errorMessage))
            }
            
            val sitesResponse: SitesResponse = response.body()
            
            val sites = sitesResponse.sites.map { it.toPredefinedSite() }
            
            // Save the final URL (after redirect) for future use
            settings.putString(remoteSitesUrlKey, finalUrl)
            
            // Save remote sites locally
            saveRemoteSites(sites)
            
            Result.success(sites)
        } catch (e: Exception) {
            // Provide a more user-friendly error message
            val errorMessage = when {
                e.message?.contains("404") == true -> "Bestand niet gevonden. Controleer of de URL correct is."
                e.message?.contains("NoTransformationFoundException") == true -> "Ongeldig JSON-formaat. Controleer of de URL een geldig JSON-bestand retourneert."
                e.message?.contains("Connection") == true -> "Kan geen verbinding maken. Controleer je internetverbinding."
                else -> e.message ?: "Onbekende fout bij het laden van de URL."
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    fun getRemoteSitesUrl(): String? {
        return settings.getStringOrNull(remoteSitesUrlKey) ?: defaultSitesUrl
    }
    
    fun getDefaultSitesUrl(): String {
        return defaultSitesUrl
    }
    
    fun getSitesByCategory(): Map<String, List<PredefinedSite>> {
        return getAllSites().groupBy { it.category.ifBlank { "Algemeen" } }
    }
    
    fun addCustomSite(site: PredefinedSite) {
        val customSites = loadCustomSites().toMutableList()
        customSites.add(site)
        saveCustomSites(customSites)
    }
    
    private fun loadCustomSites(): List<PredefinedSite> {
        val jsonString = settings.getStringOrNull(customSitesKey)
        return if (jsonString != null) {
            try {
                val serializable = json.decodeFromString<List<PredefinedSiteSerializable>>(jsonString)
                serializable.map { it.toPredefinedSite() }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    private fun saveCustomSites(sites: List<PredefinedSite>) {
        val serializable = sites.map { PredefinedSiteSerializable.fromPredefinedSite(it) }
        val jsonString = json.encodeToString(serializable)
        settings.putString(customSitesKey, jsonString)
    }
    
    private fun loadRemoteSites(): List<PredefinedSite> {
        val jsonString = settings.getStringOrNull("remote_sites")
        return if (jsonString != null) {
            try {
                val serializable = json.decodeFromString<List<PredefinedSiteSerializable>>(jsonString)
                serializable.map { it.toPredefinedSite() }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    private fun saveRemoteSites(sites: List<PredefinedSite>) {
        val serializable = sites.map { PredefinedSiteSerializable.fromPredefinedSite(it) }
        val jsonString = json.encodeToString(serializable)
        settings.putString("remote_sites", jsonString)
    }
}

