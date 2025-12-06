package data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import model.SiteConfig
import model.WebsiteMetadata

class WebsiteMetadataService(private val httpClient: HttpClient) {
    
    suspend fun fetchMetadata(rawUrl: String): Result<WebsiteMetadata> {
        return try {
            val normalizedUrl = normalizeUrl(rawUrl)
            val response: HttpResponse = httpClient.get(normalizedUrl)
            val html: String = response.body()
            
            val title = extractTitle(html)
            val iconUrl = extractIconUrl(html, normalizedUrl)
            
            Result.success(WebsiteMetadata(title = title, iconUrl = iconUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun applyToConfig(old: SiteConfig, metadata: WebsiteMetadata, url: String): SiteConfig {
        return old.copy(
            url = url,
            title = metadata.title.ifBlank { "Site ${old.id + 1}" },
            iconUrl = metadata.iconUrl
        )
    }
    
    private fun normalizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.isNotBlank() -> "https://$trimmed"
            else -> trimmed
        }
    }
    
    private fun extractTitle(html: String): String {
        val titleRegex = Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE)
        val match = titleRegex.find(html)
        return match?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() } ?: ""
    }
    
    private fun extractIconUrl(html: String, baseUrl: String): String? {
        // Try to find <link rel="icon"> or <link rel="shortcut icon">
        val iconRegex = Regex(
            """<link[^>]*rel=["'](?:icon|shortcut\s+icon)["'][^>]*href=["']([^"']+)["']""",
            RegexOption.IGNORE_CASE
        )
        val match = iconRegex.find(html)
        
        if (match != null) {
            val href = match.groupValues[1]
            return when {
                href.startsWith("http://") || href.startsWith("https://") -> href
                href.startsWith("//") -> "https:$href"
                href.startsWith("/") -> {
                    val baseUri = parseBaseUrl(baseUrl)
                    "${baseUri.protocol}://${baseUri.host}$href"
                }
                else -> {
                    val baseUri = parseBaseUrl(baseUrl)
                    "${baseUri.protocol}://${baseUri.host}/$href"
                }
            }
        }
        
        // Fallback to /favicon.ico
        return try {
            val baseUri = parseBaseUrl(baseUrl)
            "${baseUri.protocol}://${baseUri.host}/favicon.ico"
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseBaseUrl(url: String): BaseUri {
        val protocol = if (url.startsWith("https://")) "https" else "http"
        val withoutProtocol = url.removePrefix("http://").removePrefix("https://")
        val host = withoutProtocol.split("/").firstOrNull() ?: ""
        return BaseUri(protocol = protocol, host = host)
    }
    
    private data class BaseUri(val protocol: String, val host: String)
}

