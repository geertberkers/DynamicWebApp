package ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.russhwolf.settings.Settings
import data.PredefinedSitesRepository
import data.SiteSettingsRepository
import data.WebsiteMetadataService
import io.ktor.client.HttpClient
import model.SiteConfig
import model.Tab
import ui.components.BottomNavBar
import ui.screens.SettingsScreen
import ui.screens.SiteScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(
    repository: SiteSettingsRepository,
    metadataService: WebsiteMetadataService,
    settings: Settings,
    httpClient: HttpClient
) {
    var sites by remember { mutableStateOf(repository.loadSites()) }
    var currentTab by remember { mutableStateOf<Tab>(Tab.Site(0)) }
    val predefinedSitesRepository = remember { PredefinedSitesRepository(settings, httpClient) }
    
    // Load default sites from URL on app start
    LaunchedEffect(Unit) {
        predefinedSitesRepository.loadDefaultSitesIfNeeded()
    }
    
    val currentSiteTitle = when (val tab = currentTab) {
        is Tab.Site -> {
            val siteIndex = tab.index
            if (siteIndex in sites.indices) {
                sites[siteIndex].title.ifBlank { "Site ${siteIndex + 1}" }
            } else {
                "DynamicWebApp"
            }
        }
        is Tab.Settings -> "Settings"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = currentSiteTitle) },
                actions = {
                    IconButton(
                        onClick = { currentTab = Tab.Settings }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                sites = sites,
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { paddingValues ->
        when (val tab = currentTab) {
            is Tab.Site -> {
                val siteIndex = tab.index
                if (siteIndex in sites.indices) {
                    SiteScreen(
                        site = sites[siteIndex],
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = paddingValues.calculateTopPadding(),
                                bottom = paddingValues.calculateBottomPadding()
                            )
                    )
                }
            }
            is Tab.Settings -> {
                SettingsScreen(
                    sites = sites,
                    metadataService = metadataService,
                    predefinedSitesRepository = predefinedSitesRepository,
                    onSitesChanged = { updatedSites ->
                        sites = updatedSites
                        repository.saveSites(updatedSites)
                    }
                )
            }
        }
    }
}

