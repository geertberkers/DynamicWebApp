package app

import androidx.compose.runtime.Composable
import com.russhwolf.settings.Settings
import data.SiteSettingsRepository
import data.WebsiteMetadataService
import io.ktor.client.HttpClient
import ui.AppRoot

@Composable
fun App(settings: Settings, httpClient: HttpClient) {
    val repository = SiteSettingsRepository(settings)
    val metadataService = WebsiteMetadataService(httpClient)
    
    AppRoot(
        repository = repository,
        metadataService = metadataService,
        settings = settings,
        httpClient = httpClient
    )
}

